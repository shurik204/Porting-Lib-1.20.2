package io.github.fabricators_of_create.porting_lib.blocks.impl.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import io.github.fabricators_of_create.porting_lib.blocks.api.addons.BeaconColorMultiplierBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin {
	@ModifyExpressionValue(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColors()[F"))
	private static float[] modifyBeaconColor(float[] original, Level level, BlockPos beaconPos, BlockState beaconState, BeaconBlockEntity beaconBlockEntity, @Local(index = 7) BlockPos blockPos) {
		BlockState blockState = level.getBlockState(beaconPos);
		if (blockState.getBlock() instanceof BeaconColorMultiplierBlock colorMultiplierBlock)
			return colorMultiplierBlock.getBeaconColorMultiplier(blockState, level, beaconPos, beaconPos);
		return original;
	}
}
