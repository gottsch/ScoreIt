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
package com.someguyssoftware.scoreit.sound;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.someguyssoftware.gottschcore.block.ModBlock;
import com.someguyssoftware.scoreit.ScoreIt;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.audio.Sound;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.registry.Registry;
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
public class ScoreItSounds {
	
	public static SoundEvent DEPOSIT_ITEM;

	@SubscribeEvent
	public static void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		DEPOSIT_ITEM = new SoundEvent(new ResourceLocation(ScoreIt.MODID, "deposit_item")).setRegistryName(new ResourceLocation(ScoreIt.MODID, "deposit_item"));
		
        /*
         * register sounds
         */
		final IForgeRegistry<SoundEvent> registry = event.getRegistry();
		registry.register(DEPOSIT_ITEM);
	}
}
