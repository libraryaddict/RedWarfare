package me.libraryaddict.hub.types;

import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.network.ServerInfo.ServerState;

public class Server implements Comparable<Server>
{
    private long _gameStarts;
    private ServerType _gameType;
    private long _lastPing;
    private int _players;
    private String _server;
    private ServerState _serverState;

    public Server(String server, ServerType type)
    {
        _server = server;
        _gameType = type;
    }

    @Override
    public int compareTo(Server o2)
    {
        if (isInProgress() != o2.isInProgress())
        {
            return Boolean.compare(isInProgress(), o2.isInProgress());
        }

        if (getType().isGame() && isInProgress() && o2.isInProgress())
        {
            return -Long.compare(getGameStarts(), o2.getGameStarts());
        }

        if (isFull() != o2.isFull())
        {
            return Boolean.compare(isFull(), o2.isFull());
        }

        if (Math.abs(getGameStarts() - o2.getGameStarts()) > 40000)
        {
            return -Long.compare(getGameStarts(), o2.getGameStarts());
        }

        if (!getType().isGame() && Math.abs(getGameStarts() - o2.getGameStarts()) > 10000)
        {
            return Long.compare(getGameStarts(), o2.getGameStarts());
        }

        if (getPlayers() > 0 && o2.getPlayers() > 0)
            return Long.compare(getGameStarts(), o2.getGameStarts());

        return Integer.compare(getPlayers(), o2.getPlayers());
    }

    public long getGameStarts()
    {
        return _gameStarts;
    }

    public String getName()
    {
        return _server;
    }

    public int getPlayers()
    {
        return _players;
    }

    public ServerType getType()
    {
        return _gameType;
    }

    public boolean isFull()
    {
        return _players >= _gameType.getMaxPlayers();
    }

    public boolean isInProgress()
    {
        return getType().isGame() && getGameStarts() - 1000 < System.currentTimeMillis();
    }

    public boolean isValid()
    {
        return !UtilTime.elasped(_lastPing, 5000);
    }

    public void update(ServerInfo serverInfo)
    {
        _gameStarts = serverInfo.getStarts();
        _lastPing = System.currentTimeMillis();
        _players = serverInfo.getPlayers();
        _serverState = serverInfo.getState();
    }
}
