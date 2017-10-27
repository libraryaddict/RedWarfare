package me.libraryaddict.network;

import java.util.UUID;

public class ServerConfirm
{
    private boolean _outdated;
    private int _servers;
    private String _spinner;
    private UUID _uuid;

    public ServerConfirm(UUID uuid, String spinner, int servers, boolean outdated)
    {
        _uuid = uuid;
        _servers = servers;
        _outdated = outdated;
        _spinner = spinner;
    }

    public int getServers()
    {
        return _servers;
    }

    public String getSpinner()
    {
        return _spinner;
    }

    public UUID getUUID()
    {
        return _uuid;
    }

    public boolean isOutdated()
    {
        return _outdated;
    }
}
