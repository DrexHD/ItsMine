package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.LilyPadItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(BucketItem.class)
public class PlaceOnUseBlockMixin extends Item {
    public PlaceOnUseBlockMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean canModify(World world, PlayerEntity player, BlockPos pos) {
        if (!(world instanceof ServerWorld)) return world.canPlayerModifyAt(player, pos);
        Claim claim = ClaimManager.INSTANCE.getClaimAt(pos, player.world.getDimension());
        if (claim != null && !claim.hasPermission(player.getUuid(), "build")) {
            if ((Object) this instanceof LilyPadItem){
                ((ServerWorld)world).getChunkManager().markForUpdate(pos);
            }
            return false;
        }
        return world.canPlayerModifyAt(player, pos);
    }
}
