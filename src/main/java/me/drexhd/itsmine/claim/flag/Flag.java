package me.drexhd.itsmine.claim.flag;

import org.jetbrains.annotations.Nullable;

public enum Flag {

    EXPLOSION_DESTRUCTION("explosion_destruction", false),
    MOB_SPAWNING("mob_spawn", false),
    FLUID_CROSSES_BORDERS("fluid_crosses_borders", false),
    ENTER_SOUND("enter_sound", false),
    FROST_WALKER("frost_walker", false);

    private String id;
    private boolean defaultValue;

    Flag(String id, boolean defaultValue){
        this.id = id;
        this.defaultValue = defaultValue;
    }

    @Nullable
    public static Flag byID(String string) {
        for (Flag flag : values()) {
            if (flag.id.equalsIgnoreCase(string)) return flag;
        }
        return null;
    }

    public static boolean isValid(String flag) {
        return byID(flag) != null;
    }

    public boolean getDefault() {
        return this.defaultValue;
    }

    public String getID() {
        return this.id;
    }

}
