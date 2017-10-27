package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchCurrency extends DatabaseOperation
{
    private HashMap<String, Long> _items = new HashMap<String, Long>();

    public MysqlFetchCurrency(UUID uuid)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM currency WHERE uuid = ?");
            stmt.setString(1, uuid.toString());

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next())
            {
                _items.put(KeyMappings.getKey(rs.getInt("type")), rs.getLong("amount"));
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public HashMap<String, Long> getItems()
    {
        return _items;
    }
}
