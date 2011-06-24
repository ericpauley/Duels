package zonedabone.Duels;

import java.util.Comparator;

public class HighscoreComparator implements Comparator<String> {
	public int compare(String name1, String name2){
		double rating1 = Duels.highscores.getDouble(name1+".rating", 1000);
		double rating2 = Duels.highscores.getDouble(name1+".rating", 1000);
		if(rating1>rating2){
			return -1;
		}else if(rating2>rating1){
			return 1;
		}else{
			return 0;
		}
	}
}
