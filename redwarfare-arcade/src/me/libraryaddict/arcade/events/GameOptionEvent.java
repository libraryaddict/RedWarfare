package me.libraryaddict.arcade.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.arcade.game.GameOption;

public class GameOptionEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _cancelled;

    private GameOption _option;
    private Object _value;

    public <Y> GameOptionEvent(GameOption<Y> gameOption, Y value)
    {
        _option = gameOption;
        _value = value;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public GameOption getOption()
    {
        return _option;
    }

    public <Y> Y getOption(GameOption<Y> hATS)
    {
        return (Y) _value;
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
