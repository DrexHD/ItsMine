package me.drexhd.itsmine;

import me.drexhd.itsmine.util.PermissionUtil;
import net.fabricmc.api.ModInitializer;

/**
 * @author Indigo Amann
 */
public class ItsMine implements ModInitializer {
    private static PermissionUtil permissionUtil;
    public static int time = 0;
    public static int executed = 0;


    @Override
    public void onInitialize() {
        new ItsMineConfig();
        permissionUtil = new PermissionUtil();

//        SharedConstants.isDevelopment = true;
    }

    public static void reload(){
        ItsMineConfig.reload();
//        PermissionUtil.reload();
    }

    public static PermissionUtil permissions() {
        return permissionUtil;
    }

    public static String getDirectory(){
        return System.getProperty("user.dir");
    }

//    public static String blocksToAreaString3d(int blocks) {
//        int base = (int) Math.floor(Math.cbrt(blocks));
//        int additionalBlocks = blocks - (int) Math.pow(base, 3);
//        int extraRows = (int) Math.floor(Math.cbrt(Math.floor((float)additionalBlocks / base)));
//        int leftoverBlocks = additionalBlocks % base;
//        return (base + extraRows) + "x" + base + "x" + base + "(+" + leftoverBlocks + ")";
//    }
//    public static String blocksToAreaString2d(int blocks) {
//        int base = (int) Math.floor(Math.sqrt(blocks));
//        int additionalBlocks = blocks - (int) Math.pow(base, 2);
//        int extraRows = (int) Math.floor((float)additionalBlocks / base);
//        int leftoverBlocks = additionalBlocks % base;
//        return (base + extraRows) + "x" + base + "(+" + leftoverBlocks + ")";
//    }
//    public String blocksToAreaString(int blocks) {
//        return config.claims2d ? blocksToAreaString2d(blocks) : blocksToAreaString3d(blocks);
//    }
}
