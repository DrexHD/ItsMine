package me.drexhd.itsmine;

import java.util.ArrayList;

public class Region {

    private final int x;
    private final int z;
    private static ArrayList<Region> list = new ArrayList<>();

    public Region(int x, int z) {
        this.x = x >> 10;
        this.z = z >> 10;
        list.add(this);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String toString() {
        return "x: " + x + " z: " + z;
    }

    public static Region get(int x, int z) {
        for(Region region : list) {
            if(region.getX() == x >> 10 && region.getZ() == z >> 10) return region;
        }
        return new Region(x, z);
    }

}
