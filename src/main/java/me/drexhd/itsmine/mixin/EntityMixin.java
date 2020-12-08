package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.Functions;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ChatColor;
import me.drexhd.itsmine.util.ClaimUtil;
import me.drexhd.itsmine.util.WorldUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public World world;
    public Vec3d pos;
    private Claim pclaim = null;

    @Shadow
    public abstract Vec3d getPos();

    @Shadow
    public abstract void tick();

    @Inject(method = "setPos", at = @At("HEAD"))
    public void doPrePosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            pos = this.getPos();
            if (player.getBlockPos() == null) return;
            pclaim = ClaimManager.INSTANCE.getClaimAt(player.getBlockPos(), player.world.getDimension());
        }
    }

    @Inject(method = "setPos", at = @At("RETURN"))
    public void doPostPosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            Claim claim = ClaimManager.INSTANCE.getClaimAt(player.getBlockPos(), player.world.getDimension());
            if (pclaim != claim && player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                if (serverPlayerEntity.networkHandler != null) {
                    String message = null;
                    if (pclaim != null && claim == null) {
                        message = getFormattedEventMessage(player, pclaim, false);
                    } else if (claim != null) {
                        if (ClaimUtil.getParentClaim(claim).banManager.isBanned(player.getUuid())) {
                            if (pclaim != null && ClaimUtil.getParentClaim(pclaim).banManager.isBanned(player.getUuid())) {
                                BlockPos loc = ItsMineConfig.main().spawnSection().getBlockPos();
                                serverPlayerEntity.teleport((ServerWorld) WorldUtil.DEFAULT_DIMENSION, loc.getX(), loc.getY(), loc.getZ(), player.yaw,player.pitch);
                            } else {
                                serverPlayerEntity.teleport(pos.x, pos.y, pos.z);
                            }
                            message = "&cYou can't enter this claim, because you've been banned from it.";
                        } else {
                            message = getFormattedEventMessage(player, claim, true);
                            if (claim.flagManager.hasFlag("enter_sound"))
                                serverPlayerEntity.networkHandler.sendPacket(new PlaySoundIdS2CPacket(Registry.SOUND_EVENT.getId(SoundEvents.BLOCK_CONDUIT_ACTIVATE), SoundCategory.MASTER, this.getPos(), 2, 1.2F));
                        }
                    }

                    if (message != null && !message.equals("")) {
                        serverPlayerEntity.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, new LiteralText(ChatColor.translate(message)), -1, ItsMineConfig.main().message().eventStayTicks, -1));
                    }
                }
            }
        }
    }

    private String getFormattedEventMessage(PlayerEntity player, Claim claim, boolean enter) {
        if (player == null || claim == null)
            return "";

        String str = enter ? claim.enterMessage : claim.leaveMessage;
        String claimName = claim.isChild ? ClaimUtil.getParentClaim(claim).name + "." + claim.name : claim.name;
        return ChatColor.translate(str == null ? (enter ? ItsMineConfig.main().message().enterDefault : ItsMineConfig.main().message().leaveDefault) : str).replace("%claim%", claimName)
                .replace("%player%", player.getName().asString());
    }


    @Inject(method = "tick", at = @At("RETURN"))
    public void doTickActions(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            if (player.getBlockPos() == null) {
                return;
            }

            boolean oldAbility = player.getAbilities().allowFlying;
            Claim claim = ClaimManager.INSTANCE.getClaimAt(player.getBlockPos(), player.world.getDimension());

            if (player instanceof ServerPlayerEntity) {
                if (player.getAbilities().allowFlying &&
                        shouldChange(player) &&
                        (!ClaimManager.INSTANCE.flyers.contains(player.getUuid()) ||
                                claim == null ||
                                !claim.hasPermission(player.getGameProfile().getId(), "flight", null) ||
                                !Functions.canFly((ServerPlayerEntity) player))
                ) {
                    player.getAbilities().allowFlying = false;
                    player.getAbilities().flying = false;
                    Functions.setClaimFlying(player.getGameProfile().getId(), false);
/*                    World world = player.getEntityWorld();
                    if (world.getBlockState(player.getBlockPos().down(5)).isAir() && !player.isOnGround()) {
                        BlockPos pos = ClaimUtil.getPosOnGround(player.getBlockPos(), world);
                        player.teleport(pos.getX(), pos.getY(), pos.getZ());
                    }*/
                } else if (
                        !player.getAbilities().allowFlying &&
                                ClaimManager.INSTANCE.flyers.contains(player.getUuid()) &&
                                shouldChange(player) &&
                                claim != null
                                && claim.hasPermission(player.getUuid(), "flight", null)
                                && Functions.canFly((ServerPlayerEntity) player)
                ) {
                    player.getAbilities().allowFlying = true;
                }

                if (player.getAbilities().allowFlying != oldAbility) {
                    player.sendAbilitiesUpdate();
                }
            }

        }
    }

    private boolean shouldChange(PlayerEntity player) {
        return !player.isSpectator() && !player.isCreative();
    }
}
