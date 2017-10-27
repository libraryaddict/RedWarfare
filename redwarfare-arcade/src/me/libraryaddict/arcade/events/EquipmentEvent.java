package me.libraryaddict.arcade.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EquipmentEvent extends Event implements Cancellable
{
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _cancelled;
    private ItemStack _itemstack;
    private boolean _modified;
    private EquipmentSlot _slot;
    private Player _viewer;
    private Player _wearer;

    public EquipmentEvent(Player wearer, Player viewer, EquipmentSlot slot, ItemStack itemStack)
    {
        _wearer = wearer;
        _slot = slot;
        _viewer = viewer;
        _itemstack = itemStack;
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public ItemStack getItem()
    {
        return _itemstack;
    }

    public EquipmentSlot getSlot()
    {
        return _slot;
    }

    public Player getViewer()
    {
        return _viewer;
    }

    public Player getWearer()
    {
        return _wearer;
    }

    @Override
    public boolean isCancelled()
    {
        return _cancelled;
    }

    public boolean isModified()
    {
        return _modified;
    }

    @Override
    public void setCancelled(boolean cancel)
    {
        _cancelled = cancel;
    }

    public void setHat(ItemStack newHat)
    {
        _modified = true;
        _itemstack = newHat;
    }
}
