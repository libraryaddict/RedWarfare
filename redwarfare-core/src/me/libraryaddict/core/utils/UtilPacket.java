package me.libraryaddict.core.utils;

import net.minecraft.server.v1_12_R1.PacketPlayOutSpawnEntity;
import org.bukkit.Location;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

public class UtilPacket {
    private static HashMap<String, Field> _fields = new HashMap<String, Field>();

    private static Field getField(Class c, String name) {
        String entry = c.getName() + ";" + name;

        Field field = _fields.get(entry);

        if (field != null) {
            return field;
        }

        try {
            field = c.getDeclaredField(name);

            field.setAccessible(true);

            _fields.put(entry, field);
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        return field;
    }

    private static void setField(Class c, String fieldName, Object obj,
            Object value) throws IllegalArgumentException, IllegalAccessException {
        Field field = getField(c, fieldName);
        field.set(obj, value);
    }

    public static PacketPlayOutSpawnEntity spawnEntity(Location loc, int data1, int data2) {
        PacketPlayOutSpawnEntity spawnEntity = new PacketPlayOutSpawnEntity();

        try {
            setField(PacketPlayOutSpawnEntity.class, "a", spawnEntity, UtilEnt.getNewEntityId());
            setField(PacketPlayOutSpawnEntity.class, "b", spawnEntity, UUID.randomUUID());
            setField(PacketPlayOutSpawnEntity.class, "c", spawnEntity, loc.getX());
            setField(PacketPlayOutSpawnEntity.class, "d", spawnEntity, loc.getY());
            setField(PacketPlayOutSpawnEntity.class, "e", spawnEntity, loc.getZ());
            setField(PacketPlayOutSpawnEntity.class, "k", spawnEntity, data1);
            setField(PacketPlayOutSpawnEntity.class, "l", spawnEntity, data2);

            return spawnEntity;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }
}
