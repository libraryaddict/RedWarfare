package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchRankData extends DatabaseOperation
{
    private ArrayList<String> _displayedRanks = new ArrayList<String>();
    private HashMap<String, Long> _ranks = new HashMap<String, Long>();

    public MysqlFetchRankData(UUID uuid)
    {
        Connection con = null;

        try
        {
            con = getMysql();
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM ranks WHERE uuid = '" + uuid.toString() + "'");

            rs.beforeFirst();

            while (rs.next())
            {
                _ranks.put(KeyMappings.getKey(rs.getInt("rank")), rs.getTimestamp("expires").getTime());

                if (rs.getBoolean("display"))
                {
                    getDisplayedRanks().add(KeyMappings.getKey(rs.getInt("rank")));
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

    public ArrayList<String> getDisplayedRanks()
    {
        return _displayedRanks;
    }

    public HashMap<String, Long> getRanks()
    {
        return _ranks;
    }

}
