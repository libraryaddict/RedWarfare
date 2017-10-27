package me.libraryaddict.redis.operations;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisNotifyBungeeSettings extends DatabaseOperation
{
    public RedisNotifyBungeeSettings(BungeeSettings settings)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_BUNGEE_SETTINGS.getKey(), new Gson().toJson(settings));

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
