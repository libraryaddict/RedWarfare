package me.libraryaddict.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class DatabaseModifyCurrency extends DatabaseOperation
{
    public DatabaseModifyCurrency(UUID uuid, String type, String reason, long toAdd, boolean notifyRedis)
    {
        if (toAdd == 0)
            return;

        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO currency (uuid, type, amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount = amount + ?;");
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, KeyMappings.getKey(type));
            stmt.setLong(3, toAdd);
            stmt.setLong(4, toAdd);

            stmt.execute();

            stmt = con.prepareStatement("INSERT INTO currency_log (uuid, type, reason, amount, date) VALUES (?,?,?,?,?)");
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, KeyMappings.getKey(type));
            stmt.setInt(3, KeyMappings.getKey(reason));
            stmt.setLong(4, toAdd);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            stmt.execute();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        if (notifyRedis)
        {
            try (Jedis redis = getRedis())
            {
                redis.publish(RedisKey.NOTIFY_CURRENCY.getKey(), uuid.toString() + ":" + type + ":" + toAdd);
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }

        setSuccess();
    }

}
