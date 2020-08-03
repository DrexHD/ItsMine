package me.drexhd.itsmine.mixin.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

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

//    @Redirect(method = "onCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"))
//    private void onCollision$imInvincible(ProjectileEntity projectileEntity, EntityHitResult entityHitResult) {
//        Vec3d vec3d = entityHitResult.getPos();
//        Entity entity = entityHitResult.getEntity();
//        BlockPos pos = new BlockPos(vec3d.x, vec3d.y, vec3d.z);
//        Claim claim = null;
//        if (entity != null && entity.getEntityWorld() != null) {
//            DimensionType dimension = entity.getEntityWorld().getDimension();
//            claim = ClaimManager.INSTANCE.getClaimAt(pos, dimension);
//        }
//
//        if (this.getOwner() == null || claim == null) {
//            this.onEntityHit(entityHitResult);
//        } else if (this.getOwner().getUuid() != null &&
//                EntityUtil.canDamage(this.getOwner().getUuid(), claim, entity)) {
//            this.onEntityHit(entityHitResult);
//        } else {
//            if (this.getOwner() instanceof ServerPlayerEntity) {
//                MessageUtil.sendTranslatableMessage((ServerPlayerEntity) this.getOwner(), ItsMineConfig.main().message().attackEntity);
//                if (projectileEntity.getType() == EntityType.ARROW) {
//                    projectileEntity.kill();
//                }
//            }
//        }
//    }
}
