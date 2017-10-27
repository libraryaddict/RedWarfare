package me.libraryaddict.redis.operations;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.ServerConfirm;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisSpinnerConfirmOrder extends DatabaseOperation
{
    public RedisSpinnerConfirmOrder(ServerConfirm serverConfirm)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_MANAGER_OFFER.getKey(), new Gson().toJson(serverConfirm));
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
