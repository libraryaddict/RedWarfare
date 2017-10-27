package me.libraryaddict.hub.types;

import com.google.gson.Gson;

import me.libraryaddict.hub.managers.PortalManager;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class HubServerListener extends JedisPubSub
{
    private PortalManager _portalManager;

    public HubServerListener(PortalManager portalManager)
    {
        _portalManager = portalManager;

        RedisManager.addListener(this, RedisKey.NOTIFY_SERVER_STATUS);
    }

    @Override
    public void onMessage(String channel, String message)
    {
        ServerInfo serverInfo = new Gson().fromJson(message, ServerInfo.class);

        _portalManager.onReceive(serverInfo);
    }

}
