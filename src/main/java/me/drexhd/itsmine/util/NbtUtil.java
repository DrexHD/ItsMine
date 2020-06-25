package me.drexhd.itsmine.util;


import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class NbtUtil {

    public static UUID getUUID(CompoundTag tag, String key){
        if (tag.contains(key + "Least") && tag.contains(key + "Most")) {
            final long least = tag.getLong(key + "Least");
            final long most = tag.getLong(key + "Most");
            return new UUID (most, least);
        }

        return tag.getUuid(key);
    }

    public static boolean containsUUID(CompoundTag tag, String key){
        return (tag.contains(key + "Least") && tag.contains(key + "Most")) || tag.containsUuid(key);
    }

}
