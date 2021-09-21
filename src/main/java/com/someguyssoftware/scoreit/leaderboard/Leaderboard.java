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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;


/**
 * 
 * @author Mark Gottschling on Sep 20, 2021
 *
 */
public class Leaderboard {
	private static final String REGISTRY_KEY = "registry";
	
	private static final Map<String, PlayerScore> REGISTRY = new HashMap<>();
	
	// TODO is any state required? 
	
	/*
	 * A comparator that sorts on a Player's points in descending order.
	 */
	public static class SortByPoints implements Comparator<PlayerScore> {
		@Override
		public int compare(PlayerScore s1, PlayerScore s2) {
			return s2.getPoints() - s1.getPoints();
		}
	};
	
	/**
	 * 
	 */
	private Leaderboard() { }
	
	public static void addPlayer(String uuid, String name) {
		// creates new PlayerDetails and adds to the registry
		if (!REGISTRY.containsKey(uuid)) {
			REGISTRY.put(uuid, new PlayerScore(uuid, name));
		}
		// TODO resort list
	}
	
	public static void addPlayer(String uuid, PlayerScore details) {
		if (!REGISTRY.containsKey(uuid)) {
			REGISTRY.put(uuid, details);
		}
		// TDOO resort list
	}
	
	public static Optional<PlayerScore> removePlayer(String uuid) {
		Optional<PlayerScore> details = Optional.ofNullable(REGISTRY.remove(uuid));
		// TODO resort list
		
		return details;
	}
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public static Optional<PlayerScore> getPlayer(String uuid) {
		return Optional.ofNullable(REGISTRY.get(uuid));
	}
	
	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public static boolean hasPlayer(String uuid) {
		return REGISTRY.containsKey(uuid);
	}
	
	/**
	 * 
	 * @param uuid
	 * @param points
	 * @return
	 */
	public static Optional<Integer> addPoints(String uuid, int points) {
		Optional<PlayerScore> details = getPlayer(uuid);
		if (details.isPresent()) {
			details.get().addPoints(points);
			return Optional.of(details.get().getPoints());
		}
		return Optional.empty();
	}
	
	/**
	 * 
	 * @param nbt
	 */
	 public static void load(CompoundNBT leaderboard) {
		 ListNBT scoreList = leaderboard.getList(REGISTRY_KEY, 10);
		 scoreList.forEach(entry -> {
			 CompoundNBT scoreNbt = (CompoundNBT)entry;
			 // load a player score
			 Optional<PlayerScore> playerScore = PlayerScore.load(scoreNbt);
			 // add the player score to the leader registry
			 if (playerScore.isPresent()) {
				 addPlayer(playerScore.get().getUuid(), playerScore.get());
			 }
			 // TODO any other processing like, sorting the leader list
		 });
	 }
	 
	 /**
	  * 
	  * @param leaderboard
	  * @return
	  */
	 public static CompoundNBT save(CompoundNBT leaderboard) {
		 ListNBT scoreList = new ListNBT();
		 REGISTRY.entrySet().forEach(entry -> {
			 // create a compound for player score
			 CompoundNBT scoreNbt = entry.getValue().save(new CompoundNBT());
			 // add to list
			 scoreList.add(scoreNbt);
		 });
		 // TODO any other properties		 
		 
		 // add list to leaderboard
		 leaderboard.put(REGISTRY_KEY, scoreList);		 
		 return leaderboard;
	 }
}
