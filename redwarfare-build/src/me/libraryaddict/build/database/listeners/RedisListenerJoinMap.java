package me.libraryaddict.build.database.listeners;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.Pair;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public class RedisListenerJoinMap extends JedisPubSub {
    private JavaPlugin _plugin;
    private WorldManager _serverManager;

    public RedisListenerJoinMap(WorldManager serverManager, JavaPlugin plugin) {
        _serverManager = serverManager;
        _plugin = plugin;

        new BukkitRunnable() {
            public void run() {
                RedisManager.addListener(RedisListenerJoinMap.this, RedisKey.NOTIFY_BUILD_JOIN_MAP);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void onMessage(String channel, String message) {
        String[] split = message.split(":");

        UUID uuid = UUID.fromString(split[0]);
        UUID map = UUID.fromString(split[1]);

        new BukkitRunnable() {
            public void run() {
                _serverManager.getSendToMap().add(Pair.of(Pair.of(uuid, map), System.currentTimeMillis()));
            }
        }.runTask(_plugin);
    }
}
