package me.libraryaddict.bungee.redislisteners;

import com.google.gson.Gson;

import me.libraryaddict.bungee.BungeeManager;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerServerStatus extends JedisPubSub
{
    private BungeeManager _serverManager;

    public RedisListenerServerStatus(BungeeManager serverManager)
    {
        _serverManager = serverManager;

        new Thread()
        {
            public void run()
            {
                RedisManager.addListener(RedisListenerServerStatus.this, RedisKey.NOTIFY_SERVER_STATUS);
            }
        }.start();
    }

    @Override
    public void onMessage(String channel, String message)
    {
        _serverManager.onMessage(new Gson().fromJson(message, ServerInfo.class));
    }
}
