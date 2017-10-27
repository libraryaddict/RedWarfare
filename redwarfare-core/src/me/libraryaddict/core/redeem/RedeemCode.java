package me.libraryaddict.core.redeem;

import java.sql.Timestamp;
import java.util.UUID;

import me.libraryaddict.core.Pair;

public class RedeemCode
{
    private String _code;
    private Timestamp _createdWhen;
    private Timestamp _redeemedWhen;
    private Pair<UUID, String> _redeemer;
    private String _type;

    public RedeemCode(Timestamp created, String code, String type)
    {
        _createdWhen = created;
        _code = code;
        _type = type;
    }

    public String getCode()
    {
        return _code;
    }

    public Timestamp getCreatedWhen()
    {
        return _createdWhen;
    }

    public Timestamp getRedeemedWhen()
    {
        return _redeemedWhen;
    }

    public Pair<UUID, String> getRedeemer()
    {
        return _redeemer;
    }

    public String getType()
    {
        return _type;
    }

    public boolean isRedeemed()
    {
        return getRedeemer() != null;
    }

    public void setRedeemed(Timestamp redeemedWhen, Pair<UUID, String> redeemer)
    {
        _redeemedWhen = redeemedWhen;
        _redeemer = redeemer;
    }
}
