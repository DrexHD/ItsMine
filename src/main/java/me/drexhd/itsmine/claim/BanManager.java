package me.drexhd.itsmine.claim;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BanManager {

    private Set<UUID> bannedPlayers = new HashSet<>();

    public Set<UUID> getBannedPlayers() {
        return bannedPlayers;
    }

    public boolean isBanned(UUID uuid) {
        return bannedPlayers.contains(uuid);
    }

    public void ban(UUID uuid) {
        bannedPlayers.add(uuid);
    }

    public void unban(UUID uuid) {
        bannedPlayers.remove(uuid);
    }

    public void fromNBT(ListTag tag) {
        bannedPlayers.clear();
        for (int i = 0; i < tag.size(); i++) {
            bannedPlayers.add(UUID.fromString(tag.getString(i)));
        }
    }

    public ListTag toNBT() {
        ListTag tag = new ListTag();
        bannedPlayers.forEach((uuid) -> {
            StringTag stringTag = StringTag.of(uuid.toString());
            tag.add(stringTag);
        });
        return tag;
    }

}
