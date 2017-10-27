package me.libraryaddict.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import me.libraryaddict.KeyMappings;

public class PlayerOwned
{
    private ArrayList<String> _owned = new ArrayList<String>();
    private UUID _uuid;

    public PlayerOwned(UUID uuid)
    {
        _uuid = uuid;
    }

    public void add(String pref)
    {
        _owned.add(pref);
    }

    public PlayerOwned clone()
    {
        PlayerOwned stats = new PlayerOwned(getUUID());
        stats._owned = (ArrayList<String>) _owned.clone();

        return stats;
    }

    public ArrayList<String> getOwned()
    {
        return _owned;
    }

    public UUID getUUID()
    {
        return _uuid;
    }

    public boolean has(String pref)
    {
        return _owned.contains(pref);
    }

    public void load(ResultSet rs) throws SQLException
    {
        rs.beforeFirst();

        while (rs.next())
        {
            _owned.add(KeyMappings.getKey(rs.getInt("type")));
        }
    }

}
