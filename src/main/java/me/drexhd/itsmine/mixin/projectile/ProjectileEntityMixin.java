package me.drexhd.itsmine.mixin.projectile;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.EntityUtil;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo AmannS
 */
@Mixin(ProjectileEntity.class)
public abstract class ProjectileEntityMixin {
    @Shadow
    protected abstract void onEntityHit(EntityHitResult entityHitResult);

    @Shadow
    @Nullable
    public abstract Entity getOwner();

    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"))
    private void onHit$imInvincible(ProjectileEntity projectileEntity, EntityHitResult entityHitResult) {
        Vec3d vec3d = entityHitResult.getPos();
        Entity entity = entityHitResult.getEntity();
        Entity owner = this.getOwner();
        if (owner instanceof ServerPlayerEntity && vec3d != null) {
            ServerPlayerEntity player = (ServerPlayerEntity) owner;
            BlockPos pos = new BlockPos(vec3d.x, vec3d.y, vec3d.z);
            DimensionType dimension = player.getServerWorld().getDimension();
            Claim claim = ClaimManager.INSTANCE.getClaimAt(pos, dimension);
            if (claim != null) {
                if (EntityUtil.canDamage(player.getUuid(), claim, entity)) {
                    this.onEntityHit(entityHitResult);
                    return;
                } else {
                    MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().attackEntity);
                    if (projectileEntity.getType() == EntityType.ARROW) {
                        projectileEntity.kill();
                        return;
                    }
                }
            }
        }
        this.onEntityHit(entityHitResult);
    }


}
