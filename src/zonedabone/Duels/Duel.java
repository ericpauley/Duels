package zonedabone.Duels;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Duel{
	public Player starter;
	public Player target;
	public int starterstage;
	public int targetstage;
	public Location duelLocation;
	public boolean keepItems = true;
	
	public Duel(Player starter, Player target){
		this.starter = starter;
		this.target = target;
		this.duelLocation = starter.getLocation();
		starterstage = 1;
		targetstage = 0;
	}
	
	public void accept(){
		targetstage = 1;
	}
	
	public void confirm(Player player){
		if(player == starter){
			starterstage = 2;
			if(targetstage == 1){
				target.sendMessage(starter.getDisplayName() + "is ready to start the duel.");

			}else{
				target.sendMessage("FIGHT TO THE DEATH!");
				starter.sendMessage("FIGHT TO THE DEATH!");
			}
		}else{
			targetstage = 2;
			if(starterstage == 1){
				starter.sendMessage(target.getDisplayName() + "is ready to start the duel.");
			}else{
				target.sendMessage("FIGHT TO THE DEATH!");
				starter.sendMessage("FIGHT TO THE DEATH!");
			}
		}
	}
	
	public void cancel(){
		if (targetstage != 0){
			target.sendMessage("The duel has been canceled!");
			Duels.duels.remove(target);
		}
		starter.sendMessage("The duel has been canceled!");
		Duels.duels.remove(starter);
	}
	public boolean lose(Player player){
		if(starterstage==2&&targetstage==2){
			Player loser = player;
			Player winner;
			if(starter == player){
				winner = target;
			}else{
				winner = starter;
			}
			loser.sendMessage("You lost the duel!");
			winner.sendMessage("You won the duel!");
			Duels.duels.remove(winner);
			Duels.duels.remove(loser);
			if(!keepItems){
				Inventory loserInv = loser.getInventory();
				Inventory winnerInv = winner.getInventory();
				ItemStack[] transfer = loserInv.getContents();
				for(int i = 0;i<transfer.length;i++){
					HashMap<Integer,ItemStack> left = winnerInv.addItem(transfer[i]);
					if(!left.isEmpty()){
						ItemStack[] drop = (ItemStack[]) left.values().toArray();
						winner.getWorld().dropItemNaturally(winner.getLocation(), drop[0]);
					}
				}
				return false;
			}else{
				return true;
			}
		}
		return false;
	}
	
	public void checkLocations(){
		if(starter.getLocation().distance(duelLocation)>20){
			if(starterstage<2&&targetstage<2){
				cancel();
			}else{
				lose(starter);
			}
		}else if(starter.getLocation().distance(duelLocation)>20){
			if(starterstage<2&&targetstage<2){
				cancel();
			}else{
				lose(target);
			}
		}
	}
	
	public void disconnect(Player player){
		if(starterstage<2&&targetstage<2){
			cancel();
		}else{
			lose(player);
		}
	}
	
	public void setKeepItems(Player player, boolean value){
		if(value==keepItems){return;}
		keepItems = value;
		if(value){
			starter.sendMessage("You will keep your items if you lose.");
			target.sendMessage("You will keep your items if you lose.");
		}else{
			starter.sendMessage("Your opponent will get your items if you lose.");
			target.sendMessage("Your opponent will get your items if you lose.");
		}
	}
	
}