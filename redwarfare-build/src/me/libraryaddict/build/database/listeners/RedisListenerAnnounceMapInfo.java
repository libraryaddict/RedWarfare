package me.libraryaddict.build.database.listeners;

import com.mojang.authlib.GameProfile;
import me.libraryaddict.build.database.RedisPublishGameProfile;
import me.libraryaddict.build.database.RedisPublishMapInfo;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.FoomapSerializer;
import me.libraryaddict.build.types.LibsGameProfile;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class RedisListenerAnnounceMapInfo extends JedisPubSub {
    private JavaPlugin _plugin;
    private WorldManager _serverManager;

    public RedisListenerAnnounceMapInfo(WorldManager serverManager, JavaPlugin plugin) {
        _serverManager = serverManager;
        _plugin = plugin;

        new Thread() {
            public void run() {
                System.out.println("Registered");
                RedisManager.addListener(RedisListenerAnnounceMapInfo.this, RedisKey.NOTIFY_REQUEST_MAP_INFO);
            }
        }.start();
    }

    @Override
    public void onMessage(String channel, String message) {
        new BukkitRunnable() {
            public void run() {
                ArrayList<MapInfo> maps = new ArrayList<MapInfo>();

                for (WorldInfo info : _serverManager.getLoadedWorlds().values()) {
                    maps.add(info.getData());
                }
                System.out.println("Received info " + maps.size());

                if (maps.isEmpty())
                    return;

                new RedisPublishMapInfo(maps.toArray(new MapInfo[0]));

                for (Entry<UUID, HashMap<UUID, GameProfile>> a : _serverManager.getFakePlayers().entrySet()) {
                    for (GameProfile e : a.getValue().values()) {
                        new RedisPublishGameProfile(FoomapSerializer.toGson(new LibsGameProfile(null, a.getKey(), e)));
                    }
                }
            }
        }.runTask(_plugin);
    }
}
