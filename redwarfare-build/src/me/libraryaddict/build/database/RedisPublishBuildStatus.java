package me.libraryaddict.build.database;

import com.google.gson.Gson;
import me.libraryaddict.build.types.BuildInfo;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisPublishBuildStatus extends DatabaseOperation {
    public RedisPublishBuildStatus(BuildInfo info) {
        try (Jedis redis = getRedis()) {
            redis.publish(RedisKey.NOTIFY_BUILD_SERVER_REQUEST.getKey(), new Gson().toJson(info));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
