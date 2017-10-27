package me.libraryaddict.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import me.libraryaddict.core.C;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlSavePlayerData;
import me.libraryaddict.redis.operations.RedisSavePlayerData;

public class PlayerData
{
    private String _currentServer;
    private ArrayList<Integer> _displayedRanks = new ArrayList<Integer>();
    private ArrayList<UUID> _ignoring = new ArrayList<UUID>();
    private long _muteExpires = -1;
    private String _muter;
    private String _muteReason;
    private HashMap<Integer, Long> _ownedRanks = new HashMap<Integer, Long>();
    private PlayerPrefs _playerPrefs = new PlayerPrefs();
    private UUID _uuid;

    public PlayerData(UUID uuid)
    {
        _uuid = uuid;
    }

    public PlayerData clone()
    {
        PlayerData data = new PlayerData(getUUID());
        data._displayedRanks = (ArrayList<Integer>) _displayedRanks.clone();
        data._ownedRanks = (HashMap<Integer, Long>) _ownedRanks.clone();
        data._currentServer = _currentServer;
        data._playerPrefs = getPrefs().clone();
        data._muteExpires = _muteExpires;
        data._muter = _muter;
        data._muteReason = _muteReason;
        data._playerPrefs = _playerPrefs.clone();

        return data;
    }

    public ArrayList<Integer> getDisplayedRanks()
    {
        return _displayedRanks;
    }

    public ArrayList<UUID> getIgnoring()
    {
        if (_ignoring == null)
            _ignoring = new ArrayList<UUID>();

        return _ignoring;
    }

    public String getMuted()
    {
        return C.Blue + "You have been muted by " + _muter + (_muteReason != null ? " for " + _muteReason : "")
                + (_muteExpires != 0 ? ", this will expire in "
                        + UtilNumber.getTime(_muteExpires - System.currentTimeMillis(), TimeUnit.MILLISECONDS) : "");
    }

    public HashMap<Integer, Long> getOwnedRanks()
    {
        return _ownedRanks;
    }

    public PlayerPrefs getPrefs()
    {
        return _playerPrefs;
    }

    public String getServer()
    {
        return _currentServer;
    }

    public UUID getUUID()
    {
        return _uuid;
    }

    public boolean isMuted()
    {
        if (_muteExpires > 0 && UtilTime.elasped(_muteExpires))
        {
            _muter = null;
            _muteExpires = -1;
            _muteReason = null;

            save();
        }

        return _muteExpires != -1;
    }

    public void save()
    {
        PlayerData playerData = clone();

        new Thread()
        {
            public void run()
            {
                System.out.println("Writing " + getUUID().toString() + " PlayerData");

                new RedisSavePlayerData(playerData, false);
                new MysqlSavePlayerData(playerData);
            }
        }.start();
    }

    public void setMute(String muter, String reason, long expires)
    {
        _muter = muter;
        _muteExpires = expires;
        _muteReason = reason;
    }

    public void setPrefs(PlayerPrefs prefs)
    {
        _playerPrefs = prefs;
    }

    public void setServer(String newServer)
    {
        _currentServer = newServer;
    }
}
