package io.github.fabricators_of_create.porting_lib.blocks.api.addons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface MinecartPassHandlerBlock {
	void onMinecartPass(BlockState state, Level world, BlockPos pos, AbstractMinecart cart);
}
