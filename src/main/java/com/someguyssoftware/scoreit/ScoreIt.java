/*
 * This file is part of  Score It.
 * Copyright (c) 2021, Mark Gottschling (gottsch)
 * 
 * All rights reserved.
 *
 * Score It is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Score It is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Protect It.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.someguyssoftware.scoreit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.scoreit.persistence.ScoreItSavedData;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(value = ScoreIt.MODID)
public class ScoreIt {
	// logger
	public static Logger LOGGER = LogManager.getLogger(ScoreIt.NAME);

	// constants
	public static final String MODID = "scoreit";
	public static final String NAME = "Score It";
	protected static final String VERSION = "1.1.0";

	public static ScoreIt instance;
	
	/**
	 * 
	 */
    public ScoreIt() {
    	ScoreIt.instance = this;
    	
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onWorldLoad(WorldEvent.Load event) {
		/*
		 * On load of dimension 0 (overworld), initialize the loot table's context and other static loot tables
		 */
		if (!event.getWorld().isClientSide()) {
			ServerWorld world = (ServerWorld) event.getWorld();
			ScoreItSavedData.get(world);
		}
	}
}
