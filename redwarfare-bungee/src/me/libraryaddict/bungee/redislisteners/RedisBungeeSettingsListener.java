package me.libraryaddict.bungee.redislisteners;

import com.google.gson.Gson;

import me.libraryaddict.bungee.BungeeManager;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisBungeeSettingsListener extends JedisPubSub
{
    private BungeeManager _bungeeManager;

    public RedisBungeeSettingsListener(BungeeManager bungeeManager)
    {
        _bungeeManager = bungeeManager;

        new Thread()
        {
            public void run()
            {
                RedisManager.addListener(RedisBungeeSettingsListener.this, RedisKey.NOTIFY_BUNGEE_SETTINGS);
            }
        }.start();
    }

    @Override
    public void onMessage(String channel, String message)
    {
        BungeeSettings settings = new Gson().fromJson(message, BungeeSettings.class);

        _bungeeManager.setSettings(settings);
    }
}
