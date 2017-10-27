package me.libraryaddict.core;

import java.util.Arrays;

public class ServerType {
    private static ServerType[] _serverTypes = new ServerType[0];

    public static ServerType Build = new ServerType("Build", "Build", 0, 0, 60).setFiles("Build.jar", "WorldEdit.jar",
            "VoxelSniper.jar", "AsyncWorldEdit.jar");

    public static ServerType Disaster = new ServerType("Disasters", "Dis", 1, 4, 60).setGame();

    public static ServerType Hub = new ServerType("Hub", "Hub", 0, 0, 60).setHub();

    public static ServerType SearchAndDestroy = new ServerType("Search and Destroy", "SnD", 2, 4, 60).setGame();

    public static ServerType SurvivalGames = new ServerType("Survival Games", "SG", 4, 8, 24).setGame();

    // public static ServerType Disaster = new ServerType("Survive the Disaster", "Disaster", 5, 30).setGame();

    public static ServerType Vanilla = new ServerType("Vanilla", "Vanilla", 1, 1, 50);

    public static ServerType valueOf(String serverType) {
        for (ServerType type : values()) {
            if (type.getName().equalsIgnoreCase(serverType))
                return type;
        }

        return null;
    }

    public static ServerType[] values() {
        return _serverTypes;
    }

    private int _absoluteMin;

    private boolean _game;
    private int _maxPlayers;
    private int _minPlayers;
    private String _name;
    private boolean _overloading;
    private String[] _plugins = new String[0];
    private String _shortened;
    private boolean _staticPlayers;

    private ServerType(String gameName, String shortened, int absoluteMin, int minPlayers, int maxPlayers) {
        _serverTypes = Arrays.copyOf(_serverTypes, _serverTypes.length + 1);
        _serverTypes[_serverTypes.length - 1] = this;

        _name = gameName;
        _shortened = shortened;
        _absoluteMin = absoluteMin;
        _minPlayers = minPlayers;
        _maxPlayers = maxPlayers;
    }

    public int getAbsoluteMinPlayers() {
        return _absoluteMin;
    }

    public String[] getFiles() {
        return _plugins;
    }

    public int getMaxPlayers() {
        return _maxPlayers;
    }

    public int getMinPlayers() {
        return _minPlayers;
    }

    public String getName() {
        return _name;
    }

    public int getRamUsed() {
        return 512 + (getMaxPlayers() * 35);
    }

    public String getShortened() {
        return _shortened;
    }

    public boolean isGame() {
        return _game;
    }

    public boolean isOverloadSupport() {
        return _overloading;
    }

    public boolean isStaticPlayers() {
        return _staticPlayers;
    }

    public int ordinal() {
        for (int i = 0; i < _serverTypes.length; i++) {
            if (_serverTypes[i] == this)
                return i;
        }

        return -1;
    }

    public ServerType setFiles(String... plugins) {
        _plugins = plugins;

        return this;
    }

    public ServerType setGame() {
        _game = true;

        return setFiles("Arcade.jar");
    }

    public ServerType setHub() {
        return setFiles("Hub.jar");
    }

    public ServerType setStaticMinPlayers() {
        _staticPlayers = true;

        return this;
    }

    @Override
    public String toString() {
        return "[ServerType=" + getName() + "]";
    }
}
