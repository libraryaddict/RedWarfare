package me.libraryaddict.network;

import java.util.Objects;

public class BungeeStatus
{
    private long _lastUpdated = System.currentTimeMillis();
    private String _name;
    private int _players;

    public BungeeStatus(String name, int players)
    {
        _name = name;
        _players = players;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof BungeeStatus && Objects.equals(((BungeeStatus) obj).getName(), getName());
    }

    public long getLastUpdated()
    {
        return _lastUpdated;
    }

    public String getName()
    {
        return _name;
    }

    public int getPlayers()
    {
        return _players;
    }
}
