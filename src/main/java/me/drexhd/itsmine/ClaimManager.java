package me.drexhd.itsmine;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.util.ArgumentUtil;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Indigo Amann
 */
public class ClaimManager {
    public static ClaimManager INSTANCE = null;
    public static MinecraftServer server;
    public static final UUID defaultUUID = new UUID(0,0);
    public Map<PlayerEntity, Pair<BlockPos, BlockPos>> stickPositions = new HashMap<>();
    public List<UUID> ignoringClaims = new ArrayList<>();
    public List<UUID> flyers = new ArrayList<>();
    private HashMap<UUID, Integer> blocksLeft = new HashMap<>();
    private ClaimList claimList = new ClaimList();
    private int dataVersion = 1;

    public int getClaimBlocks(UUID id) {
        return blocksLeft.getOrDefault(id, ItsMineConfig.main().claims2d ? ItsMineConfig.main().claimBlock().default2D : ItsMineConfig.main().claimBlock().default3D);
    }

    public boolean useClaimBlocks(UUID player, int amount) {
        int blocks = getClaimBlocks(player) - amount;
        if (blocks < 0) return false;
        blocksLeft.put(player, blocks);
        return true;
    }

    public void addClaimBlocks(UUID player, int amount) {
        useBlocksUntil0(player, -amount);
    }

    public void addClaimBlocks(Collection<ServerPlayerEntity> players, int amount) {
        players.forEach(player -> useBlocksUntil0(player.getGameProfile().getId(), -amount));
    }

    public void useBlocksUntil0(UUID player, int amount) {
        if (!useClaimBlocks(player, amount)) blocksLeft.put(player, 0);
    }

    public void setClaimBlocks(Collection<ServerPlayerEntity> players, int amount) {
        players.forEach(player -> setClaimBlocks(player.getGameProfile().getId(), amount));
    }

    public Claim getClaim(UUID uuid, String name) {
        return claimList.get(uuid, name);
    }

    public Claim getClaim(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        return claimList.get(getGameProfile(context).getId(), name);
    }

    public GameProfile getGameProfile(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        try {
            return ArgumentUtil.getGameProfile(GameProfileArgumentType.getProfileArgument(context, "claimOwner"), context);
        } catch (IllegalArgumentException e) {
            ServerCommandSource source = context.getSource();
            return source.getMinecraftServer().getUserCache().getByUuid(source.getPlayer().getUuid());
        }
    }

    public List<Claim> getPlayerClaims(UUID id) {
        return claimList.get(id) == null ? new ArrayList<>() : claimList.get(id);
    }

    public ArrayList<Claim> getClaimList() {
        return claimList.get();
    }

    public void removeClaim(Claim claim) {
        claimList.remove(claim);
    }

    public void updateClaim(Claim claim) {
        removeClaim(claim);
        addClaim(claim);
    }

    public void setClaimBlocks(UUID player, int amount) {
        blocksLeft.put(player, Math.max(amount, 0));
    }

    public void releaseBlocksToOwner(Claim claim) {
        if (claim.claimBlockOwner != null) addClaimBlocks(claim.claimBlockOwner, claim.getArea());
    }

    public int getDataVersion() {
        return this.dataVersion;
    }

    public boolean addClaim(Claim claim) {
        return claimList.add(claim);
    }

    public boolean wouldIntersect(Claim claim) {
        for (Claim value : claimList.get()) {
            if (!value.isChild && !claim.equals(value) && (claim.intersects(value) || value.intersects(claim)))
                return true;
        }
        return false;
    }

    public boolean wouldSubzoneIntersect(Claim claim) {
        for (Claim value : claimList.get()) {
            if (!claim.equals(value) && claim.intersects(value, true)) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag toNBT() {
        //Main node;
        CompoundTag root = new CompoundTag();
        //Claim subnode
        CompoundTag claims = new CompoundTag();
        //Loop through all players with claims
        for (UUID uuid : claimList.getplayers()) {
            ListTag playerClaims = new ListTag();
            //Loop through the player claims
            for (Claim claim : claimList.get(uuid)) {
                if (!claim.isChild)
                    playerClaims.add(claim.toNBT());
            }
            claims.put(uuid.toString(), playerClaims);
        }
        root.put("claims", claims);
        CompoundTag blocksLeftTag = new CompoundTag();
        blocksLeft.forEach((id, amount) -> {
            if (id != null) blocksLeftTag.putInt(id.toString(), amount);
        });
        root.put("blocksLeft", blocksLeftTag);
        ListTag ignoring = new ListTag();
        CompoundTag targets = new CompoundTag();
        ignoringClaims.forEach(id -> {
            targets.putString("id", id.toString());
            ignoring.add(targets.get("id"));
        });
        ListTag listTag = new ListTag();
        for (UUID flyer : flyers) {
            CompoundTag tag1 = new CompoundTag();
            tag1.putUuid("uuid", flyer);
            listTag.add(tag1);
        }
        root.put("flyers", listTag);
        root.put("ignoring", ignoring);
        root.putInt("dataVersion", dataVersion);
        return root;
    }

    @Nullable
    public Claim getClaimAt(BlockPos pos, DimensionType dimension) {
        return claimList.get(pos.getX(), pos.getY(), pos.getZ(), dimension);
    }


    public void fromNBT(CompoundTag tag) {
        CompoundTag blocksLeftTag = tag.getCompound("blocksLeft");
        blocksLeft.clear();
        blocksLeftTag.getKeys().forEach(key -> blocksLeft.put(UUID.fromString(key), blocksLeftTag.getInt(key)));
        ListTag ignoringTag = (ListTag) tag.get("ignoring");
        ignoringTag.forEach(it -> ignoringClaims.add(UUID.fromString(it.asString())));
        ListTag listTag = tag.getList("flyers", 11);
        flyers.clear();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag1 = listTag.getCompound(i);
            flyers.add(tag1.getUuid("uuid"));
        }
        int oldDataVersion = tag.getInt("dataVersion");
        loadClaims(tag, oldDataVersion, dataVersion);
    }

    private void loadClaims(CompoundTag tag, int oldDataVersion, int dataVersion) {
        switch (oldDataVersion) {
            case 0: {
                ListTag listTag = (ListTag) tag.get("claims");
                claimList = new ClaimList();
                listTag.forEach(claimTag -> {
                    Claim claim = new Claim();
                    UUID uuid;
                    if(((CompoundTag) claimTag).contains("top_owner")) uuid = ((CompoundTag) claimTag).getUuid("top_owner");
                    else uuid = new UUID(0, 0);
                    claim.fromTag(uuid, (CompoundTag) claimTag);
                    claimList.add(claim);
                    for (Claim subzone : claim.subzones) {
                        claimList.add(subzone);
                    }
                });
                break;
            }
            case 1: {
                CompoundTag compoundTag = (CompoundTag) tag.get("claims");
                claimList = new ClaimList();
                for (String uuid : compoundTag.getKeys()) {
                    ListTag listTag = (ListTag) compoundTag.get(uuid);
                    listTag.forEach(it -> {
                        Claim claim = new Claim();
                        claim.fromTag(UUID.fromString(uuid), (CompoundTag) it);
                        claimList.add(claim);
                        for (Claim subzone : claim.subzones) {
                            claimList.add(subzone);
                        }
                    });
                }
                break;
            }
            default:
                throw new RuntimeException("Invalid dataVersion " + dataVersion);
        }
        if (oldDataVersion != dataVersion) {
            fromNBT(toNBT());
        }
    }

}
