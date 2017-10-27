package me.libraryaddict.arcade.events;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WinEvent extends Event
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private ArrayList<UUID> _losers;
    private ArrayList<UUID> _winners;

    public WinEvent(ArrayList<UUID> winners, ArrayList<UUID> losers)
    {
        _winners = winners;
        _losers = losers;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public ArrayList<UUID> getLosers()
    {
        return _losers;
    }

    public ArrayList<UUID> getWinners()
    {
        return _winners;
    }
}
