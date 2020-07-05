package me.drexhd.itsmine;

import me.drexhd.itsmine.util.PermissionUtil;
import net.fabricmc.api.ModInitializer;

/**
 * @author Indigo Amann
 */
public class ItsMine implements ModInitializer {
    private static PermissionUtil permissionUtil;

    @Override
    public void onInitialize() {
        new ItsMineConfig();
        permissionUtil = new PermissionUtil();
    }

    public static void reload(){
        ItsMineConfig.reload();
    }

    public static PermissionUtil permissions() {
        return permissionUtil;
    }

    public static String getDirectory(){
        return System.getProperty("user.dir");
    }
}
