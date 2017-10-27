package me.libraryaddict.build.database;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class RedisJoinMap extends DatabaseOperation {
    public RedisJoinMap(UUID player, UUID map) {
        try (Jedis redis = getRedis()) {
            redis.publish(RedisKey.NOTIFY_BUILD_JOIN_MAP.getKey(), player.toString() + ":" + map.toString());
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
