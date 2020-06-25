package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.ClaimManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Redirect(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    public ActionResult iSaidDontTouchMe(Entity entity, PlayerEntity playerEntity_1, Vec3d vec3d_1, Hand hand_1) {
        Claim claim = ClaimManager.INSTANCE.getClaimAt(entity.getBlockPos(), entity.world.getDimension());
        if (claim != null) {
            if (!claim.hasPermission(playerEntity_1.getGameProfile().getId(), "interact_entity", Registry.ENTITY_TYPE.getId(entity.getType()).getPath())) {
//                playerEntity_1.sendSystemMessage(Messages.MSG_INTERACT_ENTITY);
                return ActionResult.FAIL;
            }
        }
        return entity.interactAt(playerEntity_1, vec3d_1, hand_1);
    }
//
//    @Redirect(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"))
//    private boolean canITouchie(ServerWorld world, PlayerEntity playerEntity_1, BlockPos blockPos_1) {
//        return Functions.canPlayerActuallyModifyAt(world, playerEntity_1, blockPos_1);
//    }
}
