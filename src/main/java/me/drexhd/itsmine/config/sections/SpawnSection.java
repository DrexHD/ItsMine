package me.drexhd.itsmine.config.sections;

import net.minecraft.util.math.BlockPos;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class SpawnSection {

    @Setting(value = "x")
    public int x = 588;

    @Setting(value = "y")
    public int y = 67;

    @Setting(value = "z")
    public int z = -40;

    public BlockPos getBlockPos() {
        return new BlockPos(x, y, z);
    }

}
