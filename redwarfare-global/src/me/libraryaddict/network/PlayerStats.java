package me.libraryaddict.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import me.libraryaddict.KeyMappings;

public class PlayerStats
{
    private boolean _readOnly;
    private HashMap<String, Long> _stats = new HashMap<String, Long>();
    private UUID _uuid;

    public PlayerStats(UUID uuid)
    {
        _uuid = uuid;
    }

    public PlayerStats(UUID uuid, ResultSet rs) throws SQLException
    {
        _readOnly = true;

        _uuid = uuid;

        rs.beforeFirst();

        while (rs.next())
        {
            setStat(KeyMappings.getKey(rs.getInt("type")), rs.getLong("value"));
        }
    }

    public PlayerStats clone()
    {
        PlayerStats stats = new PlayerStats(getUUID());
        stats._readOnly = _readOnly;
        stats._stats = (HashMap<String, Long>) _stats.clone();

        return stats;
    }

    public HashMap<String, Long> combine(PlayerStats otherStats)
    {
        HashMap<String, Long> stats = (HashMap<String, Long>) getStats().clone();
        HashMap<String, Long> newStats = otherStats.getStats();

        for (Entry<String, Long> entry : newStats.entrySet())
        {
            stats.put(entry.getKey(), stats.getOrDefault(entry.getKey(), 0L) + entry.getValue());
        }

        return stats;
    }

    public void empty()
    {
        _stats.clear();
    }

    public long getStat(String statName)
    {
        if (_stats.containsKey(statName))
            return _stats.get(statName);

        return 0;
    }

    public HashMap<String, Long> getStats()
    {
        return _stats;
    }

    public UUID getUUID()
    {
        return _uuid;
    }

    public boolean isDirty()
    {
        return !isReadOnly() && !_stats.isEmpty();
    }

    public boolean isReadOnly()
    {
        return _readOnly;
    }

    public void setStat(String statName, long value)
    {
        _stats.put(statName, value);
    }
}
