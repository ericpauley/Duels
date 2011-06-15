package zonedabone.Duels;

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
import org.bukkit.util.config.Configuration;

import com.iConomy.*;

public class Duels extends JavaPlugin {
	public iConomy iConomy = null;
    //ClassListeners
	private final DuelsEntityListener entityListener = new DuelsEntityListener(this);
	private final DuelsPlayerListener playerListener = new DuelsPlayerListener(this);
	private final DuelsServerListener serverListener = new DuelsServerListener(this);
    //ClassListeners
	
	//Data storage via HashMaps
	public static Map<Player,Duel> duels = new HashMap<Player,Duel>();
	public static Map<Player,ItemStack[]> itemStore = new HashMap<Player,ItemStack[]>();
	public static Map<Player,ItemStack[]> armorStore = new HashMap<Player,ItemStack[]>();
	////Data storage via HashMaps
	
	Logger log = Logger.getLogger("Minecraft");
	
	//Configuration memory storage
	public static int MAX_DISTANCE = 20;
	public static boolean FORCE_PVP = true;
	public static boolean USE_ICONOMY = true;
	public static boolean FORCE_FIELD_DURING = true;
	public static boolean FORCE_FIELD_BEFORE = true;
	public static Map<String,String> messages = new HashMap<String,String>();
	//Configuration memory storage

	public void onDisable() {
		PluginDescriptionFile pdf = this.getDescription();
    	log.info(pdf.getName() + " version " + pdf.getVersion() + " DISABLED");
	}

	public void onEnable() {
		PluginDescriptionFile pdf = this.getDescription();
        PluginManager pm = this.getServer().getPluginManager();
        
        //Set configuration values
        Configuration config = getConfiguration();
        //Max distance between players.
    	MAX_DISTANCE = config.getInt("maxdistance", 20);
    	config.setProperty("maxdistance",MAX_DISTANCE);
    	//Max distance between players during the duel. (Instead of surrender)
    	FORCE_FIELD_DURING = config.getBoolean("forcefeild.during", true);
    	config.setProperty("forcefeild.during",FORCE_FIELD_DURING);
    	//Max distance between players while preparing the duel. (Instead of cancel)
    	FORCE_FIELD_BEFORE = config.getBoolean("forcefeild.before", true);
    	config.setProperty("forcefeild.before",FORCE_FIELD_BEFORE);
    	//Whether or not to override other pvp plugins during duels
    	FORCE_PVP = config.getBoolean("forcepvp", true);
    	config.setProperty("forcepvp",FORCE_PVP);
    	//Whether or not to use iConomy
    	USE_ICONOMY = config.getBoolean("useiconomy", true);
    	config.setProperty("useiconomy",USE_ICONOMY);
    	//Message if sent from console
    	messages.put("CLIENT_ONLY", config.getString("messages.clientonly", "Duels can only be used from the client."));
    	config.setProperty("messages.clientonly", messages.get("CLIENT_ONLY"));
    	//Message if already in a duel
    	messages.put("ALREADY_DUELING", config.getString("messages.alreadydueling", "You are currently in a duel!"));
    	config.setProperty("messages.alreadydueling", messages.get("ALREADY_DUELING"));
    	//Message if not in a duel
    	messages.put("NOT_DUELING", config.getString("messages.notdueling", "You're not in a duel!"));
    	config.setProperty("messages.notdueling", messages.get("NOT_DUELING"));
    	//Message if player tries to duel self
    	messages.put("CANT_DUEL_SELF", config.getString("messages.cantduelself", "You can't duel yourself!"));
    	config.setProperty("messages.cantduelself", messages.get("CANT_DUEL_SELF"));
    	//Message if target is offline
    	messages.put("PLAYER_OFFLINE", config.getString("messages.playeroffline", "{PLAYER} is offline."));
    	config.setProperty("messages.playeroffline", messages.get("PLAYER_OFFLINE"));
    	//Message if target is not within range
    	messages.put("NOT_IN_RANGE", config.getString("messages.notinrange", "{PLAYER} is not in range. ({RANGE} blocks)"));
    	config.setProperty("messages.notinrange", messages.get("NOT_IN_RANGE"));
    	//Message when player accepts an incoming duel request.
    	messages.put("SELF_ACCEPT", config.getString("messages.selfaccept", "Accepted {PLAYER}'s duel."));
    	config.setProperty("messages.selfaccept", messages.get("SELF_ACCEPT"));
    	//Message when target accepts the duel request
    	messages.put("OTHER_ACCEPT", config.getString("messages.otheraccept", "{PLAYER} has accepted your duel request."));
    	config.setProperty("messages.otheraccept", messages.get("OTHER_ACCEPT"));
    	//Message sent to both players when config mode is entered
    	messages.put("CONFIG", config.getString("messages.config", "set duel options with /duel set <option> <on/off>"));
    	config.setProperty("messages.config", messages.get("CONFIG"));
    	config.save();
        //Set configuration values
        
        
        //Register Events
        pm.registerEvent(Event.Type.ENTITY_DAMAGE,  entityListener, Event.Priority.Highest,  this);
        pm.registerEvent(Event.Type.PLAYER_MOVE,    playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_KICK,    playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT,    playerListener, Event.Priority.Monitor, this);
        if(USE_ICONOMY){
	        pm.registerEvent(Event.Type.PLUGIN_ENABLE,  serverListener, Event.Priority.Monitor, this);
	        pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Event.Priority.Monitor, this);
        }
        //Register Events
        
    	log.info(pdf.getName() + " version " + pdf.getVersion() + " ENABLED");
	}
	
	public static String getMessage(String msg){
		return MessageParser.parseMessage(messages.get(msg));
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
		if(sender instanceof ConsoleCommandSender){
			sender.sendMessage(getMessage("CLIENT_ONLY"));
			return true;
		}
		if(!command.getName().equalsIgnoreCase("duel")){return false;}
		if (args.length==0){return false;}
		Player player = (Player) sender;
		String subcommand = args[0];
		if(subcommand.equalsIgnoreCase("challenge")&&args.length==2){
			Player target = player.getServer().getPlayer(args[1]);
			if(duels.get(player)!=null){
				sender.sendMessage(getMessage("ALREADY_DUELING"));
			}else if(player==target){
				sender.sendMessage(getMessage("CANT_DUEL_SELF"));
			}else if(target==null||!target.isOnline()){
				sender.sendMessage(MessageParser.parseMessage(messages.get("PLAYER_OFFLINE"),"{PLAYER}",args[1]));
			}else if(player.getLocation().distance(target.getLocation())>MAX_DISTANCE){
				player.sendMessage(MessageParser.parseMessage(messages.get("NOT_IN_RANGE"),"{PLAYER}",target.getDisplayName(),"{RANGE}", Integer.toString(MAX_DISTANCE)));
			}else if(duels.get(target)!=null && duels.get(target).target == player){
				duels.put(player, duels.get(target));
				duels.get(target).accept();
				player.sendMessage(MessageParser.parseMessage(messages.get("SELF_ACCEPT"),"{PLAYER}",target.getDisplayName()));
				target.sendMessage(MessageParser.parseMessage(messages.get("OTHER_ACCEPT"),"{PLAYER}",target.getDisplayName()));
				player.sendMessage(getMessage("CONFIG"));
				target.sendMessage(getMessage("CONFIG"));
			}else{
				duels.put(player, new Duel(player, target, iConomy));
				player.sendMessage("Duel request sent to " + target.getDisplayName() + ".");
				target.sendMessage(player.getDisplayName() + " has requested to duel with you.");
			}
			return true;
		}else if(subcommand.equalsIgnoreCase("confirm")&&args.length==1){
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
			return true;
		}else if(subcommand.equalsIgnoreCase("cancel")&&args.length==1){
			Duel duel = duels.get(player);
			if (duel == null){
				player.sendMessage(getMessage("NOT_DUELING"));
			}else if(duel.targetstage == 2 && duel.starterstage == 2){
				player.sendMessage("You can't cancel a duel in progress! Use '/duel surrender' instead.");
			}else{
				duel.cancel();
			}
			return true;
		}else if(subcommand.equalsIgnoreCase("surrender")&&args.length==1){
			Duel duel = duels.get(player);
			if (duel==null){
				player.sendMessage(getMessage("NOT_DUELING"));
			}else if(duel.starterstage!=2||duel.targetstage!=2){
				player.sendMessage("The duel has not started yet. Use '/duel cancel' instead.");
			}else{
				duel.lose(player);
			}
			return true;
		}else if(subcommand.equalsIgnoreCase("set")&&args.length==3){
			Duel duel = duels.get(player);
			if (duel==null){
				player.sendMessage(getMessage("NOT_DUELING"));
			}else if(duel.starterstage!=1||duel.targetstage!=1){
				player.sendMessage("Now is not the time to change duel settings.");
			}else{
				String key = args[1];
				String value = args[2];
				if(key.equalsIgnoreCase("keepitems")){
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
				}else if(key.equalsIgnoreCase("wolves")){
					if(value=="on"||value=="true"){
						duel.setWolves(player, true);
					}else if(value=="off"||value=="false"){
						duel.setWolves(player, false);
					}
				}else if(key.equalsIgnoreCase("food")){
					if(value=="on"||value=="true"){
						duel.setFood(player, true);
					}else if(value=="off"||value=="false"){
						duel.setFood(player, false);
					}
				}
			}
			return true;
		}
		return false;
	}
	
}


