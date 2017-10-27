package me.libraryaddict.redis.operations;

import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisNotifySavePlayerData extends DatabaseOperation
{
    public RedisNotifySavePlayerData(String serverName, UUID uuid)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_SAVE_PLAYER.fromParams(serverName).getKey(), uuid.toString());

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
