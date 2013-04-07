package com.zachpotter.bukkit.stfboutposts;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;

public class Outpost {

	private String name;
	private Chunk chunk;
	private Faction owner;
	private Location warp;

	private Set<FPlayer> friendlies;
	private Set<FPlayer> attackers;
	private Set<Faction> factions;

	public Outpost(String name, Chunk chunk) {
		this.name = name;
		this.chunk = chunk;

		owner = null;
		warp = null;
		friendlies = new HashSet<FPlayer>();
		attackers = new HashSet<FPlayer>();
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
	}

	public void leave(FPlayer player) {
		FactionsOutposts.logInfo("Leaving "+name+": "+player.getName());
	}

	/**
	 * Attempt to warp a player to the outpost and send a status message to them
	 * @param player The player to warp
	 * @return true if successful, else false
	 */
	public boolean warpPlayer(FPlayer player) {
		// TODO check that a player can warp
		player.sendMessage(ChatColor.GREEN+"* Warped to " + name + " *");
		player.getPlayer().teleport(warp);
		return true;
	}

}
