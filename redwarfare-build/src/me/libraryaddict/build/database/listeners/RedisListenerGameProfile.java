package me.libraryaddict.build.database.listeners;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.FoomapSerializer;
import me.libraryaddict.build.types.LibsGameProfile;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerGameProfile extends JedisPubSub {
    private WorldManager _worldManager;

    public RedisListenerGameProfile(WorldManager worldManager) {
        _worldManager = worldManager;

        new Thread() {
            public void run() {
                RedisManager.addListener(RedisListenerGameProfile.this, RedisKey.NOTIFY_BUILD_GAMEPROFILE);
            }
        }.start();
    }

    public void onMessage(String channel, String message) {
        LibsGameProfile profile = FoomapSerializer.fromGson(message);

        new BukkitRunnable() {
            public void run() {
                _worldManager.onGameProfile(profile);
            }
        }.runTask(_worldManager.getPlugin());
    }
}
