package me.libraryaddict.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.Pair;

public class PlayerHistory
{
    private HashMap<String, Pair<Long, Long>> _ips = new HashMap<String, Pair<Long, Long>>();
    private HashMap<String, Pair<Long, Long>> _names = new HashMap<String, Pair<Long, Long>>();
    private UUID _uuid;

    public PlayerHistory(ResultSet rs) throws SQLException
    {
        rs.beforeFirst();

        while (rs.next())
        {
            _uuid = UUID.fromString(rs.getString("uuid"));

            if (KeyMappings.getKey(rs.getInt("type")).equals("Name"))
            {
                _names.put(rs.getString("info"),
                        Pair.of(rs.getTimestamp("first_used").getTime(), rs.getTimestamp("last_used").getTime()));
            }
            else
            {
                _ips.put(rs.getString("info"),
                        Pair.of(rs.getTimestamp("first_used").getTime(), rs.getTimestamp("last_used").getTime()));
            }
        }
    }

    public HashMap<String, Pair<Long, Long>> getIPs()
    {
        return _ips;
    }

    public long getLastJoined()
    {
        long oldest = 0;

        for (Pair<Long, Long> pair : _names.values())
        {
            if (pair.getValue() < oldest)
                continue;

            oldest = pair.getValue();
        }

        return oldest;
    }

    public String getName()
    {
        String name = null;

        for (String newName : _names.keySet())
        {
            if (name != null && _names.get(name).getValue() > _names.get(newName).getValue())
                continue;

            name = newName;
        }

        return name;
    }

    public UUID getUUID()
    {
        return _uuid;
    }

}
