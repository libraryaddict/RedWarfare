package me.libraryaddict.core.recharge;

import java.util.Objects;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.utils.UtilMath;

public class RechargeInfo
{
    private Material _boundTo;
    private boolean _displayExp;
    private long _expires;
    private String _message;
    private String _name;
    private long _started = System.currentTimeMillis();

    public RechargeInfo(ItemStack item, long expires)
    {
        _boundTo = item == null ? Material.AIR : item.getType();
        _expires = _started + expires;
    }

    public RechargeInfo(ItemStack item, String message, long expires)
    {
        _boundTo = item == null ? Material.AIR : item.getType();
        _expires = _started + expires;
        _message = message;
    }

    public RechargeInfo(String name, long expires)
    {
        _name = name;
        _expires = _started + expires;
    }

    public RechargeInfo(String name, String message, long expires)
    {
        _name = name;
        _expires = _started + expires;
        _message = message;
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof RechargeInfo))
            return false;

        RechargeInfo info = (RechargeInfo) object;

        if (info.isBoundToItem() && isBoundToItem())
            return Objects.equals(getBoundTo(), info.getBoundTo());

        return Objects.equals(getName(), info.getName());
    }

    public Material getBoundTo()
    {
        return _boundTo;
    }

    public long getExpires()
    {
        return _expires;
    }

    public String getMessage()
    {
        return _message;
    }

    public String getName()
    {
        return _name;
    }

    public double getPercentDone()
    {
        return UtilMath.clamp(1 - (getRemaining() / (double) getTime()), 0, 1);
    }

    public long getRemaining()
    {
        return getExpires() - System.currentTimeMillis();
    }

    public long getStarted()
    {
        return _started;
    }

    public long getTime()
    {
        return getExpires() - getStarted();
    }

    public boolean isBoundToItem()
    {
        return getBoundTo() != null;
    }

    public boolean isDisplayExp()
    {
        return _displayExp;
    }

    public boolean isMessage()
    {
        return getMessage() != null;
    }

    public void setDisplayExp()
    {
        _displayExp = true;
    }
}
