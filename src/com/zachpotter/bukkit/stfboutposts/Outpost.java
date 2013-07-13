package com.zachpotter.bukkit.stfboutposts;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.Rel;

public class Outpost {

	private static final long ONE_SEC = 20; // 20 ticks = 1 second

	private final FactionsOutposts plugin;
	private ControlRunner timer;

	private String name;
	private Chunk chunk;
	private Faction owner;
	private Location warp;

	private HashSet<FPlayer> defenders;
	private HashSet<FPlayer> attackers;
	private int control;

	private boolean attacking;

	public Outpost(String name, Chunk chunk, FactionsOutposts plugin) {
		this.name = name;
		this.chunk = chunk;
		this.plugin = plugin;

		owner = null;
		warp = null;

		defenders = new HashSet<FPlayer>();
		attackers = new HashSet<FPlayer>();
		control = 0;

		attacking = false;

		timer = new ControlRunner();
	}

	public String getName() {
		return name;
	}

	public Faction getOwner() {
		return owner;
	}

	public void setWarp(Location warpLoc) {
		warp = warpLoc;
	}

	public void enter(FPlayer player) {
		FactionsOutposts.logInfo("Entering "+name+": "+player.getName());
		// Display outpost entry message
		String msg = ChatColor.GOLD + " ~ Outpost " + name + " - ";
		if (owner == null) {
			msg += "Not controlled by any faction";
		} else {
			msg += owner.getTag(player) + " controlled";
		}
		player.sendMessage(msg);

		if ((owner == null && player.hasFaction()) || player.getRelationTo(owner).equals(Rel.ENEMY)) {
			// Attack if enemies or if no owner
			attackers.add(player);
			if (!attacking && attackers.size() == 1) {
				// If this was the first attacker, start the attack
				startControl();
			}
		} else if (player.getRelationTo(owner).isAtLeast(Rel.MEMBER)) {
			// Defend if member
			defenders.add(player);
			if (control < 100 && !attacking) {
				startControl();
			}
		}
	}

	public void leave(FPlayer player) {
		FactionsOutposts.logInfo("Leaving "+name+": "+player.getName());
		// Remove player from attackers/defenders if in a list
		if (!attackers.remove(player)) {
			defenders.remove(player);
		}
	}

	/**
	 * Attempt to warp a player to the outpost and send a status message to them
	 * @param player The player to warp
	 * @return true if successful, else false
	 */
	public boolean warpPlayer(FPlayer player) {
		if (owner == null || player.getRelationTo(owner).isLessThan(Rel.MEMBER)) {
			// The player must be part of the owning faction
			player.sendMessage(ChatColor.RED+"Your faction doesn't control outpost "+name+".");
			return false;
		}
		// TODO check for existing attacks
		player.getPlayer().teleport(warp);
		player.sendMessage(ChatColor.GREEN+"* Warped to " + name + " *");
		return true;
	}

	private void startControl() {
		// This task triggers advances the outpost attack each time it's run
		attacking = true;
		FactionsOutposts.logInfo("Starting attack!");
		// Run the task synchronously
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, timer, ONE_SEC);
	}

	private void controlTick() {
		if (owner != null) {
			// There is an owner to this outpost
			if (defenders.size() == 0 && attackers.size() > 0) {
				// Attack
				control -= rateOfControl(attackers.size());
			} else if (defenders.size() > 0 && attackers.size() == 0) {
				// Recover
				control += rateOfControl(defenders.size());
			}
		} else {
			// There is not an owner to this outpost
			// Give control to a faction if they are the only entries in attackers
			Iterator<FPlayer> iter = attackers.iterator();
			Faction attacker = iter.next().getFaction();
			if (attacker != null) {
				while (iter.hasNext()) {
					if (!iter.next().getFaction().equals(attacker)) {
						attacker = null;
						break;
					}
				}
				if (attacker != null) {
					owner = attacker;
					defenders.addAll(attackers);
					attackers.clear();
					control += rateOfControl(defenders.size());
					FactionsOutposts.logInfo("Outpost "+name+" now under "+owner.getTag()+" control.");
				}
			}
		}

		if (control < 0) {
			// Outpost is now neutral
			control = 0;
			// Defenders are now attackers along with everyone else
			attackers.addAll(defenders);
			defenders.clear();
			owner = null;
		} else if (control > 100) {
			control = 100;
		}
		FactionsOutposts.logInfo("Outpost "+name+": "+control+"% control");
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, timer, ONE_SEC);
	}

	private static int rateOfControl(int numPlayers) {
		return 3;
	}

	private class ControlRunner implements Runnable {

		@Override
		public void run() {
			if ((defenders.size() == 0 && attackers.size() > 0)
					|| (control < 100 && attackers.size() == 0 && defenders.size() > 0)) {
				controlTick();
			} else {
				attacking = false;
				FactionsOutposts.logInfo("Stopped attack!");
			}
		}
	}

}
