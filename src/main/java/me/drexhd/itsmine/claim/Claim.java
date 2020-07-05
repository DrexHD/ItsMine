package me.drexhd.itsmine.claim;

import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.ItsMineConfig;
import me.drexhd.itsmine.Messages;
import me.drexhd.itsmine.MonitorableWorld;
import me.drexhd.itsmine.claim.flag.FlagManager;
import me.drexhd.itsmine.claim.permission.PermissionManager;
import me.drexhd.itsmine.util.MessageUtil;
import me.drexhd.itsmine.util.WorldUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Indigo Amann
 */
public class Claim {
    public String name;
    public BlockPos min, max;
    public @Nullable
    BlockPos tpPos;
    public DimensionType dimension = DimensionType.getOverworldDimensionType();
    public List<Claim> subzones = new ArrayList<>();
    public FlagManager flagManager = new FlagManager();
    public PermissionManager permissionManager = new PermissionManager();
    public RentManager rentManager = new RentManager();
    public UUID claimBlockOwner = new UUID(0,0);
    public String customOwnerName, enterMessage, leaveMessage;
    public boolean isChild = false;

    public Claim() {
    }

    public Claim(UUID uuid, CompoundTag tag) {
        fromTag(uuid, tag);
    }

    public Claim(String name, UUID claimBlockOwner, BlockPos min, BlockPos max, DimensionType dimension) {
        this(name, claimBlockOwner, min, max, dimension, null, false);
    }

    public Claim(String name, UUID claimBlockOwner, BlockPos min, BlockPos max, DimensionType dimension, @Nullable BlockPos tpPos, boolean isChild) {
        this.claimBlockOwner = claimBlockOwner;
        this.min = min;
        this.max = max;
        this.name = name;
        this.dimension = dimension;
        this.tpPos = tpPos;
        this.isChild = isChild;
    }

    public boolean includesPosition(BlockPos pos) {
        return pos.getX() >= min.getX() && pos.getY() >= min.getY() && pos.getZ() >= min.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ();
    }

    public boolean isInside(Claim claim) {
        BlockPos a = min,
                b = max,
                c = new BlockPos(max.getX(), min.getY(), min.getZ()),
                d = new BlockPos(min.getX(), max.getY(), min.getZ()),
                e = new BlockPos(min.getX(), min.getY(), max.getZ()),
                f = new BlockPos(max.getX(), max.getY(), min.getZ()),
                g = new BlockPos(max.getX(), min.getY(), max.getZ()),
                h = new BlockPos(min.getX(), max.getY(), max.getZ());
        return claim.includesPosition(a) && claim.includesPosition(b) && claim.includesPosition(c) && claim.includesPosition(d) && claim.includesPosition(e) && claim.includesPosition(f) && claim.includesPosition(g) && claim.includesPosition(h);
    }

    public boolean intersects(Claim claim) {
        return intersects(claim, false);
    }

    public int getEntities(ServerWorld world) {
        AtomicReference<Integer> entities = new AtomicReference<>();
        entities.set(0);
        MonitorableWorld monitorableWorld = (MonitorableWorld) world;
        monitorableWorld.EntityList().forEach((uuid, entity) -> {
            if (includesPosition(entity.getBlockPos())) {
                entities.set(entities.get() + 1);
            }
        });
        return entities.get();
    }


    public boolean intersects(Claim claim, boolean checkforsubzone) {
        if (claim == null) return false;
        if (!claim.dimension.equals(dimension)) return false;
        BlockPos a = min,
                b = max,
                c = new BlockPos(max.getX(), min.getY(), min.getZ()),
                d = new BlockPos(min.getX(), max.getY(), min.getZ()),
                e = new BlockPos(min.getX(), min.getY(), max.getZ()),
                f = new BlockPos(max.getX(), max.getY(), min.getZ()),
                g = new BlockPos(max.getX(), min.getY(), max.getZ()),
                h = new BlockPos(min.getX(), max.getY(), max.getZ());
        if (claim.isChild && checkforsubzone || !claim.isChild && !checkforsubzone) {
            return claim.includesPosition(a) ||
                    claim.includesPosition(b) ||
                    claim.includesPosition(c) ||
                    claim.includesPosition(d) ||
                    claim.includesPosition(e) ||
                    claim.includesPosition(f) ||
                    claim.includesPosition(g) ||
                    claim.includesPosition(h);
            //If the claim is a subzone and checking for subzone is disabled or if the claim isnt a subzone and checking is enabled, instantly return false
        } else {
            return false;
        }
    }

    public boolean hasPermission(UUID player, String parent) {
        MessageUtil.debug("Checking permission ("+ player + ", " + parent  + ")");
        if(player == null) return false;
        if (parent.matches("[a-z_]+[.][\\w_]+")) {
            return hasPermission(player, parent.split("[.]")[0], parent.split("[.]")[1]);
        }
        UUID tenant = this.rentManager.getTenant();
        /*Check whether or not the player is a claim tenant and return true unless it's a modify permission*/
        if(tenant != null && tenant.equals(player) && !parent.equalsIgnoreCase("modify")){
            return true;
        }
        /*claimBlockOwner might be null*/
        if(claimBlockOwner != null && claimBlockOwner.equals(player)) return true;
        return ClaimManager.INSTANCE.ignoringClaims.contains(player) ||
                permissionManager.hasPermission(player, parent);
    }

    public boolean hasPermission(UUID player, String parent, String child) {
        MessageUtil.debug("Checking permission ("+ player + ", " + parent + ", " + child + ")");
        if(player == null) return false;
        UUID tenant = this.rentManager.getTenant();
        if(tenant != null && tenant.equals(player) && !parent.equalsIgnoreCase("modify")){
            return true;
        }
        if(claimBlockOwner != null && claimBlockOwner.equals(player)) return true;
        return ClaimManager.INSTANCE.ignoringClaims.contains(player) ||
                permissionManager.hasPermission(player, parent) ||
                permissionManager.hasPermission(player, parent, child);
    }

    public boolean canModifySettings(UUID uuid) {
        return hasPermission(uuid, "modify", "permissions");
    }

    public void addSubzone(Claim claim) {
        subzones.add(claim);
    }

    public void removeSubzone(Claim claim) {
        subzones.remove(claim);
    }

    public BlockPos getSize() {
        return max.subtract(min);
    }

    public void expand(BlockPos modifier) {
        if (modifier.getX() > 0) {
            max = max.add(modifier.getX(), 0, 0);
        } else {
            min = min.add(modifier.getX(), 0, 0);
        }
        if (modifier.getY() > 0) {
            max = max.add(0, modifier.getY(), 0);
        } else {
            min = min.add(0, modifier.getY(), 0);
        }
        if (modifier.getZ() > 0) {
            max = max.add(0, 0, modifier.getZ());
        } else {
            min = min.add(0, 0, modifier.getZ());
        }
    }

    public void shrink(BlockPos modifier) {
        if (modifier.getX() < 0) {
            min = min.add(-modifier.getX(), 0, 0);
        } else {
            max = max.add(-modifier.getX(), 0, 0);
        }
        if (modifier.getY() < 0) {
            min = min.add(0, -modifier.getY(), 0);
        } else {
            max = max.add(0, -modifier.getY(), 0);
        }
        if (modifier.getZ() < 0) {
            min = min.add(0, 0, -modifier.getZ());
        } else {
            max = max.add(0, 0, -modifier.getZ());
        }
    }

    public boolean canShrinkWithoutHittingOtherSide(BlockPos modifier) {
        if (modifier.getX() < 0) {
            if (min.getX() - modifier.getX() > max.getX()) return false;
        } else {
            if (max.getX() - modifier.getX() < min.getX()) return false;
        }
        if (modifier.getY() < 0) {
            if (min.getY() - modifier.getY() > max.getY()) return false;
        } else {
            if (max.getY() - modifier.getY() < min.getY()) return false;
        }
        if (modifier.getZ() < 0) {
            return min.getZ() - modifier.getZ() <= max.getZ();
        } else {
            return max.getZ() - modifier.getZ() >= min.getZ();
        }
    }

    public void expand(Direction direction, int distance) {
        expand(new BlockPos(direction.getOffsetX() * distance, direction.getOffsetY() * distance, direction.getOffsetZ() * distance));
    }

    public void shrink(Direction direction, int distance) {
        shrink(new BlockPos(direction.getOffsetX() * distance, direction.getOffsetY() * distance, direction.getOffsetZ() * distance));
    }

    public int getArea() {
        return getSize().getX() * (ItsMineConfig.main().claims2d ? 1 : getSize().getY()) * getSize().getZ();
    }


    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        {
            CompoundTag pos = new CompoundTag();
            pos.putInt("minX", min.getX());
            pos.putInt("minY", min.getY());
            pos.putInt("minZ", min.getZ());
            pos.putInt("maxX", max.getX());
            pos.putInt("maxY", max.getY());
            pos.putInt("maxZ", max.getZ());
            pos.putString("dimension", WorldUtil.getDimensionNameWithNameSpace(dimension));
            if (tpPos != null) {
                pos.putInt("tpX", this.tpPos.getX());
                pos.putInt("tpY", this.tpPos.getY());
                pos.putInt("tpZ", this.tpPos.getZ());
            }
            tag.put("position", pos);
        }
        {
            if (!isChild) {
                ListTag subzoneList = new ListTag();
                subzones.forEach(it -> subzoneList.add(it.toNBT()));
                tag.put("subzones", subzoneList);
            }
        }
        {
            tag.put("rent", rentManager.toTag());
        }
        {
            tag.put("flags", flagManager.toNBT());
            tag.put("permissions", permissionManager.toNBT());

        }
        {
            CompoundTag meta = new CompoundTag();
            if (this.enterMessage != null) meta.putString("enterMsg", this.enterMessage);
            if (this.leaveMessage != null) meta.putString("leaveMsg", this.leaveMessage);
            tag.put("meta", meta);
        }

        if (this.customOwnerName != null) tag.putString("cOwnerName", this.customOwnerName);
        tag.putString("name", name);
        return tag;
    }

    public void fromTag(UUID uuid, CompoundTag tag) {
        {
            CompoundTag pos = tag.getCompound("position");
            int minX = pos.getInt("minX");
            int minY = pos.getInt("minY");
            int minZ = pos.getInt("minZ");
            int maxX = pos.getInt("maxX");
            int maxY = pos.getInt("maxY");
            int maxZ = pos.getInt("maxZ");
            if (maxY == 0) maxY = 255;
            this.min = new BlockPos(minX, minY, minZ);
            this.max = new BlockPos(maxX, maxY, maxZ);
            if (pos.contains("tpX") && pos.contains("tpY") && pos.contains("tpZ")) {
                this.tpPos = new BlockPos(pos.getInt("tpX"), pos.getInt("tpY"), pos.getInt("tpZ"));
            }
            this.dimension = WorldUtil.getWorldType(pos.getString("dimension")).getDimension();
        }
        {
            if (!isChild) {
                subzones = new ArrayList<>();
                ListTag subzoneList = (ListTag) tag.get("subzones");
                if (subzoneList != null) {
                    subzoneList.forEach(it -> {
                        Claim claim = new Claim(uuid, (CompoundTag) it);
                        claim.isChild = true;
                        subzones.add(claim);
                    });
                }
            }
        }
        {
            CompoundTag rent = tag.getCompound("rent");
            if(!rent.isEmpty()) {
                rentManager = new RentManager();
                rentManager.fromTag(rent);
            }
/*            {
                CompoundTag rented = rent1.getCompound("rented");
                if (containsUUID(rented, "tenant")) rentManager.setTenant(getUUID(rented, "tenant"));
                if (rented.contains("rentedUntil")) rentManager.setUntil(rented.getInt("rentedUntil"));
            }
            {
                CompoundTag rentable = rent1.getCompound("rentable");
                CompoundTag currency = rentable.getCompound("currency");
                if (rentable.contains("rentable")) rentManager.setRentable(rentable.getBoolean("rentable"));
                if (currency != null) rentManager.setCurrency(ItemStack.fromTag(currency));
                if (rentable.contains("rentTime")) rentManager.setRentAbleTime(rentable.getInt("rentTime"));
                if (rentable.contains("maxrentTime")) rentManager.setMax(rentable.getInt("maxrentTime"));
            }
            {
                CompoundTag revenue = rent1.getCompound("revenue");
                if (!revenue.isEmpty()) {
                    for (int i = 1; i <= revenue.getSize(); i++) {
                        CompoundTag revenueTag = revenue.getCompound(String.valueOf(i));
                        rentManager.addRevenue(ItemStack.fromTag(revenueTag));
                    }
                }
            }*/
        }
        {
            CompoundTag flags = tag.getCompound("flags");
            if (!flags.isEmpty()) {
                flagManager = new FlagManager();
                flagManager.fromNBT(flags);
            }

            permissionManager = new PermissionManager();
            permissionManager.fromNBT(tag.getCompound("permissions"));
            claimBlockOwner = uuid;

        }
        {
            CompoundTag meta = tag.getCompound("meta");
            if (meta.contains("enterMsg")) this.enterMessage = meta.getString("enterMsg");
            if (meta.contains("leaveMsg")) this.leaveMessage = meta.getString("leaveMsg");
        }
        if (tag.contains("cOwnerName")) this.customOwnerName = tag.getString("cOwnerName");
        name = tag.getString("name");
    }

    public boolean is2d() {
        return min.getY() == 0 && max.getY() == 255;
    }



    public enum Event {
        ENTER_CLAIM("enter", ItsMineConfig.main().message().enterDefault),
        LEAVE_CLAIM("leave", ItsMineConfig.main().message().leaveDefault);

        public String id;
        String defaultValue;

        Event(String id, String defaultValue) {
            this.id = id;
            this.defaultValue = defaultValue;
        }

        @Nullable
        public static Event getById(String id) {
            for (Event value : values()) {
                if (value.id.equalsIgnoreCase(id)) {
                    return value;
                }
            }

            return null;
        }
    }

    public enum HelpBook {
        GET_STARTED("getStarted", Messages.GET_STARTED, "Get Started"),
        COMMAND("commands", Messages.HELP, "Claim Commands"),
        PERMS_AND_SETTINGS("perms_and_flags", Messages.SETTINGS_AND_PERMISSIONS, "Claim Permissions and Flags");

        public String id;
        public String title;
        public Text[] texts;

        HelpBook(String id, Text[] texts, String title) {
            this.id = id;
            this.title = title;
            this.texts = texts;
        }

        @Nullable
        public static HelpBook getById(String id) {
            for (HelpBook value : values()) {
                if (value.id.equalsIgnoreCase(id)) {
                    return value;
                }
            }

            return null;
        }

        public String getCommand() {
            return "/claim help " + this.id + " %page%";
        }
    }

}
