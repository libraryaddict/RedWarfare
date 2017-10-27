package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Landmine
{
    private boolean _armed;
    private Block _block;
    private long _lastState = System.currentTimeMillis();
    private Player _placer;
    private boolean _triggered;

    public Landmine(Player player, Block block)
    {
        _placer = player;
        _block = block;
    }

    public Block getBlock()
    {
        return _block;
    }

    public long getLastState()
    {
        return _lastState;
    }

    public Location getLocation()
    {
        return getBlock().getLocation().add(0.5, 0.5, 0.5);
    }

    public Player getPlayer()
    {
        return _placer;
    }

    public boolean isArmed()
    {
        return _armed;
    }

    public boolean isTriggered()
    {
        return _triggered;
    }

    public void setArmed()
    {
        _lastState = System.currentTimeMillis();
        _armed = true;
    }

    public void setTriggered()
    {
        _lastState = System.currentTimeMillis();
        _triggered = true;
    }
}
