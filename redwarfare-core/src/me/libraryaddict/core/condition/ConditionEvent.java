package me.libraryaddict.core.condition;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.core.condition.types.Condition;

public class ConditionEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _cancelled;
    private Condition _condition;

    private Entity _victim;

    public ConditionEvent(Entity entity, Condition condition)
    {
        _victim = entity;
        _condition = condition;
    }

    public Condition getCondition()
    {
        return _condition;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public Entity getVictim()
    {
        return _victim;
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

}
