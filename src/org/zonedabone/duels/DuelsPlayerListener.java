package org.zonedabone.duels;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class DuelsPlayerListener implements Listener {
	
	public static Duels plugin;
	
	public DuelsPlayerListener(Duels instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (e.getItem() == null) {
			return;
		}
		Material type = e.getItem().getType();
		Duel duel = Duels.duels.get(e.getPlayer());
		if (duel != null && duel.targetstage == 2 && duel.starterstage == 2 && !duel.food) {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (type == Material.PORK || type == Material.GRILLED_PORK || type == Material.COOKED_FISH || type == Material.RAW_FISH || type == Material.COOKIE || type == Material.BREAD || type == Material.MUSHROOM_SOUP || type == Material.GOLDEN_APPLE || type == Material.APPLE || e.getClickedBlock().getType() == Material.CAKE_BLOCK) {
					e.getPlayer().sendMessage(Duels.getMessage("BLOCK_FOOD"));
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		Player player = e.getPlayer();
		Duel duel = Duels.duels.get(player);
		if (duel != null) {
			duel.disconnect(player);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().equals(e.getTo())) {
			return;
		}
		Player player = e.getPlayer();
		Duel duel = Duels.duels.get(player);
		if (duel != null) {
			duel.checkLocations(player, e);
		}
		Object[] duels = Duels.duels.values().toArray();
		for (Object duel2 : duels) {
			if (duel2 instanceof Duel) {
				((Duel) duel2).checkLocations(player, e);
			}
		}
		ItemStack[] items = Duels.itemStore.get(e.getPlayer());
		ItemStack[] armor = Duels.armorStore.get(e.getPlayer());
		Duels.itemStore.remove(e.getPlayer());
		Duels.armorStore.remove(e.getPlayer());
		if (items != null && armor != null) {
			e.getPlayer().getInventory().setContents(items);
			e.getPlayer().getInventory().setArmorContents(armor);
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		Duel duel = Duels.duels.get(player);
		if (duel != null) {
			duel.disconnect(player);
		}
		Object[] duels = Duels.duels.values().toArray();
		for (Object duel2 : duels) {
			if (duel2 instanceof Duel) {
				Duel tocancel = (Duel) duel2;
				if (tocancel.starter == player || tocancel.target == player) {
					tocancel.cancel();
				}
			}
		}
	}
}