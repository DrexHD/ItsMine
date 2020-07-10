package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.*;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.EntityUtil;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Indigo Amann
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements ClaimShower, ClaimPlayerEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }
    private int messageCooldown = 0;
    private Claim shownClaim = null;
    private BlockPos lastShowPos = null;
    private String showmode = null;

    @Redirect(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interact(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult dontYouDareTouchMe(Entity entity, PlayerEntity playerEntity, Hand hand_1) {
        Claim claim = ClaimManager.INSTANCE.getClaimAt(entity.getBlockPos(), entity.world.getDimension());
        if (claim != null) {
            if (!claim.hasPermission(playerEntity.getGameProfile().getId(), "interact_entity", Registry.ENTITY_TYPE.getId(entity.getType()).getPath())) {
                MessageUtil.sendTranslatableMessage(playerEntity, ItsMineConfig.main().message().interactEntity);
                return ActionResult.FAIL;
            }
        }
        return entity.interact(playerEntity, hand_1);
    }
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    public void hittingIsRude(Entity entity, CallbackInfo ci) {
        PlayerEntity playerEntity_1 = (PlayerEntity)(Object)this;
        Claim claim = ClaimManager.INSTANCE.getClaimAt(entity.getBlockPos(), entity.world.getDimension());

        if (claim != null && !EntityUtil.canDamage(((PlayerEntity) (Object) this).getUuid(), claim, entity)) {
            playerEntity_1.sendSystemMessage(Messages.MSG_DAMAGE_ENTITY, playerEntity_1.getUuid());
            ci.cancel();
        }
    }

    @Override
    public void setMessageCooldown(){
        messageCooldown = ItsMineConfig.main().message().messageCooldown;
    }

    @Override
    public void setMessageCooldown(int cooldown){
        messageCooldown = cooldown;
    }

    @Override
    public void tickMessageCooldown(){
        if(messageCooldown > 0){
            messageCooldown--;
        }
    }

    @Override
    public int getMessageCooldown(){
        return messageCooldown;
    }

    @Override
    public boolean shouldMessage(){
        return messageCooldown == 0;
    }

    @Override
    public void setLastShowPos(BlockPos pos) {
        lastShowPos = pos;
    }
    @Override
    public void setShownClaim(Claim claim) {
        shownClaim = claim;
    }
    @Override
    public Claim getShownClaim() {
        return shownClaim;
    }
    @Override
    public BlockPos getLastShowPos() {
        return lastShowPos;
    }
    @Override
    public void setShowMode(String mode){showmode = mode;}
    @Override
    public String getMode(){return showmode;}

}
