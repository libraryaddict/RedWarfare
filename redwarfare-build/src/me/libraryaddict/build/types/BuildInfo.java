package me.libraryaddict.build.types;

import me.libraryaddict.core.Pair;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BuildInfo implements Comparable<BuildInfo> {
    private boolean _createServer;
    private String _desc;
    private boolean _hasServer;
    private Pair<UUID, String> _invoker;
    private String _mapSettings;
    private String _mapType;
    private UUID _mapUUID;
    private String _serverOffering;
    private UUID _uniqueUUID;
    private int _worldsMade;

    public BuildInfo(Player player, UUID uuid) {
        _uniqueUUID = UUID.randomUUID();
        _mapUUID = uuid;
        _invoker = player == null ? null : Pair.of(player.getUniqueId(), player.getName());
    }

    public BuildInfo(Player player, UUID uuid, String desc, String mapType, String mapSettings) {
        this(player, uuid);

        _desc = desc;
        _mapType = mapType;
        _mapSettings = mapSettings;
    }

    @Override
    public int compareTo(BuildInfo o) {
        return Integer.compare(o.getServers(), getServers());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BuildInfo other = (BuildInfo) obj;
        if (_mapUUID == null) {
            if (other._mapUUID != null)
                return false;
        } else if (!_mapUUID.equals(other._mapUUID))
            return false;
        if (_createServer != other._createServer)
            return false;
        if (_desc == null) {
            if (other._desc != null)
                return false;
        } else if (!_desc.equals(other._desc))
            return false;
        if (_hasServer != other._hasServer)
            return false;
        if (_invoker == null) {
            if (other._invoker != null)
                return false;
        } else if (!_invoker.equals(other._invoker))
            return false;
        if (_mapSettings == null) {
            if (other._mapSettings != null)
                return false;
        } else if (!_mapSettings.equals(other._mapSettings))
            return false;
        if (_mapType == null) {
            if (other._mapType != null)
                return false;
        } else if (!_mapType.equals(other._mapType))
            return false;
        if (_serverOffering == null) {
            if (other._serverOffering != null)
                return false;
        } else if (!_serverOffering.equals(other._serverOffering))
            return false;
        if (_worldsMade != other._worldsMade)
            return false;
        return true;
    }

    public String getDesc() {
        return _desc;
    }

    public Pair<UUID, String> getInvoker() {
        return _invoker;
    }

    public UUID getMap() {
        return _mapUUID;
    }

    public String getMapSettings() {
        return _mapSettings;
    }

    public String getMapType() {
        return _mapType;
    }

    public String getOffer() {
        return _serverOffering;
    }

    public int getServers() {
        return _worldsMade;
    }

    public UUID getUUID() {
        return _uniqueUUID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_mapUUID == null) ? 0 : _mapUUID.hashCode());
        result = prime * result + (_createServer ? 1231 : 1237);
        result = prime * result + ((_desc == null) ? 0 : _desc.hashCode());
        result = prime * result + (_hasServer ? 1231 : 1237);
        result = prime * result + ((_invoker == null) ? 0 : _invoker.hashCode());
        result = prime * result + ((_mapSettings == null) ? 0 : _mapSettings.hashCode());
        result = prime * result + ((_mapType == null) ? 0 : _mapType.hashCode());
        result = prime * result + ((_serverOffering == null) ? 0 : _serverOffering.hashCode());
        result = prime * result + _worldsMade;
        return result;
    }

    public boolean hasInvoker() {
        return _invoker != null;
    }

    public boolean isCreateServer() {
        return _createServer;
    }

    public boolean isServerExists() {
        return _hasServer;
    }

    public void setCreateServer(boolean createServer) {
        _createServer = createServer;
    }

    public void setHasServer(boolean hasServer) {
        _hasServer = hasServer;
    }

    public void setOffer(String name, int serversRunning) {
        _serverOffering = name;
        _worldsMade = serversRunning;
    }

    @Override
    public String toString() {
        return "BuildInfo [_mapUUID=" + _mapUUID + ", _createServer=" + _createServer + ", _desc=" + _desc + ", " +
                "_hasServer=" + _hasServer + ", _invoker=" + _invoker + ", _mapSettings=" + _mapSettings + ", " +
                "_mapType=" + _mapType + ", _serverOffering=" + _serverOffering + ", _worldsMade=" + _worldsMade + "," +
                "" + "" + " _uniqueUUID=" + _uniqueUUID + "]";
    }
}
