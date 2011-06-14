package zonedabone.Duels; //Your package

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.PluginDescriptionFile;
import com.iConomy.*;

public class Duels extends JavaPlugin {
	public iConomy iConomy = null;
    //ClassListeners
	private final DuelsEntityListener entityListener = new DuelsEntityListener(this);
	private final DuelsPlayerListener playerListener = new DuelsPlayerListener(this);
	private final DuelsServerListener serverListener = new DuelsServerListener(this);
	public static Map<Player,Duel> duels = new HashMap<Player,Duel>();
	public static Map<Player,ItemStack[]> itemStore = new HashMap<Player,ItemStack[]>();
    //ClassListeners
	
	Logger log = Logger.getLogger("Minecraft");//Define your logger


	public void onDisable() {
        PluginDescriptionFile pdfFile = this.getDescription();
    	log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " DISABLED");

	}

	public void onEnable() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Event.Priority.Monitor, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Event.Priority.Monitor, this);
        PluginDescriptionFile pdfFile = this.getDescription();
    	log.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " ENABLED");
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(sender instanceof ConsoleCommandSender){
			sender.sendMessage("Duels can only be used from the client.");
			return true;
		}
		if(!command.getName().equalsIgnoreCase("duel")){return false;}
		if (args.length==0){return false;}
		Player player = (Player) sender;
		String subcommand = args[0];
		if(subcommand.equalsIgnoreCase("challenge")){
			Player target;
			if(args.length<2){return false;}
			target = player.getServer().getPlayer(args[1]);
			if(duels.get(player)!=null){
				sender.sendMessage("You are currently in a duel!");
			}else if(player==target){
				sender.sendMessage("You can't duel yourself!");
			}else if(target==null||!target.isOnline()){
				sender.sendMessage(args[1] + "is offline.");
			}else if(player.getLocation().distance(target.getLocation())>10){
				player.sendMessage(target.getDisplayName() + "is not in range. (10 blocks)");
			}else if(duels.get(target)!=null && duels.get(target).target == player){
				duels.put(player, duels.get(target));
				duels.get(target).accept();
				sender.sendMessage("Accepted " + target.getDisplayName() + "'s duel.");
				player.sendMessage(player.getDisplayName() + " has accepted your duel request.");
				player.sendMessage("set duel options with /duel set <option> <on/off>");
				target.sendMessage("set duel options with /duel set <option> <on/off>");
			}else{
				duels.put(player, new Duel(player, target, iConomy));
				player.sendMessage("Duel request sent to " + target.getDisplayName() + ".");
				target.sendMessage(player.getDisplayName() + " has requested to duel with you.");
			}
			return true;
		}else if(subcommand.equalsIgnoreCase("confirm")){
			Duel duel = duels.get(player);
			if (duel.starter == player){
				if(duel!=null && duel.targetstage>=1 && duel.starterstage==1){
					duel.confirm(player);
				}
			}else{
				if(duel!=null && duel.starterstage>=1 && duel.targetstage==1){
					duel.confirm(player);
				}
			}
		}else if(subcommand.equalsIgnoreCase("cancel")){
			Duel duel = duels.get(player);
			if (duel == null){
				player.sendMessage("You're not in a duel!");
			}else if(duel.targetstage == 2 && duel.starterstage == 2){
				player.sendMessage("You can't cancel a duel in progress! Use '/duel surrender' instead.");
			}else{
				duel.cancel();
			}
		}else if(subcommand.equalsIgnoreCase("surrender")){
			Duel duel = duels.get(player);
			if (duel==null){
				player.sendMessage("You're not in a duel!");
			}else if(duel.starterstage!=2||duel.targetstage!=2){
				player.sendMessage("The duel has not started yet. Use '/duel cancel' instead.");
			}else{
				duel.lose(player);
			}
		}else if(subcommand.equalsIgnoreCase("set")){
			if(args.length<4){return false;}
			Duel duel = duels.get(player);
			if (duel==null){
				player.sendMessage("You're not in a duel!");
			}else if(duel.starterstage!=1||duel.targetstage!=1){
				player.sendMessage("Now is not the time to change duel settings.");
			}else{
				String key = args[2];
				String value = args[3];
				if(key=="keepitems"){
					if(value=="on"||value=="true"){
						duel.setKeepItems(player, true);
					}else if(value=="off"||value=="false"){
						duel.setKeepItems(player, false);
					}
				}else if(key.equalsIgnoreCase("stake")){
					if(this.iConomy!=null){
						int newStake = Integer.parseInt(value);
						duel.setStake(player, newStake);
					}
				}else if(key=="wolves"){
					if(value=="on"||value=="true"){
						duel.setWolves(player, true);
					}else if(value=="off"||value=="false"){
						duel.setWolves(player, false);
					}
				}
			}
		}
		return true;
	}
	
}


