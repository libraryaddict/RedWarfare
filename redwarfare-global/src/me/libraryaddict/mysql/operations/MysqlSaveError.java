package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlSaveError extends DatabaseOperation
{
    public MysqlSaveError(String server, String error)
    {
        if (error.contains("Plugin attempted to register task while disabled"))
            return;

        if (error.length() > 5000)
            error = error.substring(0, 5000);

        if (error.equals("java.lang.Exception: Stack trace"))
            return;

        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("SELECT `server` FROM errors WHERE error = ? LIMIT 0,1");
            stmt.setString(1, error);

            ResultSet rs = stmt.executeQuery();

            if (rs.first())
            {
                setSuccess();
                return;
            }

            stmt = con.prepareStatement("INSERT IGNORE INTO errors (`server`, `error`) VALUES (?,?)");

            stmt.setString(1, server);
            stmt.setString(2, error);
            stmt.execute();

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
