package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerStats;

public class MysqlLoadPlayerStats extends DatabaseOperation
{
    private PlayerStats _playerStats;

    public MysqlLoadPlayerStats(UUID uuid)
    {
        try (Connection con = getMysql())
        {
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT `type`, `value` FROM stats WHERE uuid = '" + uuid.toString() + "'");

            _playerStats = new PlayerStats(uuid, rs);

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public PlayerStats getStats()
    {
        return _playerStats;
    }

}
