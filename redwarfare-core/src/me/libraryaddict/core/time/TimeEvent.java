package me.libraryaddict.core.time;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TimeEvent extends Event
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private TimeType _timeType;

    public TimeEvent(TimeType timeType)
    {
        _timeType = timeType;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public TimeType getType()
    {
        return _timeType;
    }

}
