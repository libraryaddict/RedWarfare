package me.libraryaddict.core.bans.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchAlts extends DatabaseOperation
{
    private HashMap<String, HashMap<UUID, Pair<Long, Long>>> _alts = new HashMap<String, HashMap<UUID, Pair<Long, Long>>>();

    public MysqlFetchAlts(String... ips)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM playerinfo WHERE info = ?");

            for (String ip : ips)
            {
                stmt.setString(1, ip);

                ResultSet rs = stmt.executeQuery();

                rs.beforeFirst();

                while (rs.next())
                {
                    HashMap<UUID, Pair<Long, Long>> hashmap = _alts.get(ip);

                    if (hashmap == null)
                    {
                        _alts.put(ip, hashmap = new HashMap<UUID, Pair<Long, Long>>());
                    }

                    hashmap.put(UUID.fromString(rs.getString("uuid")),
                            Pair.of(rs.getTimestamp("first_used").getTime(), rs.getTimestamp("last_used").getTime()));
                }
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public HashMap<String, HashMap<UUID, Pair<Long, Long>>> getAlts()
    {
        return _alts;
    }
}
