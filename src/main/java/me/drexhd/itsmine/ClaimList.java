package me.drexhd.itsmine;

import me.drexhd.itsmine.claim.Claim;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class ClaimList {


    private ArrayList<Claim> claims = new ArrayList<>();
    private HashMap<UUID, ArrayList<Claim>> claimsByUUID = new HashMap<>();


    //This map stores a list of claims sorted by chunkPos (the center chunk of each region), claims
    private HashMap<Region, ArrayList<Claim>> claimsByRegion = new HashMap<>();

    public ArrayList<Claim> get() {
        return claims;
    }

    @Nullable
    public Claim get(int x, int y, int z, @NotNull DimensionType dimension) {
        BlockPos pos = new BlockPos(x, y, z);
        Region region = Region.get(x, z);
        ArrayList<Claim> claims;
        if (claimsByRegion.containsKey(region)) {
            claims =
                    claimsByRegion
                            .get(
                                    region);

        } else {
            return null;
        }
        if (claims == null) return null;
        for (Claim claim : claims) {
            if (claim.includesPosition(pos) &&
                    claim.dimension.equals(dimension)) {
                for (Claim subzone : claim.subzones) {
                    if (subzone.dimension.equals(dimension) &&
                            subzone.includesPosition(pos)) {
                        return subzone;
                    }
                }
                return claim;
            }
        }
        return null;
    }

    @Nullable
    public Claim get(UUID uuid, String name) {
        ArrayList<Claim> claims = claimsByUUID.get(uuid);
        if (claims != null) {
            for (Claim claim : claims) {
                if (claim.getName().equals(name)) {
                    return claim;
                }
            }
        }
        return null;
    }

    @Nullable
    public ArrayList<Claim> get(UUID uuid) {
        return claimsByUUID.get(uuid);
    }

    public Set<UUID> getplayers() {
        return claimsByUUID.keySet();
    }

    public void remove(Claim claim) {
        ArrayList<Claim> temp = new ArrayList<>();
        for (Claim c : claims) {
            if (!claim.equals(c)) temp.add(c);
        }
        claims = new ArrayList<>();
        claimsByUUID = new HashMap<>();
        claimsByRegion = new HashMap<>();

        for (Claim c1 : temp) {
            add(c1);
        }

    }


/*    public void remove(Claim claim) {
        Region regionA = Region.get(claim.min.getX(), claim.min.getZ());
        Region regionB = Region.get(claim.min.getX(), claim.max.getZ());
        Region regionC = Region.get(claim.max.getX(), claim.min.getZ());
        Region regionD = Region.get(claim.max.getX(), claim.max.getZ());
        ArrayList<Claim> a = claimsByRegion.get(regionA);
        ArrayList<Claim> b = claimsByRegion.get(regionB);
        ArrayList<Claim> c = claimsByRegion.get(regionC);
        ArrayList<Claim> d = claimsByRegion.get(regionD);
        a.remove(claim);
        b.remove(claim);
        c.remove(claim);
        d.remove(claim);
        claimsByRegion.put(regionA, a);
        claimsByRegion.put(regionB, b);
        claimsByRegion.put(regionC, c);
        claimsByRegion.put(regionD, d);
        claims.remove(claim);
        ArrayList<Claim> claims = claimsByUUID.get(claim.claimBlockOwner);
        claims.remove(claim);
        claimsByUUID.put(claim.claimBlockOwner, claims);
    }*/

    public boolean add(Claim claim) {
        /*Do not add if the claim already exists*/
        if (claims.contains(claim)) return false;

        /*Find all corners of the claim, to save them at the proper location*/
        Region regionA = Region.get(claim.min.getX(), claim.min.getZ());
        Region regionB = Region.get(claim.min.getX(), claim.max.getZ());
        Region regionC = Region.get(claim.max.getX(), claim.min.getZ());
        Region regionD = Region.get(claim.max.getX(), claim.max.getZ());
        ArrayList<Claim> a = claimsByRegion.get(regionA);
        ArrayList<Claim> b = claimsByRegion.get(regionB);
        ArrayList<Claim> c = claimsByRegion.get(regionC);
        ArrayList<Claim> d = claimsByRegion.get(regionD);

        a = a == null ? new ArrayList<>() : a;
        a.add(claim);
        claimsByRegion.put(regionA, a);

        b = b == null ? new ArrayList<>() : b;
        b.add(claim);
        claimsByRegion.put(regionB, b);

        c = c == null ? new ArrayList<>() : c;
        c.add(claim);
        claimsByRegion.put(regionC, c);

        d = d == null ? new ArrayList<>() : d;
        d.add(claim);
        claimsByRegion.put(regionD, d);

        /*Add the claims to the remaining lists*/
        claims.add(claim);
        ArrayList<Claim> oldList = claimsByUUID.get(claim.claimBlockOwner);
        if (oldList != null) {
            oldList.add(claim);
            claimsByUUID.put(claim.claimBlockOwner, oldList);
        } else {
            ArrayList<Claim> newList = new ArrayList<>();
            newList.add(claim);
            claimsByUUID.put(claim.claimBlockOwner, newList);
        }
        return true;
    }


}
