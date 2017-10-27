package me.libraryaddict.core.damage;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.core.combat.CombatLog;

public class CustomDeathEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _advertiseDeath;
    private boolean _cancelled;
    private CombatLog _combatLog;
    private Player _player;

    public CustomDeathEvent(Player player, CombatLog combatLog)
    {
        _player = player;
        _combatLog = combatLog;
    }

    public CombatLog getCombatLog()
    {
        return _combatLog;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public Player getPlayer()
    {
        return _player;
    }

    public boolean isAdvertiseDeath()
    {
        return _advertiseDeath;
    }

    @Override
    public boolean isCancelled()
    {
        return _cancelled;
    }

    public void setAdvertiseDeath(boolean advertiseDeath)
    {
        _advertiseDeath = advertiseDeath;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        _cancelled = cancelled;
    }

}
