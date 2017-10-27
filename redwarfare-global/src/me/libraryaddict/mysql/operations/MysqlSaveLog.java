package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlSaveLog extends DatabaseOperation
{
    public MysqlSaveLog(String server, String error)
    {
        if (error.length() > 5000)
            error = error.substring(0, 5000);

        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("INSERT IGNORE INTO server_log (`server`, `error`) VALUES (?,?)");

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
