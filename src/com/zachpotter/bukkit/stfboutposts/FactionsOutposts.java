package com.zachpotter.bukkit.stfboutposts;

import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;


/**
 * TODO
 * @author Zachary Potter
 *
 */
public class FactionsOutposts extends JavaPlugin {

	private static Logger logger;

	private static final String WARZONE_ID = "-2";
	private static final String WILDERNESS_ID = "0";

	private HashMap<Chunk, Outpost> outposts;

	private OutpostBoardManager outpostBoard;

	@Override
	public void onDisable() {
		// Nothing to do here.
	}

	@Override
	public void onEnable() {
		logger = getLogger();
		long start = System.currentTimeMillis();
		// Find out if Factions exists and is enabled
		Plugin factionTester = this.getServer().getPluginManager().getPlugin("Factions");

		if (factionTester != null && factionTester.isEnabled()
				&& factionTester.getDescription().getVersion().compareTo("1.8") >= 0) {
			logInfo("Factions plugin found. Hooking up and getting freaky.");

			// Set up outpost data
			outposts = new HashMap<Chunk, Outpost>();
			loadOutposts();

			// Register player listener
			getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

			// Register commands
			OutpostCommandExecutor commander = new OutpostCommandExecutor(this);
			getCommand("outpost").setExecutor(commander);
			getCommand("ow").setExecutor(commander);
			getCommand("ol").setExecutor(commander);

			// Initialize the outpost scoreboard
			outpostBoard = new OutpostBoardManager(this);

		} else {
			logInfo("Suitable Factions plugin not found. Get Factions v1.8 or higher!");
			getServer().getPluginManager().disablePlugin(this);
		}
		logInfo("Done! (" + (System.currentTimeMillis() - start) + "ms)");
	}

	/**
	 * Loads outpost data from file
	 */
	private void loadOutposts() {
		// TODO
	}

	/**
	 * Create an Outpost.
	 * @param warp The location of the Outpost warp
	 * @param name The name of the outpost (no spaces allowed)
	 * @return An Outpost if successful, or null if a conflict exists
	 */
	public Outpost createOutpost(String name, Location warp) {
		// Check for existing outposts with similar names
		if (findOutpost(name) != null) {
			return null;
		}
		// Check for existing outposts in same chunk
		Chunk outpostChunk = warp.getChunk();
		for (Chunk chunk : outposts.keySet()) {
			if (chunk.equals(outpostChunk)) {
				return null;
			}
		}
		// Make sure outpost is being set in wilderness or warzone
		Faction faction = Board.getFactionAt(warp);
		if (!faction.getId().equals(WILDERNESS_ID) && !faction.getId().equals(WARZONE_ID) ) {
			return null;
		}
		// Create the new outpost
		Outpost outpost = new Outpost(name, outpostChunk, this);
		// Set outpost's warp to given location
		outpost.setWarp(warp);
		// Add the outpost to the map and claim it as a Factions warzone
		outposts.put(outpostChunk, outpost);
		Board.setIdAt(WARZONE_ID, new FLocation(warp));
		return outpost;
	}

	/**
	 * @param chunk The Chunk location of the Outpost to get
	 * @return an Outpost if it exists, or null
	 */
	public Outpost getOutpost(Chunk chunk) {
		return outposts.get(chunk);
	}

	/**
	 * Finds the outpost with the best matching name
	 * @param str An approximate match for the outpost name
	 * @return A matching Outpost or null
	 */
	public Outpost findOutpost(String str) {
		String name = str.toLowerCase();
		Outpost bestMatch = null;
		for (Outpost outpost : outposts.values()) {
			if (outpost.getName().toLowerCase().startsWith(name)) {
				if (bestMatch == null) {
					bestMatch = outpost;
				} else {
					// Found a second match with similar name.
					// Too ambiguous, return null.
					return null;
				}
			}
		}
		return bestMatch;
	}

	/**
	 * @return All outposts
	 */
	public Collection<Outpost> getOutposts() {
		return outposts.values();
	}

	public void updateOutpostBoard(Outpost outpost) {
		outpostBoard.update(outpost);
	}

	//Prints msg to console
	public static void logInfo(String msg) {
		logger.info( msg );
	}

	public static void logWarning(String errorMsg) {
		logger.warning( errorMsg );
	}

}














