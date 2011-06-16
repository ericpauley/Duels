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
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;
import org.bukkit.plugin.Plugin;

import com.iConomy.*;

public class Duels extends JavaPlugin {
	public iConomy iConomy = null;
	public static PermissionHandler permissionHandler = null;
	
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
	public static boolean USE_PERMISSIONS = true;
	public static boolean FORCE_FIELD_DURING = true;
	public static boolean FORCE_FIELD_BEFORE = true;
	public static String MESSAGE_PREFIX = "&4[DUELS]&f";
	public static Map<String,String> messages = new HashMap<String,String>();
	//Configuration memory storage
	
	//Default duel settings
	public static int STAKE = 0;
	public static boolean WOLVES = true;
	public static boolean FOOD = true;
	public static boolean KEEP_ITEMS = true;
	//Default duel settings

	public boolean _getPerm(Player player, String node){
		if(permissionHandler!=null){
			return permissionHandler.has(player, node);
		}else{
			if(node.startsWith("duels.admin")){
				return player.isOp();
			}else{
				return true;
			}
		}
	}
	
	public boolean getPerm(Player player, String node){
		boolean canGive = _getPerm(player, node);
		if(!canGive){
			player.sendMessage(getMessage("NO_PERMS"));
		}
		return canGive;
	}
	
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
    	FORCE_FIELD_DURING = false;
    	config.setProperty("forcefeild.during",FORCE_FIELD_DURING);
    	//Max distance between players while preparing the duel. (Instead of cancel)
    	FORCE_FIELD_BEFORE = config.getBoolean("forcefeild.before", true);
    	FORCE_FIELD_BEFORE = false;
    	config.setProperty("forcefeild.before",FORCE_FIELD_BEFORE);
    	//Whether or not to override other pvp plugins during duels
    	FORCE_PVP = config.getBoolean("forcepvp", true);
    	config.setProperty("forcepvp",FORCE_PVP);
    	//Whether or not to use iConomy
    	USE_ICONOMY = config.getBoolean("useiconomy", true);
    	config.setProperty("useiconomy",USE_ICONOMY);
    	//Whether or not to use permissions
    	USE_PERMISSIONS = config.getBoolean("usepermissions", true);
    	config.setProperty("usepermissions",USE_PERMISSIONS);
    	//The prefix that goes in front of all messages
    	MESSAGE_PREFIX = config.getString("messageprefix", "&4[DUELS]&f");
    	config.setProperty("messageprefix",MESSAGE_PREFIX);
    	
    	//The default stake
    	STAKE = config.getInt("defaults.stake", 0);
    	config.setProperty("defaults.stake",STAKE);
    	//The default wolves setting
    	WOLVES = config.getBoolean("defaults.wolves", true);
    	config.setProperty("defaults.wolves",WOLVES);
    	//The default food setting
    	FOOD = config.getBoolean("defaults.food", true);
    	config.setProperty("defaults.food",FOOD);
    	//The default keepitems setting
    	KEEP_ITEMS = config.getBoolean("defaults.keepitems", true);
    	config.setProperty("defaults.keepitems",KEEP_ITEMS);
    	
    	
    	
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
    	//Message sent to starter on /duel challenge <player>
    	messages.put("SELF_REQUEST", config.getString("messages.selfrequest", "Duel request sent to {PLAYER}."));
    	config.setProperty("messages.selfrequest", messages.get("SELF_REQUEST"));
    	//Message sent to target on /duel challenge <player>
    	messages.put("OTHER_REQUEST", config.getString("messages.otherrequest", "{PLAYER} has requested to duel with you."));
    	config.setProperty("messages.otherrequest", messages.get("OTHER_REQUEST"));
    	//Message sent when a player tries to cancel a duel in progress
    	messages.put("CANCEL_STARTED", config.getString("messages.cancelstarted", "You can't cancel a duel in progress! Use '/duel surrender' instead."));
    	config.setProperty("messages.cancelstarted", messages.get("CANCEL_STARTED"));
    	//Message sent when a player surrenders a duel that hasn't started
    	messages.put("SURRENDER_NOT_STARTED", config.getString("messages.surrendernotstarted", "The duel has not started yet. Use '/duel cancel' instead."));
    	config.setProperty("messages.surrendernotstarted", messages.get("SURRENDER_NOT_STARTED"));
    	//Message sent when a player tries to configure a duel at the wrong time
    	messages.put("BLOCK_CONFIG", config.getString("messages.blockconfig", "Now is not the time to change duel settings."));
    	config.setProperty("messages.blockconfig", messages.get("BLOCK_CONFIG"));
    	//Message sent when a player is ready to start
    	messages.put("PLAYER_READY", config.getString("messages.playerready", "{PLAYER} is ready to start the duel."));
    	config.setProperty("messages.playerready", messages.get("PLAYER_READY"));
    	//Message sent when a player is ready to start
    	messages.put("DUEL_START", config.getString("messages.duelstart", "FIGHT TO THE DEATH!"));
    	config.setProperty("messages.duelstart", messages.get("DUEL_START"));
    	//Message sent when a player is ready to start
    	messages.put("DUEL_CANCEL", config.getString("messages.duelcancel", "The duel has been canceled."));
    	config.setProperty("messages.duelcancel", messages.get("DUEL_CANCEL"));
    	//Message sent when a player is ready to start
    	messages.put("DUEL_LOSE", config.getString("messages.duellose", "You lost the duel!"));
    	config.setProperty("messages.duellose", messages.get("DUEL_LOSE"));
    	//Message sent when a player is ready to start
    	messages.put("DUEL_WIN", config.getString("messages.duelwin", "You won the duel!"));
    	config.setProperty("messages.duelwin", messages.get("DUEL_WIN"));
    	//Message sent when a player is ready to start
    	messages.put("SET_KEEP_ITEMS", config.getString("messages.setkeepitems", "You will keep you items if you die."));
    	config.setProperty("messages.setkeepitems", messages.get("SET_KEEP_ITEMS"));
    	//Message sent when a player is ready to start
    	messages.put("SET_LOSE_ITEMS", config.getString("messages.setloseitems", "Your opponent will get your items if you die."));
    	config.setProperty("messages.setloseitems", messages.get("SET_LOSE_ITEMS"));
    	//Message sent when a player sets a new stake
    	messages.put("SET_STAKE", config.getString("messages.setstake", "{PLAYER} set their stake to {STAKE}."));
    	config.setProperty("messages.setstake", messages.get("SET_STAKE"));
    	//Message sent when a player can't afford the stake
    	messages.put("BLOCK_SET_STAKE", config.getString("messages.blocksetstake", "You can't afford to set your stake to that."));
    	config.setProperty("messages.blocksetstake", messages.get("BLOCK_SET_STAKE"));
    	//Message sent when a player enables wolves
    	messages.put("WOLF_ENABLE", config.getString("messages.wolfenable", "Wolves are enabled."));
    	config.setProperty("messages.wolfenable", messages.get("WOLF_ENABLE"));
    	//Message sent when a player disables wolves
    	messages.put("WOLF_DISABLE", config.getString("messages.wolfdisable", "Wolves are disabled."));
    	config.setProperty("messages.wolfdisable", messages.get("WOLF_DISABLE"));
    	//Message sent when a player enables food
    	messages.put("FOOD_ENABLE", config.getString("messages.foodenable", "Food is enabled."));
    	config.setProperty("messages.foodenable", messages.get("FOOD_ENABLE"));
    	//Message sent when a player disables food
    	messages.put("FOOD_DISABLE", config.getString("messages.fooddisable", "Food is disabled."));
    	config.setProperty("messages.fooddisable", messages.get("FOOD_DISABLE"));
    	//Message sent when a player is blocked from using food
    	messages.put("BLOCK_FOOD", config.getString("messages.blockfood", "Food is disabled in this duel!"));
    	config.setProperty("messages.blockfood", messages.get("BLOCK_FOOD"));
    	//Message sent when a player trys to do something without permission
    	messages.put("NO_PERMS", config.getString("messages.noperms", "You don't have permission to do that."));
    	config.setProperty("messages.noperms", messages.get("NO_PERMS"));
    	config.save();
        //Set configuration values
        
        
        //Register Events
        pm.registerEvent(Event.Type.ENTITY_DAMAGE,  entityListener, Event.Priority.Highest,  this);
        pm.registerEvent(Event.Type.ENTITY_DEATH,  entityListener, Event.Priority.Highest,  this);
        pm.registerEvent(Event.Type.PLAYER_MOVE,    playerListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_KICK,    playerListener, Event.Priority.Monitor, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT,    playerListener, Event.Priority.Monitor, this);
        if(USE_ICONOMY){
	        pm.registerEvent(Event.Type.PLUGIN_ENABLE,  serverListener, Event.Priority.Monitor, this);
	        pm.registerEvent(Event.Type.PLUGIN_DISABLE, serverListener, Event.Priority.Monitor, this);
        }
        //Register Events
        if(USE_PERMISSIONS){
        	setupPermissions();
        }
        
        if(permissionHandler==null){
        	log.info(pdf.getName() + " version " + pdf.getVersion() + " ENABLED (OP mode)");
        }else{
        	log.info(pdf.getName() + " version " + pdf.getVersion() + " ENABLED (Using permissions)");
        }
    	
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
			if(!getPerm(player, "duels.user.challenge")){return true;}
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
				target.sendMessage(MessageParser.parseMessage(messages.get("OTHER_ACCEPT"),"{PLAYER}",player.getDisplayName()));
				player.sendMessage(getMessage("CONFIG"));
				target.sendMessage(getMessage("CONFIG"));
			}else{
				duels.put(player, new Duel(player, target, iConomy));
				player.sendMessage(MessageParser.parseMessage(messages.get("SELF_REQUEST"),"{PLAYER}",target.getDisplayName()));
				target.sendMessage(MessageParser.parseMessage(messages.get("OTHER_REQUEST"),"{PLAYER}",player.getDisplayName()));
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
				player.sendMessage(getMessage("CANCEL_STARTED"));
			}else{
				duel.cancel();
			}
			return true;
		}else if(subcommand.equalsIgnoreCase("surrender")&&args.length==1){
			Duel duel = duels.get(player);
			if (duel==null){
				player.sendMessage(getMessage("NOT_DUELING"));
			}else if(duel.starterstage!=2||duel.targetstage!=2){
				player.sendMessage(getMessage("SURRENDER_NOT_STARTED"));
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
					if(!getPerm(player, "duels.user.set.keepitems")){return true;}
					if(value.equalsIgnoreCase("on")||value.equalsIgnoreCase("true")){
						duel.setKeepItems(player, true);
					}else if(value.equalsIgnoreCase("off")||value.equalsIgnoreCase("false")){
						duel.setKeepItems(player, false);
					}
				}else if(key.equalsIgnoreCase("stake")){
					if(!getPerm(player, "duels.user.set.stake")){return true;}
					if(this.iConomy!=null){
						int newStake = Integer.parseInt(value);
						duel.setStake(player, newStake);
					}
				}else if(key.equalsIgnoreCase("wolves")){
					if(!getPerm(player, "duels.user.set.wolves")){return true;}
					if(value.equalsIgnoreCase("on")||value.equalsIgnoreCase("true")){
						duel.setWolves(player, true);
					}else if(value.equalsIgnoreCase("off")||value.equalsIgnoreCase("false")){
						duel.setWolves(player, false);
					}
				}else if(key.equalsIgnoreCase("food")){
					if(!getPerm(player, "duels.user.set.food")){return true;}
					if(value.equalsIgnoreCase("on")||value.equalsIgnoreCase("true")){
						duel.setFood(player, true);
					}else if(value.equalsIgnoreCase("off")||value.equalsIgnoreCase("false")){
						duel.setFood(player, false);
					}
				}
			}
			return true;
		}
		return false;
	}
	
	private void setupPermissions() {
	    Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");

	    if (permissionHandler == null) {
	        if (permissionsPlugin != null) {
	            permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	        }
	    }
	}
}


