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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class OblivionEventListener implements Listener {

	OblivionPlugin plugin;

	public OblivionEventListener(OblivionPlugin p) {
		plugin = p;
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		// Coloured/Magic/Formatted text for signs!
		String[] lines = event.getLines();
		for (int i = 0; i < lines.length; i++) {
			String newLine = lines[i];
			if (event.getPlayer().hasPermission("oblivion.sign.color")) {
				newLine = newLine.replaceAll("(?i)&([0-9A-F])", "\u00A7$1");
			}
			if (event.getPlayer().hasPermission("oblivion.sign.magic")) {
				newLine = newLine.replaceAll("(?i)&([K])", "\u00A7$1");
			}
			if (event.getPlayer().hasPermission("oblivion.sign.format")) {
				newLine = newLine.replaceAll("(?i)&([L-OR])", "\u00A7$1");
			}
			event.setLine(i, newLine);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		String msg = event.getMessage();

		// Handle illegal chat text
		if (!event.getPlayer().hasPermission("oblivion.chat.forbiddenchars")) {
			if (msg.contains("§") || msg.contains("\n") || msg.contains("\r") || msg.contains("     ")) {
				plugin.log.info(event.getPlayer().getName() + " attempted to chat an illegal message: \"" + msg + "\"");
				event.getPlayer().kickPlayer("LOL NOPE.");
				plugin.getServer().broadcastMessage(event.getPlayer().getName() + " was PWNT by Oblivion Survival");
				event.setCancelled(true);
			}
		}

		// Convert ampersand colour codes
		if (event.getPlayer().hasPermission("oblivion.chat.color")) {
			msg = msg.replaceAll("(?i)&([0-9A-F])", "\u00A7$1");
		}
		if (event.getPlayer().hasPermission("oblivion.chat.magic")) {
			msg = msg.replaceAll("(?i)&([K])", "\u00A7$1");
		}
		if (event.getPlayer().hasPermission("oblivion.chat.format")) {
			msg = msg.replaceAll("(?i)&([L-OR])", "\u00A7$1");
		}

		// HAL
		if (msg.equalsIgnoreCase("open the pod bay doors")) {
			String halReply = ChatColor.RED + "I'm sorry, " + event.getPlayer().getName() + ". I'm afraid I can't do that.";
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new OblivionRunnable(plugin, halReply));
		}

		// Takei Filter
		msg = msg.replaceAll("(?i)gay", "takei");

		event.setMessage(msg);
	}

	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {
		Block block = event.getBlock();

		// -- DEBUG --
		// plugin.getLogger().info("onBlockRedstone: block type = " + block.getType());
		// plugin.getLogger().info("  New current: " + event.getNewCurrent() + ", Old Current: " + event.getOldCurrent());
		// plugin.getLogger().info("  Block Power: " + block.getBlockPower() + ", IsPowered: " + block.isBlockPowered() + ", IsIndirectPowered: " + block.isBlockIndirectlyPowered());

		// Make pumpkins toggle on/off with redstone
		if (block.getType() == Material.PUMPKIN
				|| block.getType() == Material.JACK_O_LANTERN) {
			byte facing = block.getData();

			// if (event.getNewCurrent() > 0) {
			if (block.isBlockIndirectlyPowered()) {
				block.setType(Material.JACK_O_LANTERN);
			} else {
				block.setType(Material.PUMPKIN);
			}

			block.setData(facing);

			return;
		}

		// Make netherrack ignite/unignite with redstone
		if (block.getType() == Material.NETHERRACK) {
			if (block.isBlockIndirectlyPowered()) {
				Block aboveBlock = block.getRelative(BlockFace.UP);
				if (aboveBlock.getType() == Material.AIR) {
					aboveBlock.setType(Material.FIRE);
				}
			} else {
				Block aboveBlock = block.getRelative(BlockFace.UP);
				if (aboveBlock.getType() == Material.FIRE) {
					aboveBlock.setType(Material.AIR);
				}
			}

			return;
		}

		/*
		 * if (block.getType() == Material.JUKEBOX) { Jukebox jukebox =
		 * (Jukebox)block.getState(); if (event.getNewCurrent() >
		 * event.getOldCurrent()) {
		 * plugin.getLogger().info("Jukebox Redstone: getPlaying() = " +
		 * jukebox.getPlaying() + ", isPlaying() = " + jukebox.isPlaying()); }
		 * return; }
		 */
	}
	
	// No longer needed
	/*@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if (event.isCancelled()) {
			return;
		}
		
		// Disable rain/snow
		if (event.toWeatherState() == true) {
			event.setCancelled(true);
		}
	}*/

	@EventHandler
	public void onPlayerDeath(EntityDeathEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getEntity();
		plugin.playerDeathLocations.put(player, player.getLocation());
		player.sendMessage(ChatColor.GOLD + "You can use /back to teleport to where you died.");
	}
}
