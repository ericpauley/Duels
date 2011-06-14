package zonedabone.Duels;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/* Duels Template
 * By Adamki11s
 * HUGE Plugin Tutorial
 */

public class DuelsPlayerListener extends PlayerListener {
	
	public static Duels plugin;
	
	public DuelsPlayerListener(Duels instance) {
		plugin = instance;
	}

	public void onPlayerMove(PlayerMoveEvent e){
		Player player = e.getPlayer();
		Object[] duels = Duels.duels.values().toArray();
		for(int i = 0;i<duels.length;i++){
			if(duels[i] instanceof Duel){
				((Duel)duels[i]).checkLocations(player);
			}
		}
		ItemStack[] items = Duels.itemStore.get(e.getPlayer());
		ItemStack[] armor = Duels.itemStore.get(e.getPlayer());
		Duels.itemStore.remove(e.getPlayer());
		if(items!=null||armor!=null){
			e.getPlayer().getInventory().setContents(items);
			e.getPlayer().getInventory().setArmorContents(armor);
		}
	}
	
	public void onPlayerKick(PlayerKickEvent e){
		Player player = e.getPlayer();
		Duel duel = Duels.duels.get(player);
		if(duel!=null){
			duel.disconnect(player);
		}
	}
	
	public void onPlayerQuit(PlayerQuitEvent e){
		Player player = e.getPlayer();
		Duel duel = Duels.duels.get(player);
		if(duel!=null){
			duel.disconnect(player);
		}
	}
	
	public void onPlayerRespawn(final PlayerRespawnEvent e){
		final ItemStack[] restore = Duels.itemStore.get(e.getPlayer());
		Duels.itemStore.remove(e.getPlayer());
		if(restore!=null){
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			    public void run() {
			    	for(int i =0;i<restore.length;i++){
						e.getPlayer().getInventory().addItem(restore[i]);
					}
			    }
			}, 60L);
		}
	}

}