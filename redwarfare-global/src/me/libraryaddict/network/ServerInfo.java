package me.libraryaddict.network;

import me.libraryaddict.core.ServerType;

public class ServerInfo
{
    public static enum ServerState
    {
        /**
         * When its no longer in progress, but its not ready
         */
        ENDED,

        /**
         * When you can't join
         */
        IN_PROGRESS,

        /**
         * When its joinable and mergable
         */
        JOINABLE,

        /**
         * Its joinable, but doesn't want to be merged
         */
        JOINABLE_NO_MERGE;
    }

    private long _gameStarts;
    private String _gameType;
    private String _ip;
    private boolean _outdated = true;
    private int _players;
    private String _serverName;
    private ServerState _serverState;

    public ServerInfo(ServerState serverState, String serverName, ServerType gameType, int players, long gameStarts, String ip,
            boolean outdated)
    {
        _serverState = serverState;
        _serverName = serverName;
        _gameType = gameType.getName();
        _players = players;
        _gameStarts = gameStarts;
        _ip = ip;
        _outdated = outdated;
    }

    public String getIP()
    {
        return _ip;
    }

    public String getName()
    {
        return _serverName;
    }

    public int getPlayers()
    {
        return _players;
    }

    public long getStarts()
    {
        return _gameStarts;
    }

    public ServerState getState()
    {
        return _serverState;
    }

    public ServerType getType()
    {
        for (ServerType gameType : ServerType.values())
        {
            if (gameType.getName().equals(_gameType))
                return gameType;
        }

        return null;
    }

    public boolean isOutdated()
    {
        return _outdated;
    }
}
