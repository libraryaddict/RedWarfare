package me.libraryaddict.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilTime;

public class BanInfo
{
    public enum BanState
    {
        ACTIVE,

        EXPIRED,

        OVERWRITTEN,

        REMOVED_BY_STAFF;
    }

    private Timestamp _banExpires;
    private String _banned;
    private Timestamp _bannedWhen = new Timestamp(System.currentTimeMillis());
    private String _banner;
    private BanState _banState = BanState.ACTIVE;
    private boolean _isBan;
    private String _reason;

    public BanInfo(ResultSet rs) throws SQLException
    {
        _banned = rs.getString("banned");
        _banner = KeyMappings.getKey(rs.getInt("banned_by"));
        _reason = rs.getString("reason");
        _bannedWhen = rs.getTimestamp("banned_when");
        _banExpires = rs.getTimestamp("ban_expires");
        _isBan = !rs.getBoolean("type");
        _banState = BanState.valueOf(KeyMappings.getKey(rs.getInt("ban_state")));
    }

    public BanInfo(String banned, String banner, String reason, Timestamp banExpires, boolean isBan)
    {
        _banned = banned;
        _banner = banner;
        _reason = reason;
        _banExpires = banExpires;
        _isBan = isBan;
    }

    public Timestamp getBanExpires()
    {
        return _banExpires;
    }

    public String getBanned()
    {
        return _banned;
    }

    public Timestamp getBannedWhen()
    {
        return _bannedWhen;
    }

    public String getBanner()
    {
        return _banner;
    }

    public BanState getBanState()
    {
        return _banState;
    }

    public String getReason()
    {
        return _reason;
    }

    public boolean isBan()
    {
        return _isBan;
    }

    public boolean isExpired()
    {
        return !isPerm() && UtilTime.elasped(getBanExpires());
    }

    public boolean isMute()
    {
        return !isBan();
    }

    public boolean isPerm()
    {
        return getBanExpires().getTime() == 0;
    }

    public boolean isRemoved()
    {
        return getBanState() != BanState.ACTIVE;
    }

    public void setBanState(BanState banState)
    {
        _banState = banState;
    }
}
