package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FrostWalkerEnchantment.class)
public class FrostWalkerEnchantmentMixin {

    @Redirect(method = "freezeWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;canPlaceAt(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private static boolean dontFreezeMyWater(BlockState blockState, WorldView world, BlockPos pos) {
        Claim claim = ClaimManager.INSTANCE.getClaimAt(pos, world.getDimension());
        if (claim != null) {
            return claim.flagManager.hasFlag("frost_walker");
        }
        return true;
    }
}
