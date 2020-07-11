package me.drexhd.itsmine.claim.permission.map;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;

public abstract class PermissionMap {

    protected static HashMap<String, Class<? extends PermissionMap>> mapTypes = new HashMap<>();
    protected static HashMap<Class<? extends PermissionMap>, String> reverseMapTypes = new HashMap<>();

    static {
        mapTypes.put("default", DefaultMap.class);
        mapTypes.put("inverted", InvertedMap.class);
        reverseMapTypes.put(DefaultMap.class, "default");
        reverseMapTypes.put(InvertedMap.class, "inverted");
    }

    public static PermissionMap fromRegisteredNBT(CompoundTag tag) {
        String type = tag.getString("type");
        tag.remove("type");
        Class<? extends PermissionMap> clazz = mapTypes.get(type);
        if (clazz == null) return new DefaultMap();
        try {
            PermissionMap map = clazz.newInstance();
            map.fromNBT(tag);
            return map;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract HashMap<String, Boolean> getPermissionList();

    public abstract boolean isPermissionSet(String permission);

    public abstract boolean hasPermission(String permission);

    public abstract void setPermission(String permission, boolean has);

    public abstract void clearPermission(String permission);

    public abstract void fromNBT(CompoundTag tag);

    public abstract CompoundTag toNBT();

    public CompoundTag toRegisteredNBT() {
        CompoundTag tag = toNBT();
        tag.putString("type", reverseMapTypes.get(this.getClass()));
        return tag;
    }

}
