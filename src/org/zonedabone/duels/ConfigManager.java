package org.zonedabone.duels;

import java.util.List;

import org.bukkit.configuration.Configuration;


public class ConfigManager {
	
	
	public static int MAX_DISTANCE;
	public static boolean FORCE_FIELD_DURING;
	public static boolean FORCE_FIELD_BEFORE;
	public static boolean FORCE_PVP;
	public static boolean USE_PERMISSIONS;
	public static boolean NO_DUEL_PVP;
	public static double RANKING_WEIGHT;
	public static double RANKING_MAGNITUDE;
	public static double STARTING_RATING;
	public static int DEFAULT_TOP_COUNT;
	public static int STAKE;
	public static boolean WOLVES;
	public static boolean FOOD;
	public static boolean KEEP_ITEMS;
	public static List<String> DISABLED_WORLDS;

	public static void loadConfig(Duels plugin){
		Configuration config = plugin.getConfig();
		MAX_DISTANCE = config.getInt("maxdistance", 20);
		// Max distance between players during the duel. (Instead of surrender)
		FORCE_FIELD_DURING = config.getBoolean("forcefeild.during", false);
		// Max distance between players while preparing the duel. (Instead of cancel)
		FORCE_FIELD_BEFORE = config.getBoolean("forcefeild.before", false);
		// Whether or not to override other pvp plugins during duels
		FORCE_PVP = config.getBoolean("forcepvp", true);
		// Whether or not to use permissions
		USE_PERMISSIONS = config.getBoolean("usepermissions", true);
		// If people can pvp outside of duels
		NO_DUEL_PVP = config.getBoolean("noduelpvp", false);
		// How to weight duel rankings
		RANKING_WEIGHT = config.getDouble("highscores.weightedratings", 1);
		// The maximum ranking effect a duel can have
		RANKING_MAGNITUDE = config.getDouble("highscores.ratingmagnitude", 10);
		// The starting rating for new players
		STARTING_RATING = config.getDouble("highscores.weightedratings", 1000);
		// The starting rating for new players
		DEFAULT_TOP_COUNT = config.getInt("highscores.defaulttopcount", 10);
		// The default stake
		STAKE = config.getInt("defaults.stake", 0);
		// The default wolves setting
		WOLVES = config.getBoolean("defaults.wolves", true);
		// The default food setting
		FOOD = config.getBoolean("defaults.food", true);
		// The default keepitems setting
		KEEP_ITEMS = config.getBoolean("defaults.keepitems", true);
		//Worlds to ignore completely
		DISABLED_WORLDS = config.getStringList("disabledworlds");
	}
}
