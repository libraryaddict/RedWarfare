package me.libraryaddict.redis.operations;

import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.Jedis;

public class RedisSendMessage extends DatabaseOperation
{
    public RedisSendMessage(UUID uuid, String server)
    {
        try (Jedis redis = RedisManager.getRedis())
        {
            redis.publish(RedisKey.NOTIFY_SEND_MESSAGE.getKey(), uuid.toString() + ":" + server);
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
