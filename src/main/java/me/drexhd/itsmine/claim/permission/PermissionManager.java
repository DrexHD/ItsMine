package me.drexhd.itsmine.claim.permission;

import me.drexhd.itsmine.claim.permission.map.DefaultMap;
import me.drexhd.itsmine.claim.permission.map.PermissionMap;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    public PermissionMap defaults = new DefaultMap();
    public Map<UUID, PermissionMap> playerPermissions = new HashMap<>();

    public boolean isPermissionSet(UUID player, String parent) {
        return playerPermissions.get(player) != null && playerPermissions.get(player).isPermissionSet(parent);
    }

    public boolean isPermissionSet(UUID player, String parent, String child) {
        return isPermissionSet(player, parent) || isPermissionSet(player, parent + "." + child);
    }

    public boolean hasPermission(UUID player, String parent) {
        if (isPermissionSet(player, parent)) return playerPermissions.get(player).hasPermission(parent);
        return defaults.hasPermission(parent);
    }

    public boolean hasPermission(UUID player, String parent, String child) {
        if (isPermissionSet(player, parent, child)) return playerPermissions.get(player).hasPermission(parent, child);
        return defaults.hasPermission(parent, child);
    }

    public PermissionMap getPermissionMap(UUID player) {
        PermissionMap permissionMap = playerPermissions.get(player);
        return permissionMap == null ? new DefaultMap() : permissionMap;
    }

    public void setPermission(UUID player, String permission, boolean enabled) {
        if (!playerPermissions.containsKey(player)) playerPermissions.put(player, new DefaultMap());
        playerPermissions.get(player).setPermission(permission, enabled);
    }

    public void clearPermission(UUID player, String permission) {
        if (!playerPermissions.containsKey(player)) playerPermissions.put(player, new DefaultMap());
        playerPermissions.get(player).clearPermission(permission);
    }

    public void resetPermissions(UUID player) {
        playerPermissions.remove(player);
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("defaults", defaults.toRegisteredNBT());
        {
            CompoundTag players = new CompoundTag();
            if (playerPermissions != null) playerPermissions.forEach((player, map) -> {
                if (player != null && map != null) players.put(player.toString(), map.toRegisteredNBT());
            });
            tag.put("players", players);
        }
        return tag;
    }

    public void fromNBT(CompoundTag tag) {
        defaults = PermissionMap.fromRegisteredNBT(tag.getCompound("defaults"));
        {
            CompoundTag players = tag.getCompound("players");
            playerPermissions.clear();
            players.getKeys().forEach(player -> playerPermissions.put(UUID.fromString(player), PermissionMap.fromRegisteredNBT(players.getCompound(player))));
        }
    }

}
