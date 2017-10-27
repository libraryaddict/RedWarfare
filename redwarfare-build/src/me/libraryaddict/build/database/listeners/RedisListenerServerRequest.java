package me.libraryaddict.build.database.listeners;

import com.google.gson.Gson;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.BuildInfo;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerServerRequest extends JedisPubSub {
    private JavaPlugin _plugin;
    private WorldManager _serverManager;

    public RedisListenerServerRequest(WorldManager serverManager, JavaPlugin plugin) {
        _serverManager = serverManager;
        _plugin = plugin;

        new BukkitRunnable() {
            public void run() {
                RedisManager.addListener(RedisListenerServerRequest.this, RedisKey.NOTIFY_BUILD_SERVER_REQUEST);
            }
        }.runTaskAsynchronously(plugin);
    }

    public void onMessage(String channel, String message) {
        BuildInfo info = new Gson().fromJson(message, BuildInfo.class);

        new BukkitRunnable() {
            public void run() {
                _serverManager.addOffer(info);
            }
        }.runTask(_plugin);
    }
}
