package zonedabone.Duels;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import com.iConomy.*;
import com.iConomy.system.Holdings;

public class Duel{
	public Player starter;
	public Player target;
	public int starterstage;
	public int targetstage;
	public boolean keepItems = true;
	int starterStake = 0;
	int targetStake = 0;
	iConomy iconomy;
	boolean wolves = true;
	boolean food = true;
	
	public Duel(Player starter, Player target, iConomy iConomy){
		this.starter = starter;
		this.target = target;
		this.iconomy = iConomy;
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
		if(this.iconomy != null&&targetStake!=0&&starterStake!=0){
			Holdings starterBalance = iConomy.getAccount(starter.getDisplayName()).getHoldings();
			starterBalance.add(starterStake);
			Holdings targetBalance = iConomy.getAccount(target.getDisplayName()).getHoldings();
			targetBalance.add(targetStake);
		}
		Duels.duels.remove(starter);
	}
	public int lose(Player player){
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
			if(this.iconomy!=null&&targetStake!=0&&starterStake!=0){
				Holdings winnerBalance = iConomy.getAccount(winner.getDisplayName()).getHoldings();
				winnerBalance.add(starterStake+targetStake);
			}
			Duels.duels.remove(winner);
			Duels.duels.remove(loser);
			if(!keepItems){
				Inventory loserInv = loser.getInventory();
				Inventory winnerInv = winner.getInventory();
				ItemStack[] transfer = loserInv.getContents();
				loserInv.clear();
				for(int i = 0;i<transfer.length;i++){
					HashMap<Integer,ItemStack> left = winnerInv.addItem(transfer[i]);
					if(!left.isEmpty()){
						ItemStack[] drop = (ItemStack[]) left.values().toArray();
						winner.getWorld().dropItemNaturally(winner.getLocation(), drop[0]);
					}
				}
				return 1;
			}else{
				return 2;
			}
		}else{
			cancel();
			return 0;
		}
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
		if(otherLoc.distance(newLoc)>Duels.MAX_DISTANCE){
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
					lose(mover);
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
	
	public void setStake(Player player, int newStake){
		Holdings balance = iConomy.getAccount(player.getDisplayName()).getHoldings();
		String message = player.getDisplayName() + " has set their wager to " + iConomy.format(newStake)+ ".";
		if(player==starter){
			int change = newStake-starterStake;
			if(balance.hasEnough(change)){
				balance.subtract(change);
				starter.sendMessage(message);
				target.sendMessage(message);
			}else{
				player.sendMessage("You can't afford to set your stake to that.");
			}
		}else{
			int change = newStake-targetStake;
			if(balance.hasEnough(change)){
				balance.subtract(change);
				starter.sendMessage(message);
				target.sendMessage(message);
			}else{
				player.sendMessage("You can't afford to set your stake to that.");
			}
		}
	}
	
	public void setWolves(Player player, boolean value){
		if(value!=wolves){
			wolves = value;
			if(wolves){
				starter.sendMessage(player.getDisplayName() + " enabled wolves.");
				target.sendMessage(player.getDisplayName() + " enabled wolves.");
			}else{
				starter.sendMessage(player.getDisplayName() + " disabled wolves.");
				target.sendMessage(player.getDisplayName() + " disabled wolves.");
			}
		}
	}
	
	public void setFood(Player player, boolean value){
		if(value!=food){
			food = value;
			if(food){
				starter.sendMessage(player.getDisplayName() + " enabled food.");
				target.sendMessage(player.getDisplayName() + " enabled food.");
			}else{
				starter.sendMessage(player.getDisplayName() + " disabled food.");
				target.sendMessage(player.getDisplayName() + " disabled food.");
			}
		}
	}
	
}