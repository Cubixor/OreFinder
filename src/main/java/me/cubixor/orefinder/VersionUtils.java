package me.cubixor.orefinder;

import org.bukkit.Material;

public class VersionUtils {

    private static boolean is12 = false;

    public static void initialize() {
        try {
            Material.KELP.getClass();
        } catch (NoSuchFieldError e) {
            is12 = true;
        }
    }

    public static boolean is12() {
        return is12;
    }

}
