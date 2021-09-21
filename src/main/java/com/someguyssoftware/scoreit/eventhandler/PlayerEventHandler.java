package com.someguyssoftware.scoreit.eventhandler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.someguyssoftware.gottschcore.world.WorldInfo;
import com.someguyssoftware.scoreit.ScoreIt;
import com.someguyssoftware.scoreit.block.ScoreItBlocks;
import com.someguyssoftware.scoreit.leaderboard.Leaderboard;
import com.someguyssoftware.scoreit.leaderboard.PlayerScore;
import com.someguyssoftware.treasure2.Treasure;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
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

	@SubscribeEvent
	public static void onRightClickItemEvent(PlayerInteractEvent.RightClickItem event) {
		if (WorldInfo.isClientSide(event.getPlayer().level)) {
			return;
		}
		
		// get the block
		Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
		ItemStack stack = event.getItemStack();
		// test against the dropbox
		if (ScoreItBlocks.DROPBOX == block) {
			// get all the tags for the itemstack that have the ScoreIt namespace
			Set<String> tags = stack.getItem()
					.getTags().stream()
					.filter(tag -> tag.getNamespace().equals(ScoreIt.MODID))
					.map(ResourceLocation::getPath)
					.collect(Collectors.toSet());

			// TODO future check for registered tags

			// default get "*_point" tags (should only belong to one ex. 1_point)
			List<String> pointTags = tags.stream()
					.filter(tag -> tag.endsWith("_point"))
					.collect(Collectors.toList());

			// get the first item
			if (pointTags.size() > 0) {
				String tag = pointTags.get(0);
				// extract the prefix ie before "_"
				String pointPrefix = tag.replaceAll("_point", "").trim();
				try {
					// convert prefix into Integer
					int pointsValue = Integer.valueOf(pointPrefix) * stack.getCount();

					// add points to players score
					Optional<PlayerScore> score = Leaderboard.getPlayer(event.getPlayer().getStringUUID());
					if (!score.isPresent()) {
						Leaderboard.addPlayer(event.getPlayer().getStringUUID(), event.getPlayer().getName().getString());
					}
					Leaderboard.addPoints(event.getPlayer().getStringUUID(), pointsValue);

					// remove stack from hand
					stack.shrink(stack.getCount());
				}
				catch(Exception e) {
					ScoreIt.LOGGER.warn("unable to award points for tag -> {} to player -> {}", tag, event.getPlayer().getName().getString());
				}
			}
		}
	}


}
