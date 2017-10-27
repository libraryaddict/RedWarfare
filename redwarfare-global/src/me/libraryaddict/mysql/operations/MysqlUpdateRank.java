package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class MysqlUpdateRank extends DatabaseOperation
{
    private Pair<String, Integer> _mapping;

    public MysqlUpdateRank(UUID uuid, Rank rank, long expires)
    {
        try (Connection con = getMysql())
        {
            if (expires >= 0)
            {
                PreparedStatement stmt = con.prepareStatement(
                        "INSERT INTO ranks (uuid, rank, expires, display) VALUES (?,?,?,1) ON DUPLICATE KEY UPDATE expires = ?");

                stmt.setString(1, uuid.toString());
                stmt.setInt(2, KeyMappings.getKey(rank.name()));
                stmt.setTimestamp(3, new Timestamp(expires));
                stmt.setTimestamp(4, new Timestamp(expires));
                stmt.execute();
            }
            else
            {
                con.createStatement()
                        .execute("DELETE FROM ranks WHERE uuid = '" + uuid + "' AND rank = " + KeyMappings.getKey(rank.name()));
            }

            Jedis redis = getRedis();

            redis.publish(RedisKey.NOTIFY_RANK_UPDATE.getKey(), uuid + ":" + rank.name() + ":" + expires);

            redis.close();

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public Pair<String, Integer> getMapping()
    {
        return _mapping;
    }
}
