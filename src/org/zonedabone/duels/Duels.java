package org.zonedabone.duels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Duels extends JavaPlugin {
	
	public static Map<Player, ItemStack[]> armorStore = new HashMap<Player, ItemStack[]>();
	// Configuration memory storage
	// Data storage via HashMaps
	public static Map<Player, Duel> duels = new HashMap<Player, Duel>();
	public static Economy economy = null;
	public static FileConfiguration highscores;
	public static Map<Player, ItemStack[]> itemStore = new HashMap<Player, ItemStack[]>();
	// Default duel settings
	// Configuration memory storage
	public static String MESSAGE_PREFIX = "&4[DUELS]&f";
	public static Map<String, String> messages = new HashMap<String, String>();
	// Default duel settings
	
	public static String getMessage(String msg) {
		return MessageParser.parseMessage(messages.get(msg));
	}
	// ClassListeners
	private final DuelsEntityListener entityListener = new DuelsEntityListener(this);
	Logger log = Logger.getLogger("Minecraft");
	private final DuelsPlayerListener playerListener = new DuelsPlayerListener(this);
	
	// ClassListeners
	public boolean _getPerm(Player player, String node) {
		return player.hasPermission(node);
	}
	
	public boolean getPerm(Player player, String node) {
		boolean canGive = _getPerm(player, node);
		if (!canGive) {
			player.sendMessage(getMessage("NO_PERMS"));
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
				sender.sendMessage(getMessage("CLIENT_ONLY"));
				return true;
			}
			Player player = (Player) sender;
			if (ConfigManager.DISABLED_WORLDS.contains(player.getWorld().getName())) {
				player.sendMessage(getMessage("WORLD_DISABLED"));
				return true;
			}
			String subcommand = args[0];
			if (subcommand.equalsIgnoreCase("challenge") && args.length == 2) {
				if (!getPerm(player, "duels.user.challenge")) {
					return true;
				}
				Player target = player.getServer().getPlayer(args[1]);
				if (duels.get(player) != null) {
					sender.sendMessage(getMessage("ALREADY_DUELING"));
				} else if (player == target) {
					sender.sendMessage(getMessage("CANT_DUEL_SELF"));
				} else if (target == null || !target.isOnline()) {
					sender.sendMessage(MessageParser.parseMessage(messages.get("PLAYER_OFFLINE"), "{PLAYER}", args[1]));
				} else if (player.getLocation().distance(target.getLocation()) > ConfigManager.MAX_DISTANCE) {
					player.sendMessage(MessageParser.parseMessage(messages.get("NOT_IN_RANGE"), "{PLAYER}", target.getDisplayName(), "{RANGE}", Integer.toString(ConfigManager.MAX_DISTANCE)));
				} else if (duels.get(target) != null && duels.get(target).target == player) {
					duels.put(player, duels.get(target));
					duels.get(target).accept();
					player.sendMessage(MessageParser.parseMessage(messages.get("SELF_ACCEPT"), "{PLAYER}", target.getDisplayName()));
					target.sendMessage(MessageParser.parseMessage(messages.get("OTHER_ACCEPT"), "{PLAYER}", player.getDisplayName()));
					player.sendMessage(getMessage("CONFIG"));
					target.sendMessage(getMessage("CONFIG"));
				} else {
					duels.put(player, new Duel(player, target));
					player.sendMessage(MessageParser.parseMessage(messages.get("SELF_REQUEST"), "{PLAYER}", target.getDisplayName()));
					target.sendMessage(MessageParser.parseMessage(messages.get("OTHER_REQUEST"), "{PLAYER}", player.getDisplayName()));
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("confirm") && args.length == 1) {
				Duel duel = duels.get(player);
				if (duel == null) {
					player.sendMessage(getMessage("NOT_DUELING"));
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
					player.sendMessage(getMessage("ALREADY_CONFIRMED"));
				} else if (ostage == 0) {
					player.sendMessage(getMessage("NOT_CONFIG"));
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
					player.sendMessage(getMessage("NOT_DUELING"));
				} else if (duel.targetstage == 2 && duel.starterstage == 2) {
					player.sendMessage(getMessage("CANCEL_STARTED"));
				} else {
					duel.cancel();
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("surrender") && args.length == 1) {
				Duel duel = duels.get(player);
				if (duel == null) {
					player.sendMessage(getMessage("NOT_DUELING"));
				} else if (duel.starterstage != 2 || duel.targetstage != 2) {
					player.sendMessage(getMessage("SURRENDER_NOT_STARTED"));
				} else {
					duel.lose(player, false);
				}
				return true;
			} else if (subcommand.equalsIgnoreCase("set") && args.length == 3) {
				Duel duel = duels.get(player);
				if (duel == null) {
					player.sendMessage(getMessage("NOT_DUELING"));
				} else if (duel.starterstage != 1 || duel.targetstage != 1) {
					player.sendMessage(getMessage("BLOCK_CONFIG"));
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
		PluginDescriptionFile pdf = getDescription();
		log.info(pdf.getName() + " version " + pdf.getVersion() + " DISABLED");
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
		Configuration config = getConfig();
		ConfigManager.loadConfig(this);
		// The prefix that goes in front of all messages
		MESSAGE_PREFIX = config.getString("messageprefix", "&4[DUELS]&f");
		config.set("messageprefix", MESSAGE_PREFIX);
		// Message if sent from console
		messages.put("CLIENT_ONLY", config.getString("messages.clientonly", "Duels can only be used from the client."));
		config.set("messages.clientonly", messages.get("CLIENT_ONLY"));
		// Message if already in a duel
		messages.put("ALREADY_DUELING", config.getString("messages.alreadydueling", "You are currently in a duel!"));
		config.set("messages.alreadydueling", messages.get("ALREADY_DUELING"));
		// Message if not in a duel
		messages.put("NOT_DUELING", config.getString("messages.notdueling", "You're not in a duel!"));
		config.set("messages.notdueling", messages.get("NOT_DUELING"));
		// Message if player tries to duel self
		messages.put("CANT_DUEL_SELF", config.getString("messages.cantduelself", "You can't duel yourself!"));
		config.set("messages.cantduelself", messages.get("CANT_DUEL_SELF"));
		// Message if target is offline
		messages.put("PLAYER_OFFLINE", config.getString("messages.playeroffline", "{PLAYER} is offline."));
		config.set("messages.playeroffline", messages.get("PLAYER_OFFLINE"));
		// Message if target is not within range
		messages.put("NOT_IN_RANGE", config.getString("messages.notinrange", "{PLAYER} is not in range. ({RANGE} blocks)"));
		config.set("messages.notinrange", messages.get("NOT_IN_RANGE"));
		// Message when player accepts an incoming duel request.
		messages.put("SELF_ACCEPT", config.getString("messages.selfaccept", "Accepted {PLAYER}'s duel."));
		config.set("messages.selfaccept", messages.get("SELF_ACCEPT"));
		// Message when target accepts the duel request
		messages.put("OTHER_ACCEPT", config.getString("messages.otheraccept", "{PLAYER} has accepted your duel request."));
		config.set("messages.otheraccept", messages.get("OTHER_ACCEPT"));
		// Message sent to both players when config mode is entered
		messages.put("CONFIG", config.getString("messages.config", "set duel options with /duel set <option> <on/off>"));
		config.set("messages.config", messages.get("CONFIG"));
		// Message sent to starter on /duel challenge <player>
		messages.put("SELF_REQUEST", config.getString("messages.selfrequest", "Duel request sent to {PLAYER}."));
		config.set("messages.selfrequest", messages.get("SELF_REQUEST"));
		// Message sent to target on /duel challenge <player>
		messages.put("OTHER_REQUEST", config.getString("messages.otherrequest", "{PLAYER} has requested to duel with you."));
		config.set("messages.otherrequest", messages.get("OTHER_REQUEST"));
		// Message sent when a player tries to cancel a duel in progress
		messages.put("CANCEL_STARTED", config.getString("messages.cancelstarted", "You can't cancel a duel in progress! Use '/duel surrender' instead."));
		config.set("messages.cancelstarted", messages.get("CANCEL_STARTED"));
		// Message sent when a player surrenders a duel that hasn't started
		messages.put("SURRENDER_NOT_STARTED", config.getString("messages.surrendernotstarted", "The duel has not started yet. Use '/duel cancel' instead."));
		config.set("messages.surrendernotstarted", messages.get("SURRENDER_NOT_STARTED"));
		// Message sent when a player tries to configure a duel at the wrong
		// time
		messages.put("BLOCK_CONFIG", config.getString("messages.blockconfig", "Now is not the time to change duel settings."));
		config.set("messages.blockconfig", messages.get("BLOCK_CONFIG"));
		// Message sent when a player is ready to start
		messages.put("PLAYER_READY", config.getString("messages.playerready", "{PLAYER} is ready to start the duel."));
		config.set("messages.playerready", messages.get("PLAYER_READY"));
		// Message sent when a player is ready to start
		messages.put("DUEL_START", config.getString("messages.duelstart", "FIGHT TO THE DEATH!"));
		config.set("messages.duelstart", messages.get("DUEL_START"));
		// Message sent when a player is ready to start
		messages.put("DUEL_CANCEL", config.getString("messages.duelcancel", "The duel has been canceled."));
		config.set("messages.duelcancel", messages.get("DUEL_CANCEL"));
		// Message sent when a player is ready to start
		messages.put("DUEL_LOSE", config.getString("messages.duellose", "You lost the duel!"));
		config.set("messages.duellose", messages.get("DUEL_LOSE"));
		// Message sent when a player is ready to start
		messages.put("DUEL_WIN", config.getString("messages.duelwin", "You won the duel!"));
		config.set("messages.duelwin", messages.get("DUEL_WIN"));
		// Message sent when a player is ready to start
		messages.put("SET_KEEP_ITEMS", config.getString("messages.setkeepitems", "You will keep you items if you die."));
		config.set("messages.setkeepitems", messages.get("SET_KEEP_ITEMS"));
		// Message sent when a player is ready to start
		messages.put("SET_LOSE_ITEMS", config.getString("messages.setloseitems", "Your opponent will get your items if you die."));
		config.set("messages.setloseitems", messages.get("SET_LOSE_ITEMS"));
		// Message sent when a player sets a new stake
		messages.put("SET_STAKE", config.getString("messages.setstake", "{PLAYER} set their stake to {STAKE}."));
		config.set("messages.setstake", messages.get("SET_STAKE"));
		// Message sent when a player can't afford the stake
		messages.put("BLOCK_SET_STAKE", config.getString("messages.blocksetstake", "You can't afford to set your stake to that."));
		config.set("messages.blocksetstake", messages.get("BLOCK_SET_STAKE"));
		// Message sent when a player enables wolves
		messages.put("WOLF_ENABLE", config.getString("messages.wolfenable", "Wolves are enabled."));
		config.set("messages.wolfenable", messages.get("WOLF_ENABLE"));
		// Message sent when a player disables wolves
		messages.put("WOLF_DISABLE", config.getString("messages.wolfdisable", "Wolves are disabled."));
		config.set("messages.wolfdisable", messages.get("WOLF_DISABLE"));
		// Message sent when a player enables food
		messages.put("FOOD_ENABLE", config.getString("messages.foodenable", "Food is enabled."));
		config.set("messages.foodenable", messages.get("FOOD_ENABLE"));
		// Message sent when a player disables food
		messages.put("FOOD_DISABLE", config.getString("messages.fooddisable", "Food is disabled."));
		config.set("messages.fooddisable", messages.get("FOOD_DISABLE"));
		// Message sent when a player is blocked from using food
		messages.put("BLOCK_FOOD", config.getString("messages.blockfood", "Food is disabled in this duel!"));
		config.set("messages.blockfood", messages.get("BLOCK_FOOD"));
		// Message sent when a player trys to do something without permission
		messages.put("NO_PERMS", config.getString("messages.noperms", "You don't have permission to do that."));
		config.set("messages.noperms", messages.get("NO_PERMS"));
		// Message sent when a player trys to do something without permission
		messages.put("ALREADY_CONFIRMED", config.getString("messages.alreadyconfirmed", "You've already confirmed the settings in this duel."));
		config.set("messages.alreadyconfirmed", messages.get("ALREADY_CONFIRMED"));
		// Message sent when a player trys to do something without permission
		messages.put("NOT_CONFIG", config.getString("messages.notconfig", "Your opponent is not ready for that."));
		config.set("messages.notconfig", messages.get("NOT_CONFIG"));
		// Message sent when a player trys to do something in a disabled world
		messages.put("WORLD_DISABLED", config.getString("messages.worlddisabled", "You cannot duel in this world."));
		config.set("messages.worlddisabled", messages.get("WORLD_DISABLED"));
		saveConfig();
		// Set configuration values
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
