package zonedabone.Duels;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
	
	public void onPlayerInteract(PlayerInteractEvent e){
		Material type = e.getItem().getType();
		Duel duel = Duels.duels.get(e.getPlayer());
		if(duel!=null&&duel.targetstage==2&&duel.starterstage==2&&!duel.food){
			if(e.getAction()==Action.RIGHT_CLICK_AIR||e.getAction()==Action.RIGHT_CLICK_BLOCK){
				if(type==Material.PORK||
						type==Material.GRILLED_PORK||
						type==Material.COOKED_FISH||
						type==Material.RAW_FISH||
						type==Material.COOKIE||
						type==Material.BREAD||
						type==Material.MUSHROOM_SOUP||
						type==Material.GOLDEN_APPLE||
						type==Material.APPLE||
						e.getClickedBlock().getType()==Material.CAKE_BLOCK){
					e.getPlayer().sendMessage("Food is disabled in this duel!");
					e.setCancelled(true);
				}
			}
		}
	}
}