/**
 * OblivionPlugin - Misc. stuff plugin for Oblivion Survival
 * Copyright (c) 2012, Dion Williams
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package com.oblivionsurvival.oblivionplugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OblivionPlugin extends JavaPlugin {
	
	OblivionEventListener eventListener;
	OblivionAnnouncer announcer;
	PluginManager pm;
	Logger log;
	EnderDragonManager enderDragonManager;
	public HashMap<Player,Location> playerDeathLocations;

    public void onEnable() {
    	pm = this.getServer().getPluginManager();
    	log = this.getLogger();
    	playerDeathLocations = new HashMap<Player,Location>();
    	ignoredPlayers = new HashMap<Player,ArrayList<OfflinePlayer>>();
    	
    	// Save non-existent config fields to config.yml
    	// TODO: Copy config.yml from JAR with help comments
    	FileConfiguration config = getConfig();
    	config.options().copyDefaults(true);
    	saveConfig();
    	
        eventListener = new OblivionEventListener(this);
        pm.registerEvents(eventListener, this);
        
        // Start the Announcer
        announcer = new OblivionAnnouncer(this);
        
        // Create an EnderDragonManager to manage Ender Dragon spawning in The End
        enderDragonManager = new EnderDragonManager(this);
        
        log.info("Enabled!");
    }
	
    public void onDisable() {
        log.info("Disabled!");
    }
    
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = null;
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		// Allow players to return to place of last death
		if (command.getName().equalsIgnoreCase("back")) {
			if (player == null) {
				sender.sendMessage("You can only use /back if you're a player!");
			} else {
				if (player.hasPermission("oblivion.command.back")) {
					if (playerDeathLocations.containsKey(player)) {
						player.teleport(playerDeathLocations.get(player));
						player.sendMessage(ChatColor.GREEN + "Teleported to your last death location.");
					} else {
						player.sendMessage(ChatColor.RED + "Your last death location wasn't found.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
				}
			}
			
			return true;
		}
		
		// No longer needed
		/*// Helpop command to get the attention of moderators/admins
		if (command.getName().equalsIgnoreCase("helpop")) {
			if (player == null) {
				sender.sendMessage("You can only use /helpop if you're a player!");
			} else {
				if (player.hasPermission("oblivion.command.helpop")) {
					if (args.length < 1) {
						return false;
					}
					
					String joinedMsg = concatArgs(args, 0);
					
					for (Player staffPerson : getServer().getOnlinePlayers()) {
						if (staffPerson.hasPermission("oblivion.helpop.isstaff")) {
							staffPerson.sendMessage(ChatColor.GOLD + "<HELPOP> " + sender.getName() + ": " + joinedMsg);
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
				}
			}
			
			return true;
		}*/
		
		// AChat command as a way for staff to chat with each other
		if (command.getName().equalsIgnoreCase("achat")) {
			if (sender.hasPermission("oblivion.command.achat")) {
				if (args.length < 1) {
					return false;
				}
				
				String joinedMsg = concatArgs(args, 0);
				
				for (Player staffPerson : getServer().getOnlinePlayers()) {
					if (staffPerson.hasPermission("oblivion.achat.isstaff")) {
						staffPerson.sendMessage(ChatColor.GREEN + "<ACHAT> " + sender.getName() + ": " + joinedMsg);
					}
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			}
			
			return true;
		}
		
		// GM command to toggle your own gamemode
		if (command.getName().equalsIgnoreCase("gm")) {
			if (player == null) {
				sender.sendMessage("You can only use /gm if you're a player!");
			} else {
				if (player.hasPermission("oblivion.command.gm")) {
					GameMode playerGM = player.getGameMode();
					switch(playerGM) {
						case SURVIVAL:
							player.setGameMode(GameMode.CREATIVE);
							break;
						case CREATIVE:
							player.setGameMode(GameMode.SURVIVAL);
							break;
					}
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
				}
			}
			
			return true;
		}
		
		// Ignore command
		if (command.getName().equalsIgnoreCase("ignore")) {
			if (player == null) {
				sender.sendMessage("You can only use /ignore if you're a player!");
			} else {
				if (player.hasPermission("oblivion.ignore")) {
					player.sendMessage(ChatColor.GREEN + "It worked!");
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
				}
			}
			
			return true;
		}
		
		// Ignorelist command
		if (command.getName().equalsIgnoreCase("ignorelist")) {
			if (args.length == 0) {
				// Only players can use /ignorelist without arguments
				if (player == null) {
					return false;
				}
				
				// Must have the ignorelist.self permission
				if (player.hasPermission("oblivion.ignorelist.self")) {
					sendIgnoreList(sender, player);
				} else {
					player.sendMessage(ChatColor.RED + "You don't have permission to perform this action.");
				}
			} else if (args.length == 1) {
				// Must have the ignorelist.other permission
				if (sender.hasPermission("oblivion.ignorelist.other")) {
					Player otherPlayer = getServer().getPlayer(args[0]);
					
					if (otherPlayer == null) {
						sender.sendMessage(ChatColor.RED + "No players matched your query.");
					} else {
						sendIgnoreList(sender, otherPlayer);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You don't have permission to perform this action.");
				}
			} else {
				// Invalid arguments, print command usage
				return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	HashMap<Player,ArrayList<OfflinePlayer>> ignoredPlayers = null;
	
	private void sendIgnoreList(CommandSender recipient, Player player) {
		ArrayList<OfflinePlayer> ignoredList = ignoredPlayers.get(player);
		
		if (ignoredList == null || ignoredList.isEmpty()) {
			if (recipient == player) {
				recipient.sendMessage("You are not ignoring anyone.");
			} else {
				recipient.sendMessage(ChatColor.AQUA + "Player '" + player.getName() + "' is not ignoring anyone.");
			}
			return;
		}
		
		// Build a formatted list of ignored players
		String msg = "";
		if (recipient == player) {
			msg += ChatColor.AQUA + "You are ignoring: ";
		} else {
			msg += ChatColor.AQUA + player.getName() + " is ignoring: ";
		}
		
		for (int i = 0; i < ignoredList.size(); i++) {
			OfflinePlayer op = ignoredList.get(i);
			msg += op.getName();
			if (i < ignoredList.size()-1) {
				msg += ", ";
			}
		}
		
		recipient.sendMessage(msg);
	}
	
	private void addIgnoredPlayer(Player player, OfflinePlayer playerToIgnore) {
		ArrayList<OfflinePlayer> ignoredList = ignoredPlayers.get(player);
		if (ignoredList == null) {
			ignoredList = new ArrayList<OfflinePlayer>();
		}
		
		ignoredList.add(playerToIgnore);
		
		ignoredPlayers.put(player, ignoredList);
	}

	public String concatArgs(String[] args, int index) {
		if (index < 0 || index >= args.length) {
			return "";
		}
				
		String newString = "";
		for (int i = index; i < args.length; i++)
		{
			newString += args[i];
			if (i < args.length-1) {
				newString += " ";
			}
		}
		
		return newString;
	}
	
	public String replaceColorTags(String text) {
		String newText = text;
		
		for (ChatColor color : ChatColor.values()) {
			newText = newText.replaceAll("(?i)&" + color.name() + ";", color.toString());
		}
		
		return newText;
	}
}
