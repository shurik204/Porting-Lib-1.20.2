package io.github.fabricators_of_create.porting_lib.util;

import io.github.fabricators_of_create.porting_lib.entity.MultiPartEntity;
import io.github.fabricators_of_create.porting_lib.entity.PartEntity;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.extensions.BlockItemExtensions;
import io.github.fabricators_of_create.porting_lib.extensions.LevelExtensions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.registry.RegistryEntryRemovedCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.HashMap;
import java.util.Map;

public class PortingHooks {
	public static int onBlockBreakEvent(Level world, GameType gameType, ServerPlayer entityPlayer, BlockPos pos) {
		// Logic from tryHarvestBlock for pre-canceling the event
		boolean preCancelEvent = false;
		ItemStack itemstack = entityPlayer.getMainHandItem();
		if (!itemstack.isEmpty() && !itemstack.getItem().canAttackBlock(world.getBlockState(pos), world, pos, entityPlayer)) {
			preCancelEvent = true;
		}

		if (gameType.isBlockPlacingRestricted()) {
			if (gameType == GameType.SPECTATOR)
				preCancelEvent = true;

			if (!entityPlayer.mayBuild()) {
				if (itemstack.isEmpty() || !itemstack.hasAdventureModeBreakTagForBlock(world.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY), new BlockInWorld(world, pos, false)))
					preCancelEvent = true;
			}
		}

		// Tell client the block is gone immediately then process events
		if (world.getBlockEntity(pos) == null) {
			entityPlayer.connection.send(new ClientboundBlockUpdatePacket(pos, world.getFluidState(pos).createLegacyBlock()));
		}

		// Post the block break event
		BlockState state = world.getBlockState(pos);
		BlockEvents.BreakEvent event = new BlockEvents.BreakEvent(world, pos, state, entityPlayer);
		event.setCanceled(preCancelEvent);
		event.sendEvent();

		// Handle if the event is canceled
		if (event.isCanceled()) {
			// Let the client know the block still exists
			entityPlayer.connection.send(new ClientboundBlockUpdatePacket(world, pos));

			// Update any tile entity data for this block
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity != null) {
				Packet<?> pkt = blockEntity.getUpdatePacket();
				if (pkt != null) {
					entityPlayer.connection.send(pkt);
				}
			}
		}
		return event.isCanceled() ? -1 : event.getExpToDrop();
	}

	public static void init() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof MultiPartEntity partEntity && partEntity.isMultipartEntity()) {
				PartEntity<?>[] parts = partEntity.getParts();
				if (parts != null) {
					for (PartEntity<?> part : parts) {
						((LevelExtensions) world).getPartEntityMap().put(part.getId(), part);
					}
				}
			}
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if(entity instanceof MultiPartEntity partEntity && partEntity.isMultipartEntity()) {
				PartEntity<?>[] parts = partEntity.getParts();
				if (parts != null) {
					for (PartEntity<?> part : parts) {
						((LevelExtensions) world).getPartEntityMap().remove(part.getId());
					}
				}
			}
		});
		RegistryEntryRemovedCallback.event(Registry.ITEM).register((rawId, id, item) -> {
			if (item instanceof BlockItemExtensions blockItem) {
				blockItem.removeFromBlockToItemMap(Item.BY_BLOCK, item);
			}
		});
	}
}
