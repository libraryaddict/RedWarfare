package me.libraryaddict.core.player.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.network.PlayerData;

public class PlayerLoadEvent extends Event
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private Player _player;
    private PlayerData _playerData;

    public PlayerLoadEvent(Player player, PlayerData data)
    {
        _player = player;
        _playerData = data;
    }

    public PlayerData getData()
    {
        return _playerData;
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
}
