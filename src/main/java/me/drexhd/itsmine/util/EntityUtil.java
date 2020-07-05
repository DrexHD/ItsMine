package me.drexhd.itsmine.util;

import com.mojang.authlib.GameProfile;
import me.drexhd.itsmine.ClaimManager;
import me.drexhd.itsmine.claim.Claim;
import me.drexhd.itsmine.Functions;
import me.drexhd.itsmine.MonitorableWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class EntityUtil {

    public static GameProfile getGameProfile(UUID uuid){
        return ClaimManager.server.getUserCache().getByUuid(uuid);
    }

    public static boolean canDamage(UUID player, Claim claim, Entity entity) {
        return claim.hasPermission(player, "damage_entity", Registry.ENTITY_TYPE.getId(entity.getType()).getPath());
    }


    public static ArrayList<Entity> getEntities(Claim claim){
        ArrayList<Entity> entityList = new ArrayList<>();
        ServerWorld world = WorldUtil.getServerWorld(claim.dimension);
        MonitorableWorld monitorableWorld = (MonitorableWorld) world;
        monitorableWorld.EntityList().forEach((uuid, entity) -> {
            if(claim.includesPosition(entity.getBlockPos())) entityList.add(entity);
        });
        return entityList;
    }

    public static ArrayList<Entity> filterByCategory(ArrayList<Entity> entityList, SpawnGroup spawnGroup){
        ArrayList<Entity> filteredEntityList = new ArrayList<>();
        for(Entity entity : entityList) if(entity.getType().getSpawnGroup() == spawnGroup) filteredEntityList.add(entity);
        return filteredEntityList;
    }

    public static Map<EntityType, Integer> sortByType(ArrayList<Entity> entityList){
        Map<EntityType, Integer> entityMap = new HashMap<>();
        for(Entity entity : entityList) {
            EntityType entityType = entity.getType();
            if (entityMap.containsKey(entityType)) {
                entityMap.put(entityType, entityMap.get(entityType)+1);
            } else {
                entityMap.put(entityType, 1);
            }
        }
        return Functions.sortByValue(entityMap);
    }



}
