package me.libraryaddict.redis.operations;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.BungeeStatus;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisSaveBungeeStatus extends DatabaseOperation
{
    public RedisSaveBungeeStatus(BungeeStatus status)
    {
        Jedis redis = null;

        try
        {
            redis = getRedis();

            redis.set(RedisKey.BUNGEE_STATUS.getKey(), new Gson().toJson(status));

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
        finally
        {
            try
            {
                if (redis != null)
                {
                    redis.close();
                }
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }
    }
}
