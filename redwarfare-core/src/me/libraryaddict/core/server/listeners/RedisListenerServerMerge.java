package me.libraryaddict.core.server.listeners;

import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerServerMerge extends JedisPubSub
{
    private ServerManager _serverManager;

    public RedisListenerServerMerge(ServerManager serverManager)
    {
        _serverManager = serverManager;

        RedisManager.addListener(RedisListenerServerMerge.this,
                RedisKey.NOTIFY_SERVER_MERGE.fromParams(serverManager.getServerName()));
    }

    @Override
    public void onMessage(String channel, String message)
    {
        _serverManager.doMerge(message);
    }
}
