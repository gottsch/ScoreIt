package com.someguyssoftware.scoreit.eventhandler;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.someguyssoftware.gottschcore.world.WorldInfo;
import com.someguyssoftware.scoreit.ScoreIt;
import com.someguyssoftware.scoreit.block.ScoreItBlocks;
import com.someguyssoftware.scoreit.persistence.ScoreItSavedData;
import com.someguyssoftware.scoreit.scoreboard.PlayerScore;
import com.someguyssoftware.scoreit.scoreboard.Scoreboard;
import com.someguyssoftware.scoreit.scoreboard.Scoreboard.GameState;
import com.someguyssoftware.scoreit.sound.ScoreItSounds;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * 
 * @author Mark Gottschling on Sep 21, 2021
 *
 */
@Mod.EventBusSubscriber(modid = ScoreIt.MODID, bus = EventBusSubscriber.Bus.FORGE)
public class PlayerEventHandler {
	public static Logger LOGGER = LogManager.getLogger(ScoreIt.NAME);
	
	@SubscribeEvent
	public static void onRightClickItemEvent(PlayerEvent.PlayerLoggedInEvent event) {
		if (WorldInfo.isClientSide(event.getPlayer().level)) {
			return;
		}
		
		// add player to scoreboard registry
		Optional<PlayerScore> score = Scoreboard.getPlayer(event.getPlayer().getStringUUID());
		if (!score.isPresent()) {
			Scoreboard.addPlayer(event.getPlayer().getStringUUID(), event.getPlayer().getName().getString());
		}
	}
	
	@SubscribeEvent
	public static void onRightClickItemEvent(PlayerInteractEvent.RightClickBlock event) {
		if (event.getPlayer().level.isClientSide()) {
			return;
		}

		/*
		 * why is this event being called for both hands, when only one is interacting?
		 */
		ScoreIt.LOGGER.debug("using hand -> {}", event.getHand());
		if (event.getItemStack() == ItemStack.EMPTY || event.getItemStack().getItem() == Items.AIR || event.getHand() != Hand.MAIN_HAND) {
			return;
		}
		
		// get the block
		Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
		ItemStack stack = event.getItemStack();
		// test against the dropbox
		if (ScoreItBlocks.DROPBOX == block) {
			// perform checks on the scoreboard game state
			if (Scoreboard.getGameState() == GameState.STOPPED) {
            			event.getPlayer().sendMessage((new TranslationTextComponent("message.scoreit.game_paused", Scoreboard.getGameState().toString())).withStyle(new TextFormatting[]{TextFormatting.GRAY, TextFormatting.ITALIC}),event.getPlayer().getUUID());
            			return;
			}
			else if (Scoreboard.getGameState() != GameState.STARTED) {
            			event.getPlayer().sendMessage((new TranslationTextComponent("message.scoreit.game_not_started", Scoreboard.getGameState().toString())).withStyle(new TextFormatting[]{TextFormatting.GRAY, TextFormatting.ITALIC}),event.getPlayer().getUUID());
            			return;
			}
			
			LOGGER.debug("using {} on a dropbox", stack.getDisplayName().getString());
			// get all the tags for the itemstack that have the ScoreIt namespace
			Set<String> tags = stack.getItem()
					.getTags().stream()
					.filter(tag -> tag.getNamespace().equals(ScoreIt.MODID))
					.map(ResourceLocation::getPath)
					.collect(Collectors.toSet());

			LOGGER.debug("belongs to # of tags -> {}", tags.size());
			// TODO future check for registered tags

			// default get "*_point" tags (should only belong to one ex. 1_point)
			List<String> pointTags = tags.stream()
					.filter(tag -> tag.endsWith("_point"))
					.collect(Collectors.toList());
			LOGGER.debug("belongs to # of point tags -> {}", pointTags.size());
			
			// get the first item
			if (pointTags.size() > 0) {
				String tag = pointTags.get(0);
				// extract the prefix ie before "_"
				String pointPrefix = tag.replaceAll("_point", "").trim();
				LOGGER.debug("point prefix -> {}", pointPrefix);
				try {
					// convert prefix into Integer
					int pointsValue = Integer.valueOf(pointPrefix) * stack.getCount();

					// add points to players score
					Optional<PlayerScore> score = Scoreboard.getPlayer(event.getPlayer().getStringUUID());
					if (!score.isPresent()) {
						Scoreboard.addPlayer(event.getPlayer().getStringUUID(), event.getPlayer().getName().getString());
					}
					Optional<Integer> playerPoints = Scoreboard.addPoints(event.getPlayer().getStringUUID(), pointsValue, stack);
					if (playerPoints.isPresent()) {
						// play sound
						Random random = new Random();
						event.getWorld().playSound(null, event.getPos(), ScoreItSounds.DEPOSIT_ITEM, SoundCategory.BLOCKS, 1F, random.nextFloat() * 0.1F + 0.9F);
						LOGGER.debug("player now has points -> {}", playerPoints.get());
						// remove stack from hand
						stack.shrink(stack.getCount());
					}
					
					ScoreItSavedData savedData = ScoreItSavedData.get(event.getPlayer().level);
					if (savedData != null) {
						savedData.setDirty();
					}
				}
				catch(Exception e) {
					ScoreIt.LOGGER.warn("unable to award points for tag -> {} to player -> {}", tag, event.getPlayer().getName().getString());
				}
			}
		}
	}
}
