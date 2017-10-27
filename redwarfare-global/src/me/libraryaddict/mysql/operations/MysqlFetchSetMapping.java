package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchSetMapping extends DatabaseOperation
{
    private Pair<String, Integer> _mapping;

    public MysqlFetchSetMapping(String key)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("INSERT IGNORE INTO mappings (name) VALUES (?)");
            stmt.setString(1, key);

            stmt.execute();

            stmt = con.prepareStatement("SELECT * FROM mappings WHERE name = ?");
            stmt.setString(1, key);

            ResultSet rs = stmt.executeQuery();

            if (!rs.first())
            {
                Thread.dumpStack();
                return;
            }

            _mapping = Pair.of(key, rs.getInt("value"));

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public Pair<String, Integer> getMapping()
    {
        return _mapping;
    }
}
