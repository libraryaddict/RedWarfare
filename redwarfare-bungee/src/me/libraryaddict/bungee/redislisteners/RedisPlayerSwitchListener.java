package me.libraryaddict.bungee.redislisteners;

import java.util.UUID;

import me.libraryaddict.bungee.BungeeManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisPlayerSwitchListener extends JedisPubSub
{
    private BungeeManager _bungeeManager;

    public RedisPlayerSwitchListener(BungeeManager bungeeManager)
    {
        _bungeeManager = bungeeManager;

        RedisManager.addListener(this, RedisKey.NOTIFY_SERVER_SWITCH);
    }

    @Override
    public void onMessage(String channel, String message)
    {
        _bungeeManager.switchServer(UUID.fromString(message.split(":")[0]), message.split(":")[1]);
    }
}
