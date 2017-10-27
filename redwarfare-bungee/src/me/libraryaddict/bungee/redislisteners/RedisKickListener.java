package me.libraryaddict.bungee.redislisteners;

import me.libraryaddict.bungee.BungeeManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisKickListener extends JedisPubSub
{
    private BungeeManager _bungeeManager;

    public RedisKickListener(BungeeManager bungeeManager)
    {
        _bungeeManager = bungeeManager;

        RedisManager.addListener(this, RedisKey.NOTIFY_KICK_PLAYER);
    }

    @Override
    public void onMessage(String channel, String message)
    {
        _bungeeManager.kickPlayer(message.substring(0, message.indexOf(":")), message.substring(message.indexOf(":") + 1));
    }
}
