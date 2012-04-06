package org.zonedabone.duels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Duels extends JavaPlugin {
	
	public static Map<Player, ItemStack[]> armorStore = new HashMap<Player, ItemStack[]>();
	// Data storage via HashMaps
	public static Map<Player, Duel> duels = new HashMap<Player, Duel>();
	public static Economy economy = null;
	public static FileConfiguration highscores;
	public static Map<Player, ItemStack[]> itemStore = new HashMap<Player, ItemStack[]>();
	
	// ClassListeners
	private final DuelsEntityListener entityListener = new DuelsEntityListener(this);
	private final DuelsPlayerListener playerListener = new DuelsPlayerListener(this);
	
	// ClassListeners
	public boolean _getPerm(Player player, String node) {
		return player.hasPermission(node);
	}
	
	public boolean getPerm(Player player, String node) {
		boolean canGive = _getPerm(player, node);
		if (!canGive) {
			MessageManager.sendMessage(player,"failure.no_perms");
		}
		return canGive;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		if (command.getName().equalsIgnoreCase("duel")) {
			if (sender instanceof ConsoleCommandSender) {
				MessageManager.sendMessage(sender,"failure.client_only");
				return true;
			}
			Player player = (Player) sender;
			if (ConfigManager.DISABLED_WORLDS.contains(player.getWorld().getName())) {
				MessageManager.sendMessage(player,"failure.world_disabled");
				return true;
			}
			String subcommand = args[0];
			if (subcommand.equalsIgnoreCase("challenge") && args.length == 2) {
				if (!getPerm(player, "duels.user.challenge")) {
					return true;
				}
				Player target = player.getServer().getPlayer(args[1]);
				if (duels.get(player) != null) {
					MessageManager.sendMessage(sender,"failure.already_dueling");
				} else if (player == target) {
					MessageManager.sendMessage(sender,"failure.cant_duel_self");
				} else if (target == null || !target.isOnline()) {
					MessageManager.sendMessage(sender,"failure.player_offline", "p", args[1]);
				} else if (player.getLocation().distance(target.getLocation()) > ConfigManager.MAX_DISTANCE) {
					MessageManager.sendMessage(player,"failure.not_in_range", "p", target.getDisplayName(), "r", Integer.toString(ConfigManager.MAX_DISTANCE));
				} else if (duels.get(target) != null && duels.get(target).target == player) {
					duels.put(player, duels.get(target));
					duels.get(target).accept();
					MessageManager.sendMessage(player,"success.self_accept", "p", target.getDisplayName());
					MessageManager.sendMessage(target,"success.other_accept", "p", player.getDisplayName());
					MessageManager.sendMessage(player,"success.config");
					MessageManager.sendMessage(target,"success.config");
				} else {
					duels.put(player, new Duel(player, target));
					MessageManager.sendMessage(player,"success.self_request", "p", target.getDisplayName());
					MessageManager.sendMessage(target,"success.other_request", "p", player.getDisplayName());
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("confirm") && args.length == 1) {
				Duel duel = duels.get(player);
				if (duel == null) {
					MessageManager.sendMessage(player,"failure.not_dueling");
					return true;
				}
				int pstage;
				int ostage;
				if (duel.starter == player) {
					pstage = duel.starterstage;
					ostage = duel.targetstage;
				} else {
					ostage = duel.starterstage;
					pstage = duel.targetstage;
				}
				if (pstage == 2) {
					MessageManager.sendMessage(player,"failure.already_confirmed");
				} else if (ostage == 0) {
					MessageManager.sendMessage(player,"failure.not_config");
				} else {
					if (duel.starter == player) {
						if (duel != null && duel.targetstage >= 1 && duel.starterstage == 1) {
							duel.confirm(player);
						}
					} else {
						if (duel != null && duel.starterstage >= 1 && duel.targetstage == 1) {
							duel.confirm(player);
						}
					}
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("cancel") && args.length == 1) {
				Duel duel = duels.get(player);
				if (duel == null) {
					MessageManager.sendMessage(player,"failure.not_dueling");
				} else if (duel.targetstage == 2 && duel.starterstage == 2) {
					MessageManager.sendMessage(player,"failure.cancel_started");
				} else {
					duel.cancel();
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("surrender") && args.length == 1) {
				Duel duel = duels.get(player);
				if (duel == null) {
					MessageManager.sendMessage(player,"failure.not_dueling");
				} else if (duel.starterstage != 2 || duel.targetstage != 2) {
					MessageManager.sendMessage(player,"failure.surrender_not_started");
				} else {
					duel.lose(player, false);
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("set") && args.length == 3) {
				Duel duel = duels.get(player);
				if (duel == null) {
					MessageManager.sendMessage(player,"failure.not_dueling");
				} else if (duel.starterstage != 1 || duel.targetstage != 1) {
					MessageManager.sendMessage(player,"failure.block_config");
				} else {
					String key = args[1];
					String value = args[2];
					if (key.equalsIgnoreCase("keepitems")) {
						if (!getPerm(player, "duels.user.set.keepitems")) {
							return true;
						}
						if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
							duel.setKeepItems(player, true);
						} else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
							duel.setKeepItems(player, false);
						}
					} else if (key.equalsIgnoreCase("stake")) {
						if (!getPerm(player, "duels.user.set.stake")) {
							return true;
						}
						if (economy != null) {
							int newStake = Integer.parseInt(value);
							duel.setStake(player, newStake);
						}
					} else if (key.equalsIgnoreCase("wolves")) {
						if (!getPerm(player, "duels.user.set.wolves")) {
							return true;
						}
						if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
							duel.setWolves(player, true);
						} else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
							duel.setWolves(player, false);
						}
					} else if (key.equalsIgnoreCase("food")) {
						if (!getPerm(player, "duels.user.set.food")) {
							return true;
						}
						if (value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true")) {
							duel.setFood(player, true);
						} else if (value.equalsIgnoreCase("off") || value.equalsIgnoreCase("false")) {
							duel.setFood(player, false);
						}
					}
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("highscores")) {
				List<String> players = new ArrayList<String>(highscores.getKeys(false));
				Collections.sort(players, new HighscoreComparator());
				player.sendMessage("Top " + Integer.toString(Math.max(ConfigManager.DEFAULT_TOP_COUNT, players.size())) + " Duelists:");
				for (int i = 0; i < Math.max(ConfigManager.DEFAULT_TOP_COUNT, players.size()); i++) {
					player.sendMessage(Integer.toString(i + 1) + ". " + players.get(i));
				}
			}
		} else if (command.getName().equalsIgnoreCase("da")) {
		}
		return false;
	}
	
	@Override
	public void onDisable() {
		try {
			highscores.save(new File("plugins/Duels/highscores.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		setupEconomy();
		// Set configuration values
		ConfigManager.loadConfig(this);
		MessageManager.loadMessages(this);
		//Load metrics
		Metrics m;
		try {
			m = new Metrics(this);
			if (m.start()) {
				this.getLogger().info("Plugin metrics enabled! Thank you!");
			} else {
				this.getLogger().info("You opted out of Duels metrics. =(");
			}
			m.start();
		} catch (IOException e) {
			this.getLogger().warning("Failed to load metrics.");
		}
		// Register Events
		pm.registerEvents(entityListener, this);
		pm.registerEvents(playerListener, this);
		highscores = YamlConfiguration.loadConfiguration(new File("plugins/Duels/highscores.yml"));
	}
	
	private Boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return economy != null;
	}
}
