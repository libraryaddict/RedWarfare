package me.libraryaddict.bungee.redislisteners;

import com.google.gson.Gson;

import me.libraryaddict.bungee.BungeeManager;
import me.libraryaddict.network.BungeeStatus;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisBungeeStatusListener extends JedisPubSub
{
    private BungeeManager _bungeeManager;

    public RedisBungeeStatusListener(BungeeManager bungeeManager)
    {
        _bungeeManager = bungeeManager;

        new Thread()
        {
            public void run()
            {
                RedisManager.addListener(RedisBungeeStatusListener.this, RedisKey.NOTIFY_BUNGEE_STATUS);
            }
        }.start();
    }

    @Override
    public void onMessage(String channel, String message)
    {
        BungeeStatus status = new Gson().fromJson(message, BungeeStatus.class);

        if (status.getName().equals(_bungeeManager.getName()))
            return;

        _bungeeManager.addStatus(status);
    }
}
