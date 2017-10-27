package me.libraryaddict.arcade.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LootEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private Block _block;
    private boolean _cancelled;
    private Player _looter;

    public LootEvent(Player looter, Block block)
    {
        _looter = looter;
        _block = block;
    }

    public Block getBlock()
    {
        return _block;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public Player getPlayer()
    {
        return _looter;
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
