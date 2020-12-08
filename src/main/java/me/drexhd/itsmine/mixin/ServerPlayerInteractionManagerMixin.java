package me.drexhd.itsmine.mixin;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.BlockUtil;
import me.drexhd.itsmine.util.ItemUtil;
import me.drexhd.itsmine.util.MessageUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;


/**
 * @author Indigo Amann
 */
@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {
    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public ServerWorld world;

    public BlockPos blockPos;

    public BlockHitResult rayTrace(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        float f = player.pitch;
        float g = player.yaw;
        Vec3d vec3d = player.getCameraPosVec(1.0F);
        float h = MathHelper.cos(-g * 0.017453292F - 3.1415927F);
        float i = MathHelper.sin(-g * 0.017453292F - 3.1415927F);
        float j = -MathHelper.cos(-f * 0.017453292F);
        float k = MathHelper.sin(-f * 0.017453292F);
        float l = i * j;
        float n = h * j;
        double d = 5.0D;
        Vec3d vec3d2 = vec3d.add((double) l * 5.0D, (double) k * 5.0D, (double) n * 5.0D);
        return world.raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.OUTLINE, fluidHandling, player));
    }

    //This method injects at the beginning of the method to get the block position
    @Inject(method = "interactBlock", at = @At(value = "HEAD"))
    private void getBlock(ServerPlayerEntity serverPlayerEntity, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        blockPos = hitResult.getBlockPos().offset(hitResult.getSide());
    }

    //Check whether or not the player can interact with the block he is clicking
    @Redirect(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onUse(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;"))
    private ActionResult interactIfPossible(BlockState blockState, World world, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        Claim claim = ClaimManager.INSTANCE.getClaimAt(pos, player.world.getDimension());
        if (claim != null) {
            UUID uuid = player.getUuid();
            Block block = blockState.getBlock();
            if (BlockUtil.isInteractAble(block)) {
                if (claim.isPermissionDenied(uuid, "interact_block", Registry.BLOCK.getId(block).getPath())) {
                    MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().interactBlock);
                    return ActionResult.FAIL;
                } else {
                    if (claim.hasPermission(uuid, "interact_block", Registry.BLOCK.getId(block).getPath()) ||
                            (BlockUtil.isButton(block) && claim.hasPermission(uuid, "interact_block", "BUTTONS")) ||
                            (BlockUtil.isTrapdoor(block) && claim.hasPermission(uuid, "interact_block", "TRAPDOORS")) ||
                            (BlockUtil.isDoor(block) && claim.hasPermission(uuid, "interact_block", "DOORS")) ||
                            (BlockUtil.isContainer(block) && claim.hasPermission(uuid, "interact_block", "CONTAINERS")) ||
                            (BlockUtil.isSign(block) && claim.hasPermission(uuid, "interact_block", "SIGNS")) ||
                            (BlockUtil.isGateWay(block) && claim.hasPermission(uuid, "interact_block", "GATEWAYS")) ||
                            (BlockUtil.isShulkerBox(block) && claim.hasPermission(uuid, "interact_block", "SHULKERBOXES"))) {
                        return blockState.onUse(world, player, hand, hit);
                    } else {
                        MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().interactBlock);
                        return ActionResult.FAIL;
                    }
                }
            }
        }
        return blockState.onUse(world, player, hand, hit);
    }

    //Check whether or not the player is allowed to use the item in his hand
    //returning false means the interaction will pass
    @Redirect(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", ordinal = 2))
    private boolean interactWithItemIfPossible(ItemStack stack) {
        Pair<BlockPos, BlockPos> posPair = ClaimManager.INSTANCE.stickPositions.get(player);
        if ((stack.getItem() == Items.STICK && !player.isSneaking()) || (stack.getItem() == Items.AIR && player.isSneaking())) {
            if (posPair != null) {
                posPair = new Pair<>(blockPos, posPair.getRight());
                ClaimManager.INSTANCE.stickPositions.put(player, posPair);
                player.sendSystemMessage(new LiteralText("Position #2 set: " + blockPos.getX() + (ItsMineConfig.main().claims2d ? "" : " " + blockPos.getY()) + " " + blockPos.getZ()).formatted(Formatting.GREEN), player.getUuid());
                if (posPair.getRight() != null) {
                    player.sendSystemMessage(new LiteralText("Area Selected. Type /claim create <name> to create your claim!").formatted(Formatting.GOLD), player.getUuid());
                    if (!ItsMineConfig.main().claims2d)
                        player.sendSystemMessage(new LiteralText("Remember that claims are three dimensional. Don't forget to expand up/down or select a big enough area...").formatted(Formatting.LIGHT_PURPLE).formatted(Formatting.ITALIC), player.getUuid());
                }
            }
        }
        Claim claim = ClaimManager.INSTANCE.getClaimAt(blockPos, world.getDimension());
        if (claim != null && !stack.isEmpty()) {
            Item item = stack.getItem();
            UUID uuid = player.getUuid();
            if (item instanceof BlockItem || item instanceof BucketItem) {
                if ((claim.hasPermission(uuid, "build", null)) || claim.hasPermission(uuid, "place", Registry.ITEM.getId(item).getPath())) {
                    return false;
                } else {
                    MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().placeBlock);
                    return true;
                }
            } else {
                if (claim.isPermissionDenied(player.getUuid(), "use_item", Registry.ITEM.getId(item).getPath())) {
                    MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().useItem);
                    return true;
                } else {
                    if (claim.hasPermission(player.getUuid(), "use_item", Registry.ITEM.getId(item).getPath()) || (ItemUtil.isFood(item) && claim.hasPermission(player.getUuid(), "use_item", "FOODS")) ||
                            (ItemUtil.isBoat(item) && claim.hasPermission(player.getUuid(), "use_item", "BOATS"))) {
                        return false;
                    } else {
                        MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().useItem);
                        return true;
                    }
                }
            }
        }

        return stack.isEmpty();
    }

    //Mixin to check for left click of stick (for selecting claim) and to check block breaking
    @Redirect(method = "processBlockBreakingAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"))
    public boolean canBreak(ServerWorld world, PlayerEntity player, BlockPos pos) {
        Pair<BlockPos, BlockPos> posPair = ClaimManager.INSTANCE.stickPositions.get(player);
        if ((player.getInventory().getMainHandStack().getItem() == Items.STICK && !player.isSneaking()) || (player.getInventory().getMainHandStack().getItem() == Items.AIR) && player.isSneaking()) {
            if (posPair != null) {
                posPair = new Pair<>(posPair.getLeft(), pos);
                ClaimManager.INSTANCE.stickPositions.put(player, posPair);
                player.sendSystemMessage(new LiteralText("Position #1 set: " + pos.getX() + (ItsMineConfig.main().claims2d ? "" : " " + pos.getY()) + " " + pos.getZ()).formatted(Formatting.GREEN), player.getUuid());
                if (posPair.getLeft() != null) {
                    player.sendSystemMessage(new LiteralText("Area Selected. Type /claim create <name> to create your claim!").formatted(Formatting.GOLD), player.getUuid());
                    if (!ItsMineConfig.main().claims2d)
                        player.sendSystemMessage(new LiteralText("Remember that claims are three dimensional. Don't forget to expand up/down or select a big enough area...").formatted(Formatting.LIGHT_PURPLE).formatted(Formatting.ITALIC), player.getUuid());
                }
                return false;
            }
        }
        Claim claim = ClaimManager.INSTANCE.getClaimAt(pos, player.world.getDimension());
        String block = Registry.BLOCK.getId(player.getEntityWorld().getBlockState(pos).getBlock()).getPath();
        if (claim != null) {
            if (claim.hasPermission(player.getUuid(), "build", null) || claim.hasPermission(player.getUuid(), "break", block)) {
                return true;
            } else {
                MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().breakBlock);
                return false;
            }
        }
        return world.canPlayerModifyAt(player, pos);
    }

    @Redirect(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> canUseItem(ItemStack itemStack, World world, PlayerEntity player, Hand hand) {
        Item item = itemStack.getItem();
        BlockPos pos = player.getBlockPos();
        if (ItemUtil.isBoat(item)) {
            HitResult hitResult = rayTrace(world, player, RaycastContext.FluidHandling.ANY);
            pos = new BlockPos(hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ());
        }
        Claim claim = ClaimManager.INSTANCE.getClaimAt(pos, world.getDimension());
        if (claim != null) {
            if (claim.isPermissionDenied(player.getUuid(), "use_item", Registry.ITEM.getId(item).getPath())) {
                MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().useItem);
                return TypedActionResult.fail(itemStack);
            } else {
                if (claim.hasPermission(player.getUuid(), "use_item", Registry.ITEM.getId(item).getPath()) || (ItemUtil.isFood(item) && claim.hasPermission(player.getUuid(), "use_item", "FOODS")) ||
                        (ItemUtil.isBoat(item) && claim.hasPermission(player.getUuid(), "use_item", "BOATS"))) {
                    return itemStack.use(world, player, hand);
                } else {
                    MessageUtil.sendTranslatableMessage(player, ItsMineConfig.main().message().useItem);
                    return TypedActionResult.fail(itemStack);
                }
            }
        }
        return itemStack.use(world, player, hand);
    }

}
