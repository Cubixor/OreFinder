package me.cubixor.orefinder;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

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


    public static void hideEntity(Player stacker, Entity entity) {
        try {
            Constructor<?> constructor;
            Class<?> packetClass;
            Object handle = stacker.getClass().getMethod("getHandle").invoke(stacker);
            Object playerConnection;

            try {
                constructor = getNMSClass("PacketPlayOutEntityDestroy").getConstructor(int[].class);
                packetClass = getNMSClass("Packet");
                playerConnection = handle.getClass().getField("playerConnection").get(handle);
            } catch (Throwable throwable) {
                constructor = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy").getConstructor(int[].class);
                packetClass = Class.forName("net.minecraft.network.protocol.Packet");
                playerConnection = handle.getClass().getField("b").get(handle);
            }

            Object packet = constructor.newInstance(new int[]{entity.getEntityId()});

            playerConnection.getClass().getMethod("sendPacket", packetClass).invoke(playerConnection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<?> getNMSClass(String name) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        return Class.forName("net.minecraft.server." + version + "." + name);

    }

}
