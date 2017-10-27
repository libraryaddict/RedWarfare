package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlSaveBungeeSettings extends DatabaseOperation
{
    /**
     * This should be called before the redis update is called. So the mysql updates, then if a server grabs a malformed setup it
     * is notified by redis.
     */
    public MysqlSaveBungeeSettings(BungeeSettings settings)
    {
        Connection con = null;

        try
        {
            con = getMysql();

            Statement stmt = con.createStatement();

            stmt.execute("TRUNCATE TABLE bungee;");

            PreparedStatement pre = con.prepareStatement("INSERT INTO bungee (type, value) VALUES (?,?)");

            pre.setString(1, "max_players");
            pre.setString(2, "" + settings.getMaxPlayers());
            pre.execute();

            pre.setString(1, "total_players");
            pre.setString(2, "" + settings.getTotalPlayers());
            pre.execute();

            pre.setString(1, "throttle");
            pre.setString(2, "" + settings.getThrottle());
            pre.execute();

            pre.setString(1, "whitelist");
            pre.setString(2, "" + settings.isWhitelist());
            pre.execute();

            pre.setString(1, "protocol");
            pre.setString(2, settings.getProtocol());
            pre.execute();

            pre.setString(1, "header");
            pre.setString(2, settings.getHeader());
            pre.execute();

            pre.setString(1, "footer");
            pre.setString(2, settings.getFooter());
            pre.execute();

            pre.setString(1, "motd");

            for (String motd : settings.getMotd())
            {
                pre.setString(2, motd);
                pre.execute();
            }

            pre.setString(1, "players");

            for (String player : settings.getPlayers())
            {
                pre.setString(2, player);
                pre.execute();
            }

            pre.setString(1, "favicon");
            pre.setString(2, settings.getFavIcon());
            pre.execute();

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
                con.close();
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }
    }
}
