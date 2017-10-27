package me.libraryaddict.redis.operations;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisKickPlayer extends DatabaseOperation
{
    public RedisKickPlayer(String uuidOrIP, String message)
    {
        Jedis redis = null;

        try
        {
            redis = getRedis();

            redis.publish(RedisKey.NOTIFY_KICK_PLAYER.getKey(), uuidOrIP + ":" + message);

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
