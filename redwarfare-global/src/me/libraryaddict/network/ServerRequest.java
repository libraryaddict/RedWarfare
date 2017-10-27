package me.libraryaddict.network;

import java.util.HashMap;
import java.util.UUID;

public class ServerRequest
{
    private HashMap<String, String> _files = new HashMap<String, String>();
    private String _makeServer;
    private int _maxPlayers;
    private int _mbRam;
    private String _serverName;
    private String _serverType;
    private UUID _uuid = UUID.randomUUID();

    public ServerRequest(String serverType, String serverName, int mbRam, int maxPlayers)
    {
        _serverType = serverType;
        _serverName = serverName;
        _mbRam = mbRam;
        _maxPlayers = maxPlayers;
    }
    
    public void addFile(String copyFrom, String copyTo)
    {
        _files.put(copyFrom, copyTo);
    }

    public HashMap<String, String> getFiles()
    {
        return _files;
    }

    public int getMaxPlayers()
    {
        return _maxPlayers;
    }

    public String getName()
    {
        return _serverName;
    }

    public int getRam()
    {
        return _mbRam;
    }

    public String getServerType()
    {
        return _serverType;
    }

    public String getSpinnerID()
    {
        return _makeServer;
    }

    public UUID getUUID()
    {
        return _uuid;
    }

    public boolean isMakeServer()
    {
        return _makeServer != null;
    }

    public void setSpinner(String bestId)
    {
        _makeServer = bestId;
    }
}
