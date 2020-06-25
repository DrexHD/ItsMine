package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Indigo Amann
 */
@Mixin(FlowableFluid.class)
public class FluidMixin {
    @Inject(method = "flow", at = @At("HEAD"), cancellable = true)
    private void dontFlow(WorldAccess world, BlockPos newPos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        BlockPos oldPos = newPos.offset(direction.getOpposite());
        Claim oldClaim = ClaimManager.INSTANCE.getClaimAt(oldPos, world.getDimension());
        Claim newClaim = ClaimManager.INSTANCE.getClaimAt(newPos, world.getDimension());
        if (oldClaim != newClaim) {
            if (oldClaim == null) {
                if (!newClaim.flagManager.hasFlag("fluid_crosses_borders")) ci.cancel();
            } else if (newClaim == null) {
                if (!oldClaim.flagManager.hasFlag("fluid_crosses_borders")) ci.cancel();
            } else {
                if (!oldClaim.flagManager.hasFlag("fluid_crosses_borders") ||
                        !newClaim.flagManager.hasFlag("fluid_crosses_borders")) ci.cancel();
            }
        }
    }
}
