package me.libraryaddict.core.player.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerSendMessage extends JedisPubSub
{
    private JavaPlugin _plugin;

    public RedisListenerSendMessage(JavaPlugin plugin)
    {
        _plugin = plugin;

        RedisManager.addListener(this, RedisKey.NOTIFY_SEND_MESSAGE);
    }

    public void onMessage(String channel, String message)
    {
        UUID uuid = UUID.fromString(message.split(":")[0]);
        String toSend = message.substring(message.indexOf(":") + 1);

        new BukkitRunnable()
        {
            public void run()
            {
                Player player = Bukkit.getPlayer(uuid);

                if (player == null)
                    return;

                player.sendMessage(toSend);
            }
        }.runTask(_plugin);
    }
}
