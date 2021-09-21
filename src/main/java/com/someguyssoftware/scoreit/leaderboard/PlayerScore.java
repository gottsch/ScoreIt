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
package com.someguyssoftware.scoreit.leaderboard;

import java.util.Map;
import java.util.Optional;

import com.someguyssoftware.scoreit.ScoreIt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;

/**
 * 
 * @author Mark Gottschling on Sep 20, 2021
 *
 */
public class PlayerScore {

	private static final String UUID_KEY = "uuid";
	private static final String NAME_KEY = "name";
	private static final String POINTS_KEY = "points";
	private static final String STACK_KEY = "stack";
	
	private String uuid;
	private String name;
	private int points;
	private Map<ResourceLocation, ItemStack> itemCounts;

	public PlayerScore(String uuid) {

	}

	public PlayerScore(String uuid, String name) {

	}

	/**
	 * 
	 * @param nbt
	 */
	public static Optional<PlayerScore> load(CompoundNBT nbt) {
		Optional<PlayerScore> optionalScore = Optional.empty();
		try {
			String uuid = nbt.getString(UUID_KEY);
			// TODO finish
			PlayerScore score = new PlayerScore(uuid);
			optionalScore = Optional.of(score);
		}
		catch(Exception e) {
			ScoreIt.LOGGER.error("Unable to read state to NBT:", e);
		}
		return optionalScore;
	}

	/**
	 * 
	 * @param nbt
	 * @return
	 */
	public CompoundNBT save(CompoundNBT nbt) {
		nbt.putString(UUID_KEY, getUuid());
		nbt.putString(NAME_KEY, getName());
		nbt.putInt(POINTS_KEY, getPoints());
		ListNBT countList = new ListNBT();
		getItemCounts().forEach((resource, stack) -> {
			CompoundNBT count = new CompoundNBT();
			count.putString(NAME_KEY, resource.toString());
			CompoundNBT stackNbt = stack.save(new CompoundNBT());
			count.put(STACK_KEY, stackNbt);
			countList.add(count);
		});
		
		return null;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public Map<ResourceLocation, ItemStack> getItemCounts() {
		return itemCounts;
	}

	public void setItemCounts(Map<ResourceLocation, ItemStack> itemCounts) {
		this.itemCounts = itemCounts;
	}

	public void addPoints(int points) {
		this.points += points;		
	}
}
