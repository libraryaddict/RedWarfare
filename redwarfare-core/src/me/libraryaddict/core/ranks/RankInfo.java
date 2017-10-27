package me.libraryaddict.core.ranks;

import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilTime;

public class RankInfo
{
    private boolean _display;
    private long _expires;
    private Rank _rank;

    public RankInfo(Rank rank, long expires, boolean isDisplay)
    {
        _rank = rank;
        _expires = expires;
        _display = isDisplay;
    }

    public long getExpires()
    {
        return _expires;
    }

    public Rank getRank()
    {
        return _rank;
    }

    public boolean hasExpired()
    {
        return getExpires() > 0 && UtilTime.elasped(getExpires());
    }

    public boolean isDisplay()
    {
        return _display;
    }

    public void setDisplay(boolean display)
    {
        _display = display;
    }

    public void setExpires(long newExpires)
    {
        _expires = newExpires;
    }
}
