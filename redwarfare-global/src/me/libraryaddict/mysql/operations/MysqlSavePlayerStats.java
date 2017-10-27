package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map.Entry;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerStats;

public class MysqlSavePlayerStats extends DatabaseOperation
{
    public MysqlSavePlayerStats(PlayerStats... playerStats)
    {
        for (PlayerStats playerStat : playerStats)
        {
            if (playerStat.isReadOnly())
            {
                throw new IllegalArgumentException("Cannot pass read only Player Stats to be saved");
            }
        }

        Connection con = null;

        try
        {
            con = getMysql();
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO stats (uuid, type, value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE value = ? + value;");

            for (PlayerStats playerStat : playerStats)
            {
                stmt.setString(1, playerStat.getUUID().toString());

                for (Entry<String, Long> stats : playerStat.getStats().entrySet())
                {
                    stmt.setInt(2, KeyMappings.getKey(stats.getKey()));
                    stmt.setLong(3, stats.getValue());
                    stmt.setLong(4, stats.getValue());

                    stmt.execute();
                }
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
                con.close();
            }
            catch (SQLException e)
            {
                UtilError.handle(e);
            }
        }
    }

}
