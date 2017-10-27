package me.libraryaddict.redis.operations;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisMergeServer extends DatabaseOperation
{
    public RedisMergeServer(String mergeFrom, String mergeTo)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_SERVER_MERGE.fromParams(mergeFrom).getKey(), mergeTo);
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
