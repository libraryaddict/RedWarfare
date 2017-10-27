package me.libraryaddict.build.database;

import com.google.gson.GsonBuilder;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisPublishMapInfo extends DatabaseOperation {
    public RedisPublishMapInfo(MapInfo... mapInfo) {
        try (Jedis redis = getRedis()) {
            for (MapInfo info : mapInfo) {
                System.out.println("Published " + info.getUUID());
                redis.publish(RedisKey.NOTIFY_MAP_INFO.fromParams(ServerManager.getServerName()).getKey(),
                        new GsonBuilder().serializeNulls().create().toJson(info));
            }
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
