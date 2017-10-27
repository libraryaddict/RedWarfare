package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerOwned;

public class MysqlLoadOwned extends DatabaseOperation
{
    private PlayerOwned _playerData;

    public MysqlLoadOwned(UUID uuid)
    {
        _playerData = new PlayerOwned(uuid);

        try (Connection con = getMysql())
        {
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT `type` FROM owned WHERE uuid = '" + uuid.toString() + "'");

            _playerData.load(rs);

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public PlayerOwned getOwned()
    {
        return _playerData;
    }

}
