package io.github.fabricators_of_create.porting_lib.blocks.impl.mixin;

import io.github.fabricators_of_create.porting_lib.blocks.api.addons.CustomExpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin implements CustomExpBlock {
	@Override
	public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
		return silkTouchLevel == 0 ? 5 : 0;
	}
}
