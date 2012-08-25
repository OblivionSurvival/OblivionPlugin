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

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class EnderDragonManager implements Runnable, Listener, CommandExecutor {

	OblivionPlugin plugin;
	
	public EnderDragonManager(OblivionPlugin p) {
		plugin = p;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getCommand("resetdragon").setExecutor(this);
	}
	
	// 
	public void run() {
		
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		World world = event.getPlayer().getWorld();
		
		if (world.getName().equalsIgnoreCase("world_the_end")) {
			ArrayList<EnderDragon> dragons = (ArrayList<EnderDragon>) world.getEntitiesByClass(EnderDragon.class);
			
			if (dragons.isEmpty()) {
				plugin.getLogger().info("No Ender Dragons are in the end!");
				resetDragon();
			} else {
				plugin.getLogger().info("There are Ender Dragons in the end!");
			}
		}
		
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (command.getName().equalsIgnoreCase("resetdragon")) {
			if (sender.hasPermission("oblivion.command.resetdragon")) {
				sender.sendMessage(ChatColor.BLUE + "Resetting Ender Dragon...");
				resetDragon();
			} else {
				sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
			}
			
			return true;
		}
		
		return false;
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		
	}
	
	public void resetDragon() {
		World endWorld = plugin.getServer().getWorld("world_the_end");
		if (endWorld == null) {
			plugin.getLogger().info("resetDragon: The world 'world_the_end' wasn't found!");
			return;
		}
		
		for (EnderDragon dragon : endWorld.getEntitiesByClass(EnderDragon.class)) {
			dragon.remove();
		}
		
		// Hopefully the spawn location is ok for spawning an Ender Dragon...
		// TODO: Find original Ender Dragon spawning algorithm
		endWorld.spawnEntity(endWorld.getSpawnLocation(), EntityType.ENDER_DRAGON);
		
		plugin.getServer().broadcastMessage(ChatColor.GREEN + "The Ender Dragon has been respawned in The End!");
		plugin.getLogger().info("resetDragon: The Ender Dragon has been respawned in The End!");
	}
}
