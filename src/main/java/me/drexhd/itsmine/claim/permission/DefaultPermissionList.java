package me.drexhd.itsmine.claim.permission;

import net.minecraft.nbt.CompoundTag;
import java.util.HashMap;

public class DefaultPermissionList {

    private HashMap<String, Boolean> permissions = new HashMap<>();

    public void fromNBT(CompoundTag tag) {
        permissions.clear();
        for (String permission : tag.getKeys()) {
            permissions.put(permission, tag.getBoolean(permission));
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        permissions.forEach((permission, allowed) -> {
            if (allowed != null) tag.putBoolean(permission, allowed);
        });
        return tag;
    }

    public void add(String permission, boolean value) {
        this.permissions.put(permission, value);
    }

    public boolean remove(String permission) {
        return this.permissions.remove(permission);
    }

    public boolean isPermissionSet(String permission) {
        return this.permissions.containsKey(permission);
    }

    public HashMap<String, Boolean> get() {
        return this.permissions;
    }

    public boolean hasPermission(String permission) {
        return this.permissions.get(permission);
    }
}
