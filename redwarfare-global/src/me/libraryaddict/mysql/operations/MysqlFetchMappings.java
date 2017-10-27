package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ConcurrentHashMap;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchMappings extends DatabaseOperation
{
    private ConcurrentHashMap<Integer, String> _keys = new ConcurrentHashMap<Integer, String>();

    public MysqlFetchMappings()
    {
        Connection con = null;

        try
        {
            con = getMysql();

            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM mappings");

            rs.beforeFirst();

            while (rs.next())
            {
                _keys.put(rs.getInt("value"), rs.getString("name"));
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

    public ConcurrentHashMap<Integer, String> getKeys()
    {
        return _keys;
    }
}
