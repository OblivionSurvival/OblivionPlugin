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
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

public class OblivionAnnouncer implements Runnable {

	OblivionPlugin plugin;
	Logger log;
	Server server;
	
	long intervalTicks = 0;
	String prefix = null;
	ArrayList<String> messages = null;
	
	boolean randomized = false;
	Random random = new Random(); // for random announcements
	int nextMsg = 0; // for linear announcements
	
	public OblivionAnnouncer(OblivionPlugin p) {
		plugin = p;
		log = plugin.getLogger();
		server = plugin.getServer();
		
		FileConfiguration config = plugin.getConfig();
		
		// Check if the announcer should be enabled (default: false)
		boolean enabled = config.getBoolean("announcer.enabled", false);
		if (!enabled) {
			// Don't try to initialise anything else if disabled
			return;
		}
		
		// Load all the messages
		messages = (ArrayList<String>)config.getStringList("announcer.messages");
		if (messages == null || messages.size() < 1) {
			log.info("Announcer can't start: No messages specified in configuration.");
			return;
		}
		
		// Replace all &DARK_RED; etc. color tags with color codes
		for (String msg : messages) {
			msg = plugin.replaceColorTags(msg);
		}
		
		// Get randomized (default: true)
		randomized = config.getBoolean("announcer.randomized", true);
		
		// Get message prefix (default: empty string)
		prefix = config.getString("announcer.prefix", "");
		prefix = plugin.replaceColorTags(prefix);
				
		// Get interval (default: 60 seconds)
		long interval = config.getLong("announcer.interval", 60);
		intervalTicks = interval * 20;
		
		// Start the scheduler
		server.getScheduler().scheduleSyncRepeatingTask(plugin, this, intervalTicks, intervalTicks);
	}
	
	// Announce to the server
	public void run() {
		String msg;
		
		if (randomized) {
			msg = messages.get( random.nextInt(messages.size()) );
		} else {
			msg = messages.get( nextMsg );
			
			nextMsg++;
			nextMsg %= messages.size();
		}
		
		server.broadcastMessage(prefix + msg);
	}
}
