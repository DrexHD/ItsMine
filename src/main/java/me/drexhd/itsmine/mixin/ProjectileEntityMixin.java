package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.Messages;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author Indigo Amann
 */
@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {
    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public boolean imInvincible(Entity entity, DamageSource damageSource_1, float float_1) {
        ProjectileEntity projectile = (ProjectileEntity)(Object)this;

        if (((ProjectileEntity)(Object)this).getServer().getPlayerManager().getPlayer(((OwnedProjectile)projectile).getOwner()) != null) {
            PlayerEntity playerEntity_1 = ((ProjectileEntity)(Object)this).getServer().getPlayerManager().getPlayer(((OwnedProjectile)projectile).getOwner());
            Claim claim = ClaimManager.INSTANCE.getClaimAt(entity.getBlockPos(), entity.world.getDimension());
            if (claim != null && entity != playerEntity_1) {
                if (!claim.hasPermission(playerEntity_1.getGameProfile().getId(), "damage_entity", Registry.ENTITY_TYPE.getId(entity.getType()).getPath())) {
                    playerEntity_1.sendSystemMessage(Messages.MSG_DAMAGE_ENTITY, playerEntity_1.getUuid());
                    projectile.kill(); // You do not want an arrow bouncing between two armor stands
                    return false;
                }
            }
        }
        return entity.damage(damageSource_1, float_1);
    }
}
