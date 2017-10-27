package me.libraryaddict.core.player.database;

import com.google.gson.Gson;

import me.libraryaddict.core.player.types.Mute;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisNotifyMute extends DatabaseOperation
{
    public RedisNotifyMute(Mute mute)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_MUTE.getKey(), new Gson().toJson(mute));

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
