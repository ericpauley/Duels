package zonedabone.Duels;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

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
				if(Duels.duels.get(player1)==Duels.duels.get(player2)
						&&Duels.duels.get(player1)!=null
						&&Duels.duels.get(player1).targetstage==2
						&&Duels.duels.get(player1).starterstage==2){
					String message = player1.getDisplayName() + " hits a mighty blow to " + player2.getDisplayName() + "!";
					player1.sendMessage(message);
					player2.sendMessage(message);
				}
				else{
					e.setCancelled(true);
				}
			}
			
		}
	}
	
	public void onEntityDeath(EntityDeathEvent e){
		if(e.getEntity() instanceof Player){
			Player player = (Player) e.getEntity();
			Duel duel = Duels.duels.get(player);
			if(duel!=null){
				duel.lose(player);
			}
		}
	}

}