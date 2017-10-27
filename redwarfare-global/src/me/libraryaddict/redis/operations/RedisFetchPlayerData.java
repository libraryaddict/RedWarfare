package me.libraryaddict.redis.operations;

import java.util.UUID;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisFetchPlayerData extends DatabaseOperation
{
    private PlayerData _playerData;

    public RedisFetchPlayerData(UUID uuid)
    {
        try (Jedis redis = getRedis())
        {
            _playerData = new Gson().fromJson(redis.get(RedisKey.PLAYER_DATA.fromParams(uuid.toString()).getKey()),
                    PlayerData.class);
            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public PlayerData getPlayerData()
    {
        return _playerData;
    }
}
