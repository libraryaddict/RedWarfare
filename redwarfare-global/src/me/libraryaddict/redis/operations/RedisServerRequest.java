package me.libraryaddict.redis.operations;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.ServerRequest;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisServerRequest extends DatabaseOperation
{
    public RedisServerRequest(ServerRequest serverRequest)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_SPINNERS_SERVER.getKey(), new Gson().toJson(serverRequest));
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
