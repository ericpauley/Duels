package org.zonedabone.duels;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Duel {
	
	boolean food = ConfigManager.WOLVES;
	public boolean keepItems = ConfigManager.KEEP_ITEMS;
	public Player starter;
	public int starterstage;
	int starterStake = ConfigManager.STAKE;
	public Player target;
	public int targetstage;
	int targetStake =ConfigManager.STAKE;
	boolean wolves = ConfigManager.WOLVES;
	
	public Duel(Player starter, Player target) {
		this.starter = starter;
		this.target = target;
		starterstage = 1;
		targetstage = 0;
	}
	
	public void accept() {
		targetstage = 1;
	}
	
	public void cancel() {
		if (targetstage != 0) {
			MessageManager.sendMessage(target,"success.duel_cancel");
			Duels.duels.remove(target);
		}
		MessageManager.sendMessage(starter,"success.duel_cancel");
		if (Duels.economy != null && targetStake != 0 && starterStake != 0) {
			Duels.economy.depositPlayer(starter.getName(), starterStake);
			Duels.economy.depositPlayer(target.getName(), targetStake);
		}
		Duels.duels.remove(starter);
	}
	
	public void checkLocations(Player mover, PlayerMoveEvent e) {
		if (mover != starter && mover != target) {
			return;
		}
		Location newLoc = e.getTo();
		Location otherLoc;
		if (mover == starter) {
			otherLoc = target.getLocation();
		} else {
			otherLoc = starter.getLocation();
		}
		if (newLoc == null || otherLoc == null || newLoc.getWorld() != otherLoc.getWorld() || otherLoc.distance(newLoc) > ConfigManager.MAX_DISTANCE) {
			if (starterstage == 1 && targetstage == 1) {
				if (ConfigManager.FORCE_FIELD_BEFORE) {
					e.setCancelled(true);
				} else {
					cancel();
				}
			} else if (starterstage == 2 && targetstage == 2) {
				if (ConfigManager.FORCE_FIELD_DURING) {
					e.setCancelled(true);
				} else {
					lose(mover, false);
				}
			} else {
				cancel();
			}
		}
	}
	
	public void confirm(Player player) {
		if (player == starter) {
			starterstage = 2;
			if (targetstage == 1) {
				MessageManager.sendMessage(target, "success.player_ready", "p", starter.getDisplayName());
			} else {
				MessageManager.sendMessage(target, "success.duel_start");
				MessageManager.sendMessage(starter, "success.duel_start");
			}
		} else {
			targetstage = 2;
			if (starterstage == 1) {
				MessageManager.sendMessage(starter,"success.player_ready", "p", target.getDisplayName());
			} else {
				MessageManager.sendMessage(target,"success.duel_start");
				MessageManager.sendMessage(target, "success.duel_start");
			}
		}
	}
	
	public void disconnect(Player player) {
		if (starterstage < 2 && targetstage < 2) {
			cancel();
		} else {
			lose(player, false);
		}
	}
	
	public boolean lose(Player player, boolean died) {
		Player loser = player;
		Player winner;
		if (starter == player) {
			winner = target;
		} else {
			winner = starter;
		}
		MessageManager.sendMessage(loser,"success.duel_lose");
		MessageManager.sendMessage(winner,"success.duel_win");
		// Set highscores
		String winnerName = winner.getName();
		String loserName = loser.getName();
		double winnerRating = Duels.highscores.getDouble(winnerName + ".rating", ConfigManager.STARTING_RATING);
		double loserRating = Duels.highscores.getDouble(loserName + ".rating", ConfigManager.STARTING_RATING);
		double winnerChance = 1 / (1 + Math.pow(10, (loserRating - winnerRating) / 400));
		double outcome;
		if (died && ConfigManager.RANKING_WEIGHT != 0) {
			outcome = (double) winner.getHealth() / 20;
			outcome = Math.pow(outcome, 1 / ConfigManager.RANKING_WEIGHT);
		} else {
			outcome = 1;
		}
		double change = ConfigManager.RANKING_MAGNITUDE * (outcome - winnerChance);
		Duels.highscores.set(winnerName + ".rating", winnerRating + change);
		Duels.highscores.set(loserName + ".rating", loserRating - change);
		Duels.highscores.set(winnerName + ".duels", Duels.highscores.getInt(winnerName + ".duels", 0) + 1);
		Duels.highscores.set(loserName + ".duels", Duels.highscores.getInt(loserName + ".duels", 0) + 1);
		Duels.highscores.set(winnerName + ".wins", Duels.highscores.getInt(winnerName + ".wins", 0) + 1);
		Duels.highscores.set(loserName + ".losses", Duels.highscores.getInt(loserName + ".losses", 0) + 1);
		// Set Highscores
		if (Duels.economy != null) {
			Duels.economy.depositPlayer(winner.getName(), starterStake + targetStake);
		}
		Duels.duels.remove(winner);
		Duels.duels.remove(loser);
		if (!keepItems) {
			Inventory loserInv = loser.getInventory();
			Inventory winnerInv = winner.getInventory();
			ItemStack[] transfer = loserInv.getContents();
			loserInv.clear();
			for (ItemStack element : transfer) {
				if (element != null) {
					HashMap<Integer, ItemStack> left = winnerInv.addItem(element);
					if (!left.isEmpty()) {
						ItemStack[] drop = (ItemStack[]) left.values().toArray();
						winner.getWorld().dropItemNaturally(winner.getLocation(), drop[0]);
					}
				}
			}
			return false;
		}
		return true;
	}
	
	public void setFood(Player player, boolean value) {
		if (value != food) {
			food = value;
			if (food) {
				MessageManager.sendMessage(starter,"config.food_enable");
				MessageManager.sendMessage(target,"config.food_enable");
			} else {
				MessageManager.sendMessage(starter,"config.food_disable");
				MessageManager.sendMessage(target,"config.food_disable");
			}
		}
	}
	
	public void setKeepItems(Player player, boolean value) {
		if (value == keepItems) {
			return;
		}
		keepItems = value;
		if (value) {
			MessageManager.sendMessage(starter,"config.set_keep_items");
			MessageManager.sendMessage(target,"config.set_keep_items");
		} else {
			MessageManager.sendMessage(starter,"config.set_lose_items");
			MessageManager.sendMessage(target,"config.set_lose_items");
		}
	}
	
	public void setStake(Player player, int newStake) {
		String message = MessageManager.parseMessage("success.player_ready", "p", player.getDisplayName(), "s", Duels.economy.format(newStake));
		if (player == starter) {
			int change = newStake - starterStake;
			if (Duels.economy.has(player.getName(), change)) {
				Duels.economy.withdrawPlayer(player.getName(), change);
				starterStake = newStake;
				starter.sendMessage(message);
				target.sendMessage(message);
			} else {
				MessageManager.sendMessage(player,"failure.block_set_stake");
			}
		} else {
			int change = newStake - targetStake;
			if (Duels.economy.has(player.getName(), change)) {
				Duels.economy.withdrawPlayer(player.getName(), change);
				targetStake = newStake;
				starter.sendMessage(message);
				target.sendMessage(message);
			} else {
				MessageManager.sendMessage(player,"failure.block_set_stake");
			}
		}
	}
	
	public void setWolves(Player player, boolean value) {
		if (value != wolves) {
			wolves = value;
			if (wolves) {
				MessageManager.sendMessage(starter,"config.wolf_enable");
				MessageManager.sendMessage(target,"config.wolf_enable");
			} else {
				MessageManager.sendMessage(starter,"config.wolf_disable");
				MessageManager.sendMessage(target,"config.wolf_disable");
			}
		}
	}
}