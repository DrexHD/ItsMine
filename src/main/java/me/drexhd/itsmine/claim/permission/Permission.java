package me.drexhd.itsmine.claim.permission;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public enum Permission {
    PLACE("place", PermissionGroup.BLOCK),
    BREAK("break", PermissionGroup.BLOCK),
    MODIFY("modify", PermissionGroup.MODIFY),
    INTERACT_BLOCK("interact_block", PermissionGroup.BLOCK_ENTITY),
//    CONTAINER("container", PermissionGroup.CONTAINER),
    BUILD("build"),
    FLIGHT("flight"),
    INTERACT_ENTITY("interact_entity", PermissionGroup.ENTITY),
    DAMAGE_ENTITY("damage_entity", PermissionGroup.ENTITY),
    USE_ITEM("use_item", PermissionGroup.ITEM);


    public String id;
    public PermissionGroup permissionGroup;

    Permission(String id) {
        this.id = id;
    }

    Permission(String id, PermissionGroup permissionGroup) {
        this.id = id;
        this.permissionGroup = permissionGroup;
    }

    @Nullable
    private static Permission byID(String string) {
        for (Permission permission : values()) {
            if (permission.id.equals(string)) return permission;
        }
        return null;
    }

    public static boolean isValid(String permission) {
        if (permission.matches("[a-z_]+[.][\\w_]+")) {
            String parent = permission.split("[.]")[0];
            String child = permission.split("[.]")[1];
            Permission perm = byID(parent);
            if (perm != null) {
                PermissionGroup permGroup = perm.permissionGroup;
                for (String string : permGroup.list) {
                    if (string.equals(child)) return true;
                }
            }
            return false;
        } else {
            return byID(permission) != null;
        }
    }


    public enum PermissionGroup {
        BLOCK(new ArrayList<String>(){{
            for (Block block : Registry.BLOCK) {
                this.add(Registry.BLOCK.getId(block).getPath());
            }
        }}),
        BLOCK_ENTITY(new ArrayList<String>(){{
            for(BlockEntityType blockEntityType : Registry.BLOCK_ENTITY_TYPE) {
                this.add(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType).getPath());
            }
            for (Block block : Registry.BLOCK) {
                if(block instanceof AbstractButtonBlock || block instanceof LeverBlock || block instanceof DoorBlock || block instanceof TrapdoorBlock)
                this.add(Registry.BLOCK.getId(block).getPath());
            }
            this.add("TRAPDOORS");
            this.add("DOORS");
            this.add("BUTTONS");
            this.add("CONTAINERS");
        }}),
        ENTITY(new ArrayList<String>(){{
            for (EntityType entityType : Registry.ENTITY_TYPE) {
                this.add(Registry.ENTITY_TYPE.getId(entityType).getPath());
            }
        }}),
        ITEM(new ArrayList<String>(){{
            for (Item item : Registry.ITEM) {
                if(!(item instanceof BlockItem)){
                    this.add(Registry.ITEM.getId(item).getPath());
                }
            }
        }}),
        MODIFY(new ArrayList<String>(){{
            add("size");
            add("flags");
            add("permissions");
            add("properties");
            add("subzone");
        }});

        public ArrayList<String> list;

        PermissionGroup(ArrayList<String> list) {
            this.list = list;
        }

    }

}
