package me.libraryaddict.redis.operations;

import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerUpdate extends JedisPubSub
{
    private String _key;
    private Runnable _runnable;

    public RedisListenerUpdate(String key, Runnable run)
    {
        _runnable = run;
        _key = key;

        new Thread()
        {
            public void run()
            {
                RedisManager.addListener(RedisListenerUpdate.this, RedisKey.NOTIFY_UPDATE);
            }
        }.start();
    }

    public void onMessage(String channel, String message)
    {
        if (!message.equals(_key))
            return;

        _runnable.run();
    }
}
