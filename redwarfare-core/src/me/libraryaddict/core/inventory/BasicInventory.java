package me.libraryaddict.core.inventory;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.inventory.utils.IButton;

public abstract class BasicInventory implements InventoryHolder, Listener
{
    private HashMap<Integer, IButton> _buttons = new HashMap<Integer, IButton>();
    protected Inventory _inventory;
    protected HashMap<Integer, ItemStack> _items = new HashMap<Integer, ItemStack>();
    private Player _owner;
    private int _size;
    private String _title;

    public BasicInventory(Player player, String title)
    {
        this(player, title, 54);
    }

    public BasicInventory(Player player, String title, int size)
    {
        _owner = player;
        _size = size;
        _title = title;
    }

    public void addButton(int slot, ItemStack item, IButton button)
    {
        if (button != null)
        {
            _buttons.put(slot, button);
        }
        else
        {
            _buttons.remove(slot);
        }

        _items.put(slot, item);

        if (_inventory != null && _inventory.getSize() > slot)
        {
            _inventory.setItem(slot, item);
        }
    }

    public void addItem(int slot, ItemStack item)
    {
        _buttons.remove(slot);
        _items.put(slot, item);

        if (_inventory != null && _inventory.getSize() > slot)
        {
            _inventory.setItem(slot, item);
        }
    }

    public void clear()
    {
        _buttons.clear();
        _items.clear();

        if (_inventory != null)
        {
            _inventory.clear();
        }
    }

    public void closeInventory()
    {
        closeInventory(true);
    }

    public void closeInventory(boolean forceClose)
    {
        HandlerList.unregisterAll(this);

        if (forceClose && isInventory(getPlayer()))
        {
            getPlayer().closeInventory();
        }
    }

    @Override
    public Inventory getInventory()
    {
        return _inventory;
    }

    public ItemStack getItem(int slot)
    {
        return _items.get(slot);
    }

    public Player getPlayer()
    {
        return _owner;
    }

    public int getSize()
    {
        return _size;
    }

    public String getTitle()
    {
        return _title;
    }

    public boolean hasItem(int slot)
    {
        return _buttons.containsKey(slot) || _items.containsKey(slot);
    }

    public boolean isInUse()
    {
        Inventory inv = getPlayer().getOpenInventory().getTopInventory();

        return inv.getHolder() == this;
    }

    public boolean isInventory(HumanEntity entity)
    {
        return getPlayer() == entity;
    }

    public boolean isInventory(Inventory inv)
    {
        return inv != null && inv.getHolder() == this;
    }

    public boolean onClick(ClickType clickType, int slot)
    {
        if (!_buttons.containsKey(slot))
        {
            return true;
        }

        return _buttons.get(slot).onClick(clickType);
    }

    @EventHandler
    public final void onClick(InventoryClickEvent event)
    {
        if (event.getClickedInventory() == null || !isInventory(event.getView().getTopInventory()))
            return;

        int slot = event.getRawSlot();

        if (slot < 0 || slot > getSize())
        {
            if (event.isShiftClick())
            {
                event.setCancelled(true);
            }

            return;
        }

        if (!onClick(event.getClick(), slot))
        {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        if (!isInventory(event.getPlayer()))
            return;

        closeInventory(false);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if (!isInventory(event.getView().getTopInventory()))
            return;

        if (event.getRawSlots().size() == 1)
        {
            if (!onClick(ClickType.LEFT, event.getRawSlots().iterator().next()))
            {
                return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        if (!isInventory(event.getPlayer()))
            return;

        closeInventory(false);
    }

    public void openInventory()
    {
        if (getSize() == -1 || getTitle() == null)
            throw new IllegalArgumentException("Title or inventory size hasn't been set");

        Inventory openInv = getPlayer().getOpenInventory().getTopInventory();

        if (openInv != null && openInv != _inventory && openInv.getType() != InventoryType.CRAFTING)
        {
            getPlayer().closeInventory();
        }
        else if (openInv != null && openInv == _inventory)
        {
            return;
        }

        _inventory = Bukkit.createInventory(this, getSize(), getTitle());

        for (Entry<Integer, ItemStack> entry : _items.entrySet())
        {
            _inventory.setItem(entry.getKey(), entry.getValue());
        }

        getPlayer().openInventory(_inventory);

        InventoryManager.Manager.registerInventory(this);
    }

    public void setSize(int size)
    {
        _size = size;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

}
