package io.github.fabricators_of_create.porting_lib.blocks.impl.mixin;

import io.github.fabricators_of_create.porting_lib.blocks.api.addons.CustomExpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DropExperienceBlock.class)
public abstract class DropExperienceBlockMixin implements CustomExpBlock {
	@Shadow
	@Final
	private IntProvider xpRange;

	@Override
	public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, net.minecraft.util.RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
		return silkTouchLevel == 0 ? this.xpRange.sample(randomSource) : 0;
	}
}
