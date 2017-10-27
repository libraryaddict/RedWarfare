package me.libraryaddict.core.player.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.network.Pref;

public class PreferenceSetEvent<T> extends Event
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private Player _player;
    private Pref<T> _preference;
    private T _value;

    public PreferenceSetEvent(Player player, Pref<T> pref, T newValue)
    {
        _player = player;
        _preference = pref;
        _value = newValue;
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

    public Pref<T> getPreference()
    {
        return _preference;
    }

    public T getValue()
    {
        return _value;
    }

}