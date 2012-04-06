package org.zonedabone.duels;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

public class DuelsEntityListener implements Listener {
	
	public static Duels plugin;
	
	public DuelsEntityListener(Duels instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		Entity damaged = e.getEntity();
		if (damaged instanceof Player) {
			Player pDamaged = (Player) damaged;
			Player pDamager;
			if (damager instanceof Player) {
				pDamager = (Player) damager;
			} else if (damager instanceof Projectile) {
				Projectile p = (Projectile) damager;
				if (p.getShooter() instanceof Player) {
					pDamager = (Player) p.getShooter();
				} else {
					return;
				}
			} else {
				return;
			}
			if (Duels.DISABLED_WORLDS.contains(pDamager.getWorld().getName())) {
				return;
			}
			if (Duels.duels.get(pDamager) == Duels.duels.get(pDamaged) && Duels.duels.get(pDamager) != null && Duels.duels.get(pDamager).targetstage == 2 && Duels.duels.get(pDamager).starterstage == 2) {
				if (Duels.FORCE_PVP) {
					e.setCancelled(false);
				}
			} else {
				if (!Duels.NO_DUEL_PVP) {
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof Player) {
			Player player = (Player) e.getEntity();
			Duel duel = Duels.duels.get(player);
			if (duel != null && duel.starterstage == 2 && duel.targetstage == 2) {
				e.getDrops().clear();
				boolean result = duel.lose(player, true);
				ItemStack[] items;
				ItemStack[] armor;
				if (result) {
					items = player.getInventory().getContents();
					armor = player.getInventory().getArmorContents();
					Duels.itemStore.put(player, items);
					Duels.armorStore.put(player, armor);
				}
				player.getInventory().clear();
			}
		}
	}
	
	public void onEntityTarget(EntityTargetEvent e) {
		Entity entity = e.getEntity();
		if (entity instanceof Tameable) {
			Player owner = (Player) ((Tameable) entity).getOwner();
			Duel duel = Duels.duels.get(owner);
			if (duel != null && !duel.wolves) {
				e.setCancelled(true);
			}
		}
	}
}