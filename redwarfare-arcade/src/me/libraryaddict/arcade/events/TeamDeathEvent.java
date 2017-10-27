package me.libraryaddict.arcade.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.arcade.game.GameTeam;

public class TeamDeathEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _cancelled;

    private GameTeam _team;

    public TeamDeathEvent(GameTeam team)
    {
        _team = team;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public GameTeam getTeam()
    {
        return _team;
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
