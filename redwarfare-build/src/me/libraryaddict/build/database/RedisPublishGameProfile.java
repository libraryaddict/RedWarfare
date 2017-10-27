package me.libraryaddict.build.database;

import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisPublishGameProfile extends DatabaseOperation {
    public RedisPublishGameProfile(String profile) {
        try (Jedis redis = getRedis()) {
            redis.publish(RedisKey.NOTIFY_BUILD_GAMEPROFILE.getKey(), profile);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
