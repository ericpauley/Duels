package zonedabone.Duels;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

public class DuelsEntityListener extends EntityListener {
	
	public static Duels plugin;
	
	public DuelsEntityListener(Duels instance) {
		plugin = instance;
	}

	public void onEntityDamage(EntityDamageEvent e){
		
		if(e instanceof EntityDamageByEntityEvent){
			EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) e;
			Entity entity1 = event.getDamager();
			Entity entity2 = event.getEntity();
			if(entity1 instanceof Player && entity2 instanceof Player){
				Player player1 = (Player) entity1;
				Player player2 = (Player) entity2;
				if(Duels.DISABLED_WORLDS.contains(player1.getWorld().getName())){return;}
				if(Duels.duels.get(player1)==Duels.duels.get(player2)
						&&Duels.duels.get(player1)!=null
						&&Duels.duels.get(player1).targetstage==2
						&&Duels.duels.get(player1).starterstage==2){
					if(Duels.FORCE_PVP){
						e.setCancelled(false);
					}
				}else{
					if(!Duels.NO_DUEL_PVP){
						e.setCancelled(true);
					}
				}
			}
			
		}
	}
	
	public void onEntityDeath(EntityDeathEvent e){
		if(e.getEntity() instanceof Player){
			Player player = (Player) e.getEntity();
			Duel duel = Duels.duels.get(player);
			if(duel!=null&&duel.starterstage==2&&duel.targetstage==2){
				e.getDrops().clear();
				boolean result = duel.lose(player, true);
				ItemStack[] items;
				ItemStack[] armor;
				if(result){
					items = player.getInventory().getContents();
					armor = player.getInventory().getArmorContents();
					Duels.itemStore.put(player, items);
					Duels.armorStore.put(player, armor);
				}
				player.getInventory().clear();
			}
		}
	}
	
	public void onEntityTarget(EntityTargetEvent e){
		Entity entity = e.getEntity();
		if(entity instanceof Tameable){
			Player owner = (Player)(((Tameable)entity).getOwner());
			Duel duel = Duels.duels.get(owner);
			if(duel!=null && !duel.wolves){
				e.setCancelled(true);
			}
		}
	}

}