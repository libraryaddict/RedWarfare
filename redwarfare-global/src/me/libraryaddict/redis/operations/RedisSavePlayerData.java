package me.libraryaddict.redis.operations;

import com.google.gson.GsonBuilder;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisSavePlayerData extends DatabaseOperation
{
    public RedisSavePlayerData(PlayerData playerData, boolean notify)
    {
        try (Jedis redis = getRedis())
        {
            redis.set(RedisKey.PLAYER_DATA.fromParams(playerData.getUUID().toString()).getKey(),
                    new GsonBuilder().serializeNulls().create().toJson(playerData));

            if (notify)
            {
                redis.publish(RedisKey.NOTIFY_SAVED_PLAYER.getKey(), playerData.getUUID().toString());
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
