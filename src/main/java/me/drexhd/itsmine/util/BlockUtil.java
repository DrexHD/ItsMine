package me.drexhd.itsmine.util;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class BlockUtil {

    public static boolean isContainer(Block block) {
        return block instanceof AbstractChestBlock<?> ||
                isChest(block) ||
                isEnderchest(block) ||
                isShulkerBox(block) ||
                isFurnace(block) ||
                block instanceof HopperBlock ||
                block instanceof BrewingStandBlock ||
                block instanceof DispenserBlock;
    }

    public static boolean isBlockEntity(Block block) {
        ArrayList<String> blocks = new ArrayList<String>(){{
            for(BlockEntityType blockEntityType : Registry.BLOCK_ENTITY_TYPE) {
                this.add(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType).getPath());
            }
        }};
        return blocks.contains(Registry.BLOCK.getId(block).getPath());
    }

    public static boolean isFurnace(Block block) { return block instanceof AbstractFurnaceBlock; }

    public static boolean isSign(Block block) { return block instanceof AbstractSignBlock; }

    public static boolean isChest(Block block) {
        return block == Blocks.CHEST || block == Blocks.BARREL;
    }

    public static boolean isEnderchest(Block block) {
        return block instanceof EnderChestBlock;
    }

    public static boolean isShulkerBox(Block block) {
        return block == Blocks.SHULKER_BOX || block == Blocks.WHITE_SHULKER_BOX || block == Blocks.ORANGE_SHULKER_BOX || block == Blocks.MAGENTA_SHULKER_BOX || block == Blocks.LIGHT_BLUE_SHULKER_BOX || block == Blocks.YELLOW_SHULKER_BOX || block == Blocks.LIME_SHULKER_BOX || block == Blocks.PINK_SHULKER_BOX || block == Blocks.GRAY_SHULKER_BOX || block == Blocks.LIGHT_GRAY_SHULKER_BOX || block == Blocks.CYAN_SHULKER_BOX || block == Blocks.PURPLE_SHULKER_BOX || block == Blocks.BLUE_SHULKER_BOX || block == Blocks.BROWN_SHULKER_BOX || block == Blocks.GREEN_SHULKER_BOX || block == Blocks.RED_SHULKER_BOX || block == Blocks.BLACK_SHULKER_BOX;
    }

    public static boolean isButton(Block block) {
        return block instanceof AbstractButtonBlock;
    }

    public static boolean isDoor(Block block) {
        return block instanceof DoorBlock;
    }

    public static boolean isTrapdoor(Block block) {
        return block instanceof TrapdoorBlock;
    }

    public static boolean isCake(Block block) {
        return block instanceof CakeBlock;
    }

    public static boolean isRedstoneWire(Block block) {
        return block instanceof RedstoneWireBlock;
    }

    public static boolean isBed(Block block) {
        return block instanceof BedBlock;
    }

    public static boolean isInteractAble(Block block) {
        return isBlockEntity(block) ||
                isRedstoneWire(block) ||
                isCake(block) ||
                isButton(block) ||
                isDoor(block) ||
                isTrapdoor(block) ||
                isContainer(block) ||
                isSign(block) ||
                isBed(block);



    }
}
