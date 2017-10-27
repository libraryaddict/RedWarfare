package me.libraryaddict.redis.operations;

import java.util.ArrayList;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.network.BungeeStatus;
import me.libraryaddict.network.DatabaseOperation;
import redis.clients.jedis.Jedis;

public class RedisFetchBungeeServers extends DatabaseOperation
{
    private ArrayList<BungeeStatus> _bungeeStatus = new ArrayList<BungeeStatus>();

    public RedisFetchBungeeServers()
    {
        Jedis redis = null;

        try
        {
            redis = getRedis();

            for (String key : redis.keys("ServerStatus.Bungee.*"))
            {
                BungeeStatus status = new Gson().fromJson(redis.get(key), BungeeStatus.class);

                if (UtilTime.elasped(status.getLastUpdated(), 30000))
                    continue;

                _bungeeStatus.add(status);
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
        finally
        {
            try
            {
                redis.close();
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }
    }

    public ArrayList<BungeeStatus> getStatus()
    {
        return _bungeeStatus;
    }

}
