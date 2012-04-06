package org.zonedabone.duels;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;


public class MessageManager {
	
	private static Map<String, String> messages = new ConcurrentHashMap<String, String>();
	
	public static void loadMessages(Duels plugin){
		File f = new File(plugin.getDataFolder(), "messages.yml");
		if (!f.exists()) {
			plugin.getLogger().info("Creating default messages.yml.");
			plugin.saveResource("messages.yml", true);
		}
		Configuration config = YamlConfiguration.loadConfiguration(f);
		for(String k:config.getKeys(true)){
			if(config.isString(k)){
				messages.put(k, config.getString(k));

			}
		}
		plugin.getLogger().info("Loaded "+messages.size()+" messages.");
	}
	
	public static String parseMessage(String message, String... replacements){
		message = message.toLowerCase();
		String raw = messages.get(message);
		String prefix = messages.get("prefix");
		if(raw != null && prefix !=null){
			for(int i = 0;i<replacements.length-1;i++){
				raw = raw.replaceAll("(?iu)\\{"+replacements[i]+"[a-zA-Z0-9]*\\}", replacements[i+1]);
			}
			return ChatColor.translateAlternateColorCodes('&', prefix+raw);
		}else{
			return "Could not find message "+message+".";
		}
	}
	
	public static void sendMessage(CommandSender cs,String message, String... replacements){
		cs.sendMessage(parseMessage(message, replacements));
	}
}
