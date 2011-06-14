package zonedabone.Duels;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
		Duel duel = Duels.duels.get(player);
		if(duel!=null){
			duel.checkLocations();
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

}