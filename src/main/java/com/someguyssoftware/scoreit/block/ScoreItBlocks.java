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
package com.someguyssoftware.scoreit.block;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.someguyssoftware.gottschcore.block.ModBlock;
import com.someguyssoftware.scoreit.ScoreIt;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 
 * @author Mark Gottschling on Sep 21, 2021
 *
 */
@Mod.EventBusSubscriber(modid = ScoreIt.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ScoreItBlocks {
	public static Block DROPBOX;

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		DROPBOX = new ModBlock(ScoreIt.MODID, "dropbox", Block.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2.5F));
		
        /*
         * register blocks
         */
		final IForgeRegistry<Block> registry = event.getRegistry();
		registry.register(DROPBOX);
	}
	
	/**
	 * Register this mod's {@link ItemBlock}s.
	 *
	 * @param event The event
	 */
	@SubscribeEvent
	public static void registerItemBlocks(final RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		
		List<Block> blocks = new ArrayList<>(3);
		
		blocks.add(DROPBOX);
		
		for (Block b : blocks) {
			BlockItem blockItem = new BlockItem(b, new Item.Properties());
			final ResourceLocation registryName = Preconditions.checkNotNull(b.getRegistryName(),
					"Block %s has null registry name", b);
			registry.register(blockItem.setRegistryName(registryName));
		}
	}
}
