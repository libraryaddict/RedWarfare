package me.libraryaddict.bungee.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlSavePlayerHistory extends DatabaseOperation
{
    public MysqlSavePlayerHistory(UUID uuid, String name, String ip)
    {
        Connection con = null;

        try
        {
            con = getMysql();

            PreparedStatement stmt = con
                    .prepareStatement("INSERT INTO playerinfo (uuid, type, info, first_used, last_used) VALUES ('" + uuid + "',"
                            + KeyMappings.getKey("Name") + ",?,?,?) ON DUPLICATE KEY UPDATE last_used = ?;");

            stmt.setString(1, name);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            stmt.execute();

            stmt = con.prepareStatement("INSERT INTO playerinfo (uuid, type, info, first_used, last_used) VALUES ('" + uuid + "',"
                    + KeyMappings.getKey("IP") + ",?,?,?) ON DUPLICATE KEY UPDATE last_used = ?;");

            stmt.setString(1, ip);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

            stmt.execute();

            ResultSet rs = stmt.executeQuery("SELECT id FROM playerinfo WHERE uuid = '" + uuid + "' AND type = "
                    + KeyMappings.getKey("IP") + " ORDER BY last_used DESC LIMIT 5, 1000");

            rs.beforeFirst();

            ArrayList<String> delete = new ArrayList<String>();

            while (rs.next())
            {
                delete.add("" + rs.getInt("id"));
            }

            if (!delete.isEmpty())
            {
                stmt.execute("DELETE from playerinfo WHERE id IN (" + UtilString.join(delete, ",") + ");");
            }

            // We don't need to remove playernames..

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
