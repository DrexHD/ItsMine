package me.drexhd.itsmine.claim.flag;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class FlagManager {

    private HashMap<String, Boolean> flags = new HashMap<>();


    private boolean isFlagSet(String flag) {
        return flags.containsKey(flag);
    }

    public boolean hasFlag(String flag) {
        if (isFlagSet(flag)) {
            return flags.get(flag);
        }
        if (Flag.isValid(flag)) {
            return Flag.byID(flag).getDefault();
        }
        return false;
    }

    public void setFlag(String flag, boolean enabled) {
        if (Flag.isValid(flag)) {
            flags.put(flag, enabled);
        }
    }

    public void clearFlag(String flag) {
        flags.remove(flag);
    }

    public void fromNBT(CompoundTag tag) {
        flags.clear();
        for (String flag : tag.getKeys()) {
            flags.put(flag, tag.getBoolean(flag));
        }
    }

    public CompoundTag toNBT() {
        CompoundTag compoundTag = new CompoundTag();
        if (flags != null) flags.forEach((flag, value) -> {
            if (flag != null && value != null) compoundTag.putBoolean(flag, value);
        });
        return compoundTag;
    }

}
