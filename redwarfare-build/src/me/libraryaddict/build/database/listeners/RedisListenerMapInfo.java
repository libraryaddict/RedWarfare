package me.libraryaddict.build.database.listeners;

import com.google.gson.*;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Type;
import java.util.UUID;

public class RedisListenerMapInfo extends JedisPubSub {
    private JavaPlugin _plugin;
    private WorldManager _worldManager;

    public RedisListenerMapInfo(WorldManager worldManager, JavaPlugin plugin) {
        _worldManager = worldManager;
        _plugin = plugin;

        new Thread() {
            public void run() {
                RedisManager.addListener(RedisListenerMapInfo.this, RedisKey.NOTIFY_MAP_INFO.fromParams("*"));
            }
        }.start();
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        String server = channel.substring(RedisKey.NOTIFY_MAP_INFO.fromParams("").getKey().length());

        MapInfo info;

        if (message.isEmpty()) {
            info = null;
        } else {
            info = new GsonBuilder().registerTypeAdapter(Pair.class, new JsonDeserializer<Pair>() {
                @Override
                public Pair deserialize(JsonElement arg0, Type arg1,
                        JsonDeserializationContext arg2) throws JsonParseException {
                    JsonObject obj;

                    if (!arg0.isJsonObject())
                        obj = new JsonParser().parse(arg0.getAsString()).getAsJsonObject();
                    else
                        obj = arg0.getAsJsonObject();

                    return Pair.of(UUID.fromString(obj.get("left").getAsString()), obj.get("right").getAsString());
                }
            }).create().fromJson(message, MapInfo.class);
        }

        new BukkitRunnable() {
            public void run() {
                System.out.println("Received " + info.getUUID());
                if (ServerManager.getServerName().equals(server))
                    return;
                System.out.println("Received 1 " + info.getUUID());

                _worldManager.updateInfo(info);
            }
        }.runTask(_plugin);
    }
}
