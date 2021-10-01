/*
 * This file is part of  ScoreIt2.
 * Copyright (c) 2021, Mark Gottschling (gottsch)
 * 
 * All rights reserved.
 *
 * ScoreIt2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScoreIt2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ScoreIt2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.someguyssoftware.scoreit.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.someguyssoftware.scoreit.ScoreIt;
import com.someguyssoftware.scoreit.persistence.ScoreItSavedData;
import com.someguyssoftware.scoreit.scoreboard.PlayerScore;
import com.someguyssoftware.scoreit.scoreboard.Scoreboard;
import com.someguyssoftware.treasure2.ScoreIt;
import com.someguyssoftware.treasure2.world.gen.structure.TemplateHolder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 
 * @author Mark Gottschling on Sep 15, 2021
 *
 */
public class ScoreItCommand {
	public static Logger LOGGER = LogManager.getLogger(ScoreIt.NAME);

	/**
	 * 
	 * @param dispatcher
	 */
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher
		.register(Commands.literal("scoreit")
				.requires(source -> {
					return source.hasPermission(4);
				})
				.then(Commands.literal("start")
						.executes(source -> {
							return start(source.getSource());
						})
						)
				.then(Commands.literal("stop")
						.executes(source -> {
							return stop(source.getSource());
						})
						)
				.then(Commands.literal("end")
						.executes(source -> {
							return end(source.getSource());
						})	
						)
				.then(Commands.literal("reset")
						.executes(source -> {
							return reset(source.getSource());
						})	
						)		
				.then(Commands.literal("scores")
						.requires(source -> {
							return source.hasPermission(4);
						})
						.executes(source -> {
							return scores(source.getSource());
						})	
						)						
				);
	}

	/**
	 * 
	 * @param source
	 * @return
	 */
	private static int start(CommandSource source) {
		if (Scoreboard.start()) {
			// message player that state has changed
			source.sendSuccess(new TranslationTextComponent("command.scoreit.start.success"), true);
			saveData(source);
		} else {
			source.sendSuccess(new TranslationTextComponent("command.scoreit.start.failure", Scoreboard.getGameState().toString()), true);
		}

		return 1;
	}

	private static int stop(CommandSource source) {
		if (Scoreboard.stop()) {
			source.sendSuccess(new TranslationTextComponent("command.scoreit.stop.success"), true);
			saveData(source);
		}
		else {
			source.sendSuccess(new TranslationTextComponent("command.scoreit.stop.failure", Scoreboard.getGameState().toString()), true);
		}
		return 1;
	}

	private static int end(CommandSource source) {
		if (Scoreboard.end()) {
			source.sendSuccess(new TranslationTextComponent("command.scoreit.end.success"), true);
			saveData(source);
			// TODO message all with final scores
			
			// dump the score to a file
			try {
				dump(getRankedTopScores(Optional.empty(), Optional.ofNullable(source.getPlayerOrException())));
			}
			catch(Exception e) {
				LOGGER.error("Unable to dump() scores -> ", e);
			}
		} else {
			source.sendSuccess(new TranslationTextComponent("command.scoreit.end.failure", Scoreboard.getGameState().toString()), true);
		}
		return 1;
	}

	private static int reset(CommandSource source) {
		Scoreboard.reset();
		source.sendSuccess(new TranslationTextComponent("command.scoreit.reset"), true);
		saveData(source);
		return 1;
	}

	/**
	 * TODO update to take the number of top rankings as a command param
	 * @param source
	 * @return
	 */
	private static int scores(CommandSource source) {
		try {
			List<Tuple<Integer, PlayerScore>> tuple = getRankedTopScores(Optional.of(Scoreboard.TOP_RANKINGS), Optional.ofNullable(source.getPlayerOrException()));
			List<ITextComponent> scoreMessages = formatScores(tuple, Optional.ofNullable(source.getPlayerOrException()));
			sendScores(scoreMessages, Optional.ofNullable(source.getPlayerOrException()));
			
			List<PlayerScore> scores = Scoreboard.getScores();
			if (!scores.isEmpty()) {
				Collections.sort(scores, Scoreboard.sortByPoints);
				int rank = 1;
				boolean playerIsTopRanked = false;
				for (PlayerScore score : scores) {
					LOGGER.info("player score -> {}", score);
					if (rank <= 5) {
						TranslationTextComponent text = new TranslationTextComponent("command.scoreit.score", rank++, score.getName(), String.valueOf(score.getPoints()));
						if (score.getUuid().equals(source.getPlayerOrException().getStringUUID())) {
							text.withStyle(TextFormatting.BOLD, TextFormatting.GOLD);
							playerIsTopRanked = true;
						}
						source.sendSuccess(text, true);
					}
					else {
						if (!playerIsTopRanked) {
							if (score.getUuid().equals(source.getPlayerOrException().getStringUUID())) {
								source.sendSuccess(new TranslationTextComponent("command.scoreit.score", rank, score.getName(), String.valueOf(score.getPoints())), true);
								break;
							}
							rank++;
						}
						else {
							break;
						}
					}
				}
			}
			else {
				/*
				 *  this condition shouldn't happen unless the Scoring is reset while players are on the server
				 */
				source.sendSuccess(new TranslationTextComponent("command.scoreit.score", "0", source.getTextName(), "0"), true);				
			}
		}
		catch(Exception e) {
			LOGGER.error("Unable to complete scores command -> ", e);
		}
		return 1;
	}

	private static void sendScores(List<ITextComponent> scoreMessages, Optional<ServerPlayerEntity> ofNullable) {
		// TODO Auto-generated method stub
		
	}

	private static List<ITextComponent> formatScores(List<Tuple<Integer, PlayerScore>> tuple,
			Optional<ServerPlayerEntity> ofNullable) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param numRankings
	 * @param player
	 * @return
	 */
	private static List<Tuple<Integer, PlayerScore>> getRankedTopScores(final Optional<Integer> numRankings,
			final Optional<ServerPlayerEntity> player) {
		
		List<Tuple<Integer, PlayerScore>> rankedScores = new ArrayList<>();

		// TODO if topRankings is not present then get all rankings
		
		List<PlayerScore> scores = Scoreboard.getScores();
		
		if (!scores.isEmpty()) {
			Collections.sort(scores, Scoreboard.sortByPoints);
			int maxRanks = numRankings.isPresent() ? numRankings.get() : scores.size(); 
			int currentRank = 1;
			boolean playerIsTopRanked = false;
			for (PlayerScore score : scores) {
				LOGGER.info("player score -> {}", score);
				if (currentRank <= maxRanks) {
					// add the rank and score to the tuple
					
					if (player.isPresent() && score.getUuid().equals(player.get().getStringUUID())) {
						playerIsTopRanked = true;
					}
					source.sendSuccess(text, true);
				}
			}
		}
		
		return rankedScores;
	}

	/**
	 * 
	 * @param source
	 */
	private static void saveData(CommandSource source) {
		// save world data
		ServerWorld world = source.getLevel();
		ScoreItSavedData savedData = ScoreItSavedData.get(world);
		if (savedData != null) {
			LOGGER.info("saving ScoreIt data after issuing command...");
			savedData.setDirty();
		}
	}
	
	/**
	 * 
	 */
	private static void dump(List<Tuple<Integer, PlayerScore>> scores) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyymmdd");

		String filename = String.format("scoreit-scores-%s.txt", formatter.format(new Date()));

		Path path = Paths.get("config", ScoreIt.MODID, "dumps").toAbsolutePath();
		try {
			Files.createDirectories(path);
		} catch (IOException e) {
			ScoreIt.LOGGER.error("Couldn't create directories for dump files:", e);
			return;
		}

		// setup a divider line
		char[] chars = new char[75];
		Arrays.fill(chars, '*');
		String div = new String(chars) + "\n";
		String format = "**    %1$-33s: %2$-30s  **\n";
		String format2 = "**    %1$-15s: %2$-15s: %3$-33s  **\n";
		String heading = "**  %1$-67s  **\n";
		String format3 = "**  %1$-2s) %2$-25s - %3$-35s Points  **\n";
		
		StringBuilder sb = new StringBuilder();
		sb.append(div).append(String.format(heading, "SCORES")).append(div);
		
		scores.forEach(tuple -> {
			sb.append(String.format(format3,"TODO"));
		});

		try {
			Files.write(Paths.get(path.toString(), filename), sb.toString().getBytes());
		} catch (IOException e) {
			ScoreIt.LOGGER.error("Error writing ScoreItTemplateManager to dump file", e);
		}
	}

}
