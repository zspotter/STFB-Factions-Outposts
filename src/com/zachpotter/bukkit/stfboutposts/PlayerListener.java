package com.zachpotter.bukkit.stfboutposts;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

public class PlayerListener implements Listener {

	private FactionsOutposts outposts;

	public PlayerListener(FactionsOutposts plugin) {
		outposts = plugin;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;

		// From Factions source code:
		// Quick check to make sure player is moving between chunks; good performance boost
		if (
				event.getFrom().getBlockX() >> 4 == event.getTo().getBlockX() >> 4 &&
				event.getFrom().getBlockZ() >> 4 == event.getTo().getBlockZ() >> 4 &&
				event.getFrom().getWorld() == event.getTo().getWorld() ) {
			return;
		}

		// Only do things if moving to or from an outpost
		Outpost outpostFrom = outposts.getOutpost(event.getFrom().getChunk());
		Outpost outpostTo = outposts.getOutpost(event.getTo().getChunk());

		// Return if no outposts are involved
		if (outpostFrom == null && outpostTo == null) return;

		Player player = event.getPlayer();
		FPlayer me = FPlayers.i.get(player);

		if (outpostFrom != null) {
			// Update players in outpost
			outpostFrom.leave(me);
		}

		if (outpostTo != null) {
			// Set Faction's FPlayer last location to the next location so that they don't
			// display the faction entrance message. A simple hack, but works well.
			me.setLastStoodAt(new FLocation(event.getTo()));
			// Update players in outpost
			outpostTo.enter(me);
		}
	}

	// TODO, check login and logoff events when inside an outpost
	// TODO, also check for relationship change between factions while neutral inside outpost
	// TODO, also check die & spawn events in outpost
}
