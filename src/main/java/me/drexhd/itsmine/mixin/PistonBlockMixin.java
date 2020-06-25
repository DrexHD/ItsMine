package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.block.BlockState;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * @author Indigo Amann
 */
@Mixin(PistonBlock.class)
public class PistonBlockMixin {
    private PistonHandler handler;

    @Inject(method = "isMovable", at = @At("HEAD"), cancellable = true)
    private static void youCantMoveMe(BlockState blockState_1, World world, BlockPos newPos, Direction direction, boolean boolean_1, Direction direction_2, CallbackInfoReturnable<Boolean> ci) {
        BlockPos oldPos = newPos.offset(direction_2.getOpposite());
        Claim oldClaim = ClaimManager.INSTANCE.getClaimAt(oldPos, world.getDimension());
        Claim newClaim = ClaimManager.INSTANCE.getClaimAt(newPos, world.getDimension());
        if (oldClaim != newClaim) {
            if (oldClaim == null || newClaim == null) {
                ci.setReturnValue(false);
            }
        }
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/piston/PistonHandler;getMovedBlocks()Ljava/util/List;"))
    public List<BlockPos> saveDatHandler(PistonHandler pistonHandler) {
        handler = pistonHandler;
        return pistonHandler.getMovedBlocks();
    }
}
