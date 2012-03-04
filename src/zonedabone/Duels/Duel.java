package zonedabone.Duels;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.nijikokun.register.payment.Method;

public class Duel{
	public Player starter;
	public Player target;
	public int starterstage;
	public int targetstage;
	public boolean keepItems = Duels.KEEP_ITEMS;
	int starterStake = Duels.STAKE;
	int targetStake = Duels.STAKE;
	boolean wolves = Duels.WOLVES;
	boolean food = Duels.WOLVES;
	Method method = null;
	
	public Duel(Player starter, Player target, Method method){
		this.starter = starter;
		this.target = target;
		starterstage = 1;
		targetstage = 0;
		this.method = method;
	}
	
	public void accept(){
		targetstage = 1;
	}
	
	public void confirm(Player player){
		if(player == starter){
			starterstage = 2;
			if(targetstage == 1){
				target.sendMessage(MessageParser.parseMessage(Duels.messages.get("PLAYER_READY"), "{PLAYER}", starter.getDisplayName()));

			}else{
				target.sendMessage(Duels.getMessage("DUEL_START"));
				starter.sendMessage(Duels.getMessage("DUEL_START"));
			}
		}else{
			targetstage = 2;
			if(starterstage == 1){
				starter.sendMessage(MessageParser.parseMessage(Duels.messages.get("PLAYER_READY"), "{PLAYER}", target.getDisplayName()));
			}else{
				target.sendMessage(Duels.getMessage("DUEL_START"));
				starter.sendMessage(Duels.getMessage("DUEL_START"));
			}
		}
	}
	
	public void cancel(){
		if (targetstage != 0){
			target.sendMessage(Duels.getMessage("DUEL_CANCEL"));
			Duels.duels.remove(target);
		}
		starter.sendMessage(Duels.getMessage("DUEL_CANCEL"));
		if(method != null&&targetStake!=0&&starterStake!=0){
			method.getAccount(starter.getName()).add(starterStake);
			method.getAccount(target.getName()).add(targetStake);
		}
		Duels.duels.remove(starter);
	}
	public boolean lose(Player player, boolean died){
		Player loser = player;
		Player winner;
		if(starter == player){
			winner = target;
		}else{
			winner = starter;
		}
		loser.sendMessage(Duels.getMessage("DUEL_LOSE"));
		winner.sendMessage(Duels.getMessage("DUEL_WIN"));
		//Set highscores
		String winnerName = winner.getName();
		String loserName = loser.getName();
		double winnerRating = Duels.highscores.getDouble(winnerName+".rating", Duels.STARTING_RATING);
		double loserRating = Duels.highscores.getDouble(loserName+".rating", Duels.STARTING_RATING);
		double winnerChance = 1/(1+Math.pow(10,(loserRating-winnerRating)/400));
		double outcome;
		if(died&&Duels.RANKING_WEIGHT!=0){
			outcome = ((double)winner.getHealth())/20;
			outcome = Math.pow(outcome, 1/Duels.RANKING_WEIGHT);
		}else{
			outcome = 1;
		}
		double change = Duels.RANKING_MAGNITUDE*(outcome-winnerChance);
		Duels.highscores.set(winnerName+".rating", winnerRating+change);
		Duels.highscores.set(loserName+".rating", loserRating-change);
		Duels.highscores.set(winnerName+".duels", Duels.highscores.getInt(winnerName+".duels", 0)+1);
		Duels.highscores.set(loserName+".duels", Duels.highscores.getInt(loserName+".duels", 0)+1);
		Duels.highscores.set(winnerName+".wins", Duels.highscores.getInt(winnerName+".wins", 0)+1);
		Duels.highscores.set(loserName+".losses", Duels.highscores.getInt(loserName+".losses", 0)+1);
		//Set Highscores
		if(this.method!=null){
			method.getAccount(winner.getName()).add(starterStake+targetStake);
		}
		Duels.duels.remove(winner);
		Duels.duels.remove(loser);
		if(!keepItems){
			Inventory loserInv = loser.getInventory();
			Inventory winnerInv = winner.getInventory();
			ItemStack[] transfer = loserInv.getContents();
			loserInv.clear();
			for(int i = 0;i<transfer.length;i++){
				if(transfer[i]!=null){
					HashMap<Integer,ItemStack> left = winnerInv.addItem(transfer[i]);
					if(!left.isEmpty()){
						ItemStack[] drop = (ItemStack[]) left.values().toArray();
						winner.getWorld().dropItemNaturally(winner.getLocation(), drop[0]);
					}
				}
			}
			return false;
		}
		return true;
	}
	
	public void checkLocations(Player mover, PlayerMoveEvent e){
		if(mover!=starter&&mover!=target){return;}
		Location newLoc = e.getTo();
		Location otherLoc;
		if(mover == starter){
			otherLoc=target.getLocation();
		}else{
			otherLoc=starter.getLocation();
		}
		if(newLoc==null || otherLoc==null || newLoc.getWorld()!=otherLoc.getWorld() || otherLoc.distance(newLoc)>Duels.MAX_DISTANCE){
			if(starterstage==1&&targetstage==1){
				if(Duels.FORCE_FIELD_BEFORE){
					e.setCancelled(true);
				}else{
					cancel();
				}
			}else if (starterstage==2&&targetstage==2){
				if(Duels.FORCE_FIELD_DURING){
					e.setCancelled(true);
				}else{
					lose(mover, false);
				}
			}else{
				cancel();
			}
		}
	}
	
	public void disconnect(Player player){
		if(starterstage<2&&targetstage<2){
			cancel();
		}else{
			lose(player, false);
		}
	}
	
	public void setKeepItems(Player player, boolean value){
		if(value==keepItems){return;}
		keepItems = value;
		if(value){
			starter.sendMessage(Duels.getMessage("SET_KEEP_ITEMS"));
			target.sendMessage(Duels.getMessage("SET_KEEP_ITEMS"));
		}else{
			starter.sendMessage(Duels.getMessage("SET_LOSE_ITEMS"));
			target.sendMessage(Duels.getMessage("SET_LOSE_ITEMS"));
		}
	}
	
	public void setStake(Player player, int newStake){
		String message = MessageParser.parseMessage(Duels.messages.get("PLAYER_READY"), "{PLAYER}", player.getDisplayName(), "{STAKE}", method.format(newStake));
		if(player==starter){
			int change = newStake-starterStake;
			if(method.getAccount(player.getName()).hasEnough(change)){
				method.getAccount(player.getName()).subtract(change);
				starterStake = newStake;
				starter.sendMessage(message);
				target.sendMessage(message);
			}else{
				player.sendMessage(Duels.getMessage("BLOCK_SET_STAKE"));
			}
		}else{
			int change = newStake-targetStake;
			if(method.getAccount(player.getName()).hasEnough(change)){
				method.getAccount(player.getName()).subtract(change);
				targetStake = newStake;
				starter.sendMessage(message);
				target.sendMessage(message);
			}else{
				player.sendMessage(Duels.getMessage("BLOCK_SET_STAKE"));
			}
		}
	}
	
	public void setWolves(Player player, boolean value){
		if(value!=wolves){
			wolves = value;
			if(wolves){
				starter.sendMessage(Duels.getMessage("WOLF_ENABLE"));
				target.sendMessage(Duels.getMessage("WOLF_ENABLE"));
			}else{
				starter.sendMessage(Duels.getMessage("WOLF_DISABLE"));
				target.sendMessage(Duels.getMessage("WOLF_DISABLE"));
			}
		}
	}
	
	public void setFood(Player player, boolean value){
		if(value!=food){
			food = value;
			if(food){
				starter.sendMessage(Duels.getMessage("FOOD_ENABLE"));
				target.sendMessage(Duels.getMessage("FOOD_ENABLE"));
			}else{
				starter.sendMessage(Duels.getMessage("FOOD_DISABLE"));
				target.sendMessage(Duels.getMessage("FOOD_DISABLE"));
			}
		}
	}
	
}