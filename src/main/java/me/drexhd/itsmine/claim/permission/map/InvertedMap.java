package me.drexhd.itsmine.claim.permission.map;

import me.drexhd.itsmine.claim.permission.Permission;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public class InvertedMap extends PermissionMap {
    private HashMap<String, Boolean> permissions = new HashMap<>();

    @Override
    public HashMap<String, Boolean> getPermissionList() {
        return permissions;
    }

    @Override
    public boolean isPermissionSet(String permission) {
        return true;
    }

    @Override
    public boolean hasPermission(String permission) {
        return !permissions.containsKey(permission) || permissions.get(permission);
    }

    @Override
    public void setPermission(String permission, boolean has) {
        permissions.put(permission, has);
    }

    @Override
    public void clearPermission(String permission) {
        permissions.remove(permission);
    }

    @Override
    public void fromNBT(CompoundTag tag) {
        permissions.clear();
        for (String permission : tag.getKeys()) {
            if (Permission.isValid(permission))
                permissions.put(permission, tag.getBoolean(permission));
        }
    }

    @Override
    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        permissions.forEach((permission, allowed) -> {
            if (allowed != null) tag.putBoolean(permission, allowed);
        });
        return tag;
    }
}
