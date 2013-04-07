package com.zachpotter.bukkit.stfboutposts;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;

public class OutpostCommandExecutor implements CommandExecutor {

	private FactionsOutposts plugin;

	public OutpostCommandExecutor(FactionsOutposts outposts) {
		this.plugin = outposts;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String subcommand = (label.equalsIgnoreCase("outpost") && args.length > 0)? args[0].toLowerCase() : null;
		if (subcommand == null) {
			// Check shortcut commands
			if (label.equalsIgnoreCase("ow") && args.length > 0)
				return warpToOutpost(sender, args[0]);
			if (label.equalsIgnoreCase("ol"))
				return listOutposts(sender);
			if (label.equalsIgnoreCase("outpost"))
				return displayHelp(sender);
		} else {
			// Check normal commands
			if (subcommand.equals("create") && args.length > 1)
				return createOutpost(sender, args);
			if (subcommand.equals("delete") && args.length > 1)
				return deleteOutpost(sender, args[1]);
			if (subcommand.equals("setwarp"))
				return setOutpostWarp(sender);
			if (subcommand.equals("list"))
				return listOutposts(sender);
			if (subcommand.equals("warp") && args.length > 1)
				return warpToOutpost(sender, args[1]);
			if (subcommand.equals("help"))
				return displayHelp(sender);
		}
		return false;
	}

	private boolean createOutpost(CommandSender sender, String[] args) {
		// Must have permission
		if (!sender.isOp()) {
			sendInsufficientPermissionsMessage(sender);
			return true;
		}
		// Only for Players
		if (!(sender instanceof Player)) {
			sendPlayerOnlyMessage(sender);
			return true;
		}
		// No spaces allowed in the name
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED+"Outpost names cannot contain spaces.");
			return true;
		}
		// Attempt to create the outpost
		Outpost outpost = plugin.createOutpost(args[1], ((Player) sender).getLocation());
		if (outpost == null) {
			sender.sendMessage(ChatColor.RED+"Couldn't create an outpost in this chunk.");
			return true;
		}
		sender.sendMessage(ChatColor.YELLOW+"Created outpost "+outpost.getName()+" with a warp at your location.");
		return true;
	}

	private boolean deleteOutpost(CommandSender sender, String name) {
		// Must have permission
		if (!sender.isOp()) {
			sendInsufficientPermissionsMessage(sender);
			return true;
		}
		// TODO
		sender.sendMessage("delete not implemented");
		return true;
	}

	private boolean setOutpostWarp(CommandSender sender) {
		// Must have permission
		if (!sender.isOp()) {
			sendInsufficientPermissionsMessage(sender);
			return true;
		}
		// Only for Players
		if (!(sender instanceof Player)) {
			sendPlayerOnlyMessage(sender);
			return true;
		}
		Outpost currentOutpost = plugin.getOutpost(((Player) sender).getLocation().getChunk());
		if (currentOutpost == null) {
			sender.sendMessage(ChatColor.RED+"You aren't standing in an outpost.");
			return true;
		}
		currentOutpost.setWarp(((Player)sender).getLocation());
		sender.sendMessage(ChatColor.YELLOW+"Outpost warp updated.");
		return true;
	}

	private boolean listOutposts(CommandSender sender) {
		FPlayer fplayer = (sender instanceof Player)? FPlayers.i.get((Player) sender) : null;
		Collection<Outpost> outposts = plugin.getOutposts();
		String msg = ChatColor.AQUA+"Outposts: ";
		if (!outposts.isEmpty()) {
			for (Outpost outpost : outposts) {
				msg += outpost.getOwner().getTag(fplayer) + ", ";
			}
			msg = msg.substring(0, msg.length()-2);
		} else {
			msg += "(none)";
		}
		sender.sendMessage(msg);
		return true;
	}

	private boolean warpToOutpost(CommandSender sender, String name) {
		// Only for Players
		if (!(sender instanceof Player)) {
			sendPlayerOnlyMessage(sender);
			return true;
		}
		Outpost warpDestination = plugin.findOutpost(name);
		if (warpDestination == null) {
			sender.sendMessage(ChatColor.RED+"Couln't find a matching outpost for "+name+".");
			return true;
		}
		FPlayer fplayer = FPlayers.i.get((Player) sender);
		warpDestination.warpPlayer(fplayer);
		return true;
	}

	private boolean displayHelp(CommandSender sender) {
		// TODO
		sender.sendMessage("help not implemented");
		return true;
	}

	private void sendPlayerOnlyMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+"You must be an in-game player to do that.");
	}

	private void sendInsufficientPermissionsMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+"You do not have permissions to do that.");
	}

}
