package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerHistory;

public class MysqlFetchPlayerHistory extends DatabaseOperation
{
    private PlayerHistory _playerHistory;

    // Can be UUID, IP, Name
    public MysqlFetchPlayerHistory(UUID uuid)
    {
        Connection con = null;
        try
        {
            con = getMysql();

            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM playerinfo WHERE uuid = '" + uuid + "'");

            if (rs.first())
            {
                _playerHistory = new PlayerHistory(rs);
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

    public PlayerHistory getHistory()
    {
        return _playerHistory;
    }
}
