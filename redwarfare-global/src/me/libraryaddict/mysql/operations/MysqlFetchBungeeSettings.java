package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchBungeeSettings extends DatabaseOperation
{
    private BungeeSettings _bungeeSettings = new BungeeSettings();

    public MysqlFetchBungeeSettings()
    {
        try (Connection con = getMysql())
        {
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM bungee");

            BungeeSettings settings = new BungeeSettings(rs);

            _bungeeSettings = settings;

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public BungeeSettings getSettings()
    {
        return _bungeeSettings;
    }
}
