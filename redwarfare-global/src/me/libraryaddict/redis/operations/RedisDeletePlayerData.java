package me.libraryaddict.redis.operations;

import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import redis.clients.jedis.Jedis;

public class RedisDeletePlayerData extends DatabaseOperation
{
    public RedisDeletePlayerData(UUID uuid)
    {
        try (Jedis redis = getRedis())
        {
            redis.del("PlayerData.Info." + uuid.toString());

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
