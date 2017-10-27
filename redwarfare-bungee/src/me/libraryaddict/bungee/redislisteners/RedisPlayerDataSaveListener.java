package me.libraryaddict.bungee.redislisteners;

import me.libraryaddict.bungee.BungeeManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisPlayerDataSaveListener extends JedisPubSub
{
    private BungeeManager _bungeeManager;

    public RedisPlayerDataSaveListener(BungeeManager bungeeManager)
    {
        _bungeeManager = bungeeManager;

        RedisManager.addListener(RedisPlayerDataSaveListener.this, RedisKey.NOTIFY_SAVED_PLAYER);
    }

    @Override
    public void onMessage(String channel, String message)
    {
        _bungeeManager.notify(message);
    }
}
