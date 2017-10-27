package me.libraryaddict.core.player.types;

import java.util.UUID;

public class Mute
{
    private long _expires;
    private UUID _muted;
    private String _muter;
    private String _reason;

    public Mute(UUID muted, String muter, String reason, long expires)
    {
        _muted = muted;
        _muter = muter;
        _reason = reason;
        _expires = expires;
    }

    public long getExpires()
    {
        return _expires;
    }

    public UUID getMuted()
    {
        return _muted;
    }

    public String getMuter()
    {
        return _muter;
    }

    public String getReason()
    {
        return _reason;
    }
}
