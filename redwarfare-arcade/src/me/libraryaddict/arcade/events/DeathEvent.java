package me.libraryaddict.arcade.events;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.core.combat.CombatLog;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;

public class DeathEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _cancelled;
    private CombatLog _combatLog;
    private String _killedPrefix = "";
    private String _killerPrefix = "";
    private Player _player;

    public DeathEvent(Player player, CombatLog combatLog)
    {
        _player = player;
        _combatLog = combatLog;
    }

    public AttackType getAttackType()
    {
        return getDamageEvent().getAttackType();
    }

    public CombatLog getCombatLog()
    {
        return _combatLog;
    }

    public CustomDamageEvent getDamageEvent()
    {
        return getCombatLog().getLastEvent().getEvent();
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public String getKilledPrefix()
    {
        return _killedPrefix;
    }

    public String getKillerPrefix()
    {
        return _killerPrefix;
    }

    public Entity getLastAttacker()
    {
        return _combatLog.getKiller();
    }

    public Player getPlayer()
    {
        return _player;
    }

    @Override
    public boolean isCancelled()
    {
        return _cancelled;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        _cancelled = cancel;
    }

    public void setKilledPrefix(String prefix)
    {
        _killedPrefix = prefix;
    }

    public void setKillerPrefix(String prefix)
    {
        _killerPrefix = prefix;
    }

}
