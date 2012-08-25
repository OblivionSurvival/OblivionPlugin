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

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;

public class JailManager implements Listener {

	OblivionPlugin plugin;
	Logger log;
	Server server;
	Location jailLocation;
	Location unjailLocation;
	
	public JailManager(OblivionPlugin p) {
		plugin = p;
		log = plugin.getLogger();
		server = plugin.getServer();
		
		FileConfiguration config = plugin.getConfig();
		
		// Check if the jail manager should be enabled (default: false)
		boolean enabled = config.getBoolean("jail.enabled", false);
		if (!enabled) {
			// Don't try to initialise anything else if disabled
			return;
		}
		
		// Get the jail location
		jailLocation = parseJailLocation(config.getConfigurationSection("jail.jail-location"));
		if (jailLocation == null) {
			log.info("Jail Manager can't start: Error parsing jail location");
			return;
		}
		
		// Get the unjail location
		unjailLocation = parseJailLocation(config.getConfigurationSection("jail.unjail-location"));
		if (unjailLocation == null) {
			log.info("Jail Manager can't start: Error parsing unjail location");
			return;
		}
		
	}
	
	/**
	 * Parse a jail location section.
	 * @param section
	 * @return
	 */
	Location parseJailLocation(ConfigurationSection section) {
		String jailWorldName = section.getString("world");
		if (jailWorldName == null) {
			log.info("Jail Manager - parseJailLocation: No world specified for jail location.");
			return null;
		}
		World jailWorld = server.getWorld(jailWorldName);
		if (jailWorld == null) {
			log.info("Jail Manager - parseJailLocation: Invalid world '" + jailWorldName + "' for jail location.");
			return null;
		}

		double jailX = section.getDouble("x");
		double jailY = section.getDouble("y");
		double jailZ = section.getDouble("z");

		float jailYaw = (float) section.getDouble("yaw");
		float jailPitch = (float) section.getDouble("pitch");
		
		return new Location(jailWorld, jailX, jailY, jailZ, jailYaw, jailPitch);
	}
}
