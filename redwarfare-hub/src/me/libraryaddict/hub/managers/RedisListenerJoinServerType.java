package me.libraryaddict.hub.managers;

import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.ServerType;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerJoinServerType extends JedisPubSub
{
    private PortalManager _portalManager;

    public RedisListenerJoinServerType(PortalManager portalManager)
    {
        _portalManager = portalManager;

        RedisManager.addListener(this, RedisKey.NOTIFY_JOIN_GAME);
    }

    public void onMessage(String channel, String message)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                _portalManager.joinGame(UUID.fromString(message.split(":")[0]), ServerType.valueOf(message.split(":")[1]));
            }
        }.runTask(_portalManager.getPlugin());
    }
}
