package me.libraryaddict.arcade.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.arcade.managers.GameState;

public class GameStateEvent extends Event
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private GameState _gameState;

    public GameStateEvent(GameState newGameState)
    {
        _gameState = newGameState;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public GameState getState()
    {
        return _gameState;
    }
}
