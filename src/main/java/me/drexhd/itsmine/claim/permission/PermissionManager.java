package me.drexhd.itsmine.claim.permission;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.permission.map.DefaultMap;
import me.drexhd.itsmine.claim.permission.map.PermissionMap;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    public PermissionMap defaults = new DefaultMap();
    public Map<UUID, PermissionMap> playerPermissions = new HashMap<>();

    public boolean isPermissionSet(UUID player, String permission) {
        if (permission != null) {
            PermissionMap map = playerPermissions.get(player);
            return map != null && map.isPermissionSet(permission);
        }
        return false;
    }

    //This method allows you to check if a permissions has been specifically denied for a player
    public boolean isPermissionDenied(UUID player, String permission) {
        if (isPermissionSet(player, permission)) {
            return !playerPermissions.get(player).hasPermission(permission);
        } else {
            if (defaults.isPermissionSet(permission)) {
                return !defaults.hasPermission(permission);
            } else {
                DefaultPermissionList defaultPermissionList = ClaimManager.INSTANCE.getDefaultPerms();
                if (defaultPermissionList.isPermissionSet(permission)) {
                    return !defaultPermissionList.hasPermission(permission);
                } else {
                    return false;
                }
            }
        }
    }


    /**
     * @param parent the root permission node
     * @param child  the permission group node (maybe be null)
     * @param player uuid of the player who's permission you want to check
     * @return true if the player has the specified permission, a parent permission or it is set in either default claim permissions or global claim permissions
     */
    public boolean hasPermission(UUID player, String parent, @Nullable String child) {
        if (child == null) {
            if (isPermissionSet(player, parent)) {
                return playerPermissions.get(player).hasPermission(parent);
            } else {
                if (defaults.isPermissionSet(parent)) {
                    return defaults.hasPermission(parent);
                } else {
                    DefaultPermissionList defaultPermissionList = ClaimManager.INSTANCE.getDefaultPerms();
                    if (defaultPermissionList.isPermissionSet(parent)) {
                        return defaultPermissionList.hasPermission(parent);
                    } else {
                        return false;
                    }
                }
            }
        } else {
            String permission = parent + "." + child;
            if (isPermissionSet(player, permission)) {
                return playerPermissions.get(player).hasPermission(permission);
            } else {
                if (defaults.isPermissionSet(permission)) {
                    return defaults.hasPermission(permission);
                } else {
                    //Before checking the global permissions, check if the parent flag is set to true in the claim
                    if (hasPermission(player, parent, null)) {
                        return true;
                    } else {
                        DefaultPermissionList defaultPermissionList = ClaimManager.INSTANCE.getDefaultPerms();
                        if (defaultPermissionList.isPermissionSet(permission)) {
                            return defaultPermissionList.hasPermission(permission);
                        } else {
                            return hasPermission(player, parent, null);
                        }
                    }
                }
            }
        }
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
