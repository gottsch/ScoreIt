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
package com.someguyssoftware.scoreit.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.scoreit.ScoreIt;
import com.someguyssoftware.scoreit.scoreboard.Scoreboard;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.IWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

/**
 * 
 * @author Mark Gottschling on Sep 21, 2021
 *
 */
public class ScoreItSavedData extends WorldSavedData {
	public static Logger LOGGER = LogManager.getLogger(ScoreIt.NAME);
	
	public static final String GEN_DATA_KEY = ScoreIt.MODID + ":generationData";
	private static final String SCORE_IT = ScoreIt.MODID;
	private static final String SCOREBOARD = "scoreboard";
	
	public ScoreItSavedData() {
		super(GEN_DATA_KEY);
	}
	
	public ScoreItSavedData(String key) {
		super(key);
	}

	@Override
	public void load(CompoundNBT nbt) {
		LOGGER.info("loading ScoreIt data ...");
		CompoundNBT scoreIt = nbt.getCompound(SCORE_IT);
		if (scoreIt.contains(SCOREBOARD)) {
			LOGGER.info("has scoreboard, loading...");
			Scoreboard.load(scoreIt.getCompound(SCOREBOARD));
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		LOGGER.info("saving ScoreIt data...");
		// create a treasure compound			
		CompoundNBT scoreIt = new CompoundNBT();
		nbt.put(SCORE_IT, scoreIt);
		scoreIt.put(SCOREBOARD, Scoreboard.save(new CompoundNBT()));
		return nbt;
	}
	
	/**
	 * @param world
	 * @return
	 */
	public static ScoreItSavedData get(IWorld world) {
		DimensionSavedDataManager storage = ((ServerWorld)world).getDataStorage();
		ScoreItSavedData data = (ScoreItSavedData) storage.computeIfAbsent(ScoreItSavedData::new, GEN_DATA_KEY);
		
		if (data == null) {
			data = new ScoreItSavedData();
			storage.set(data);
		}
		return data;
	}
}
