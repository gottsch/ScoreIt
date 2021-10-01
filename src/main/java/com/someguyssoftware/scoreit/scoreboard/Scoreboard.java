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
package com.someguyssoftware.scoreit.scoreboard;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.scoreit.ScoreIt;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;


/**
 * 
 * @author Mark Gottschling on Sep 20, 2021
 *
 */
public class Scoreboard {
	public static Logger LOGGER = LogManager.getLogger(ScoreIt.NAME);
	
	public static final int TOP_RANKINGS = 5;
	
	private static final Map<String, PlayerScore> REGISTRY = new HashMap<>();
	private static final String REGISTRY_KEY = "registry";
	private static final String STATE_KEY = "state";
	
	public enum GameState {
		NONE,
		STARTED,
		STOPPED,
		ENDED;
	}
	
	/*
	 * A comparator that sorts on a Player's points in descending order.
	 */
	public static class SortByPoints implements Comparator<PlayerScore> {
		@Override
		public int compare(PlayerScore s1, PlayerScore s2) {
			return s2.getPoints() - s1.getPoints();
		}
	};
	
	private static GameState gameState = GameState.NONE;
	public static Comparator<PlayerScore> sortByPoints = new SortByPoints();
	
	/**
	 * 
	 */
	private Scoreboard() { }
	
	public static boolean start() {
		if (gameState == GameState.NONE || gameState == GameState.STOPPED) {
			gameState = GameState.STARTED;
			return true;
		}
		return false;
	}
	
	public static boolean stop() {
		if (gameState == GameState.STARTED) {
			gameState = GameState.STOPPED;
			return true;
		}
		return false;
	}
	
	public static boolean end() {
		if (gameState == GameState.STARTED || gameState == GameState.STOPPED) {
			gameState = GameState.ENDED;
			// TODO should dump the scores to a text file or something
			return true;
		}
		return false;
	}
	
	public static void reset() {
		gameState = GameState.NONE;
		REGISTRY.clear();
	}
	
	public static boolean isRunning() {
		return gameState == GameState.STARTED;
	}
	
	public static boolean isPaused() {
		return gameState == GameState.STOPPED;
	}
	
	public static boolean isComplete() {
		return gameState == GameState.ENDED;
	}
	
	public static GameState getGameState() {
		return gameState;
	}
	
	public static List<PlayerScore> getScores() {
		return REGISTRY.values().stream().collect(Collectors.toList());
	}
	
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
	 * @param stringUUID
	 * @param pointsValue
	 * @param stack
	 * @return
	 */
	public static Optional<Integer> addPoints(String uuid, int points, ItemStack stack) {
		Optional<PlayerScore> details = getPlayer(uuid);
		if (details.isPresent()) {
			details.get().addPoints(points);
			if (details.get().getItemCounts().containsKey(stack.getItem().getRegistryName())) {
				Integer i = details.get().getItemCounts().get(stack.getItem().getRegistryName());
				details.get().getItemCounts().put(stack.getItem().getRegistryName(), i + stack.getCount());
			}
			else {
				details.get().getItemCounts().put(stack.getItem().getRegistryName(), stack.getCount());
			}
			return Optional.of(details.get().getPoints());
		}
		return Optional.empty();
	}
	
	/**
	 * 
	 * @param nbt
	 */
	 public static void load(CompoundNBT scoreboard) {
		 if (scoreboard.contains(STATE_KEY)) {
			 LOGGER.info("loading state -> {}", GameState.valueOf(scoreboard.getString(STATE_KEY)));
			 gameState = GameState.valueOf(scoreboard.getString(STATE_KEY));
		 }
		 
		 ListNBT scoreList = scoreboard.getList(REGISTRY_KEY, 10);
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
	  * @param scoreboard
	  * @return
	  */
	 public static CompoundNBT save(CompoundNBT scoreboard) {
		 ListNBT scoreList = new ListNBT();
		 REGISTRY.entrySet().forEach(entry -> {
			 LOGGER.info("saving for player -> {}", entry.getValue().getName());
			 // create a compound for player score
			 CompoundNBT scoreNbt = entry.getValue().save(new CompoundNBT());
			 // add to list
			 scoreList.add(scoreNbt);
		 });
		 // TODO any other properties		
		 scoreboard.putString(STATE_KEY, gameState.toString());
		 LOGGER.info("saving state -> {}", gameState.toString());
		 // add list to scoreboard
		 scoreboard.put(REGISTRY_KEY, scoreList);		 
		 return scoreboard;
	 }
}
