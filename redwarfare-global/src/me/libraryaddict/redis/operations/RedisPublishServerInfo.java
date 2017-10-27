package me.libraryaddict.redis.operations;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisPublishServerInfo extends DatabaseOperation
{
    public RedisPublishServerInfo(ServerInfo serverInfo)
    {
        Jedis redis = getRedis();

        try
        {
            redis.publish(RedisKey.NOTIFY_SERVER_STATUS.getKey(), new Gson().toJson(serverInfo));

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
        finally
        {
            redis.close();
        }
    }
}
