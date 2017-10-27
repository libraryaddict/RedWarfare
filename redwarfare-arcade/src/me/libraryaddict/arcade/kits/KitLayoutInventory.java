package me.libraryaddict.arcade.kits;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;

import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.utils.UtilInv;

public class KitLayoutInventory extends BasicInventory
{
    private Kit _kit;
    private ItemStack _offhand = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 3).setTitle(C.Aqua + "Offhand slot")
            .build();
    private ItemStack _unaccessible = new ItemBuilder(Material.COAL_BLOCK).setTitle(C.DRed + C.Bold + "Unaccessible slot")
            .build();

    public KitLayoutInventory(Player player, Kit kit)
    {
        super(player, "Kit Layout", 45);

        _kit = kit;

        HashMap<Integer, Integer> layout = new Gson().fromJson(Preference.getPreference(player, kit.getKitLayout()),
                kit.getKitLayout().getToken());
        HashMap<Integer, Integer> defaultLayout = new Gson().fromJson(kit.getKitLayout().getDefault(),
                kit.getKitLayout().getToken());

        if (layout.size() != defaultLayout.size())
        {
            layout = defaultLayout;
        }
        else
        {
            for (int key : defaultLayout.keySet())
            {
                if (!layout.containsKey(key))
                {
                    layout = defaultLayout;
                    break;
                }
            }
        }

        for (int i = 0; i < kit.getItems().length; i++)
        {
            ItemStack item = kit.getItems()[i];

            if (item == null || item.getType() == Material.AIR)
                continue;

            int slot = layout.get(i);

            if (slot != 40)
            {
                if (slot < 9)
                {
                    slot -= 9;
                }

                slot = (slot + 45) % 45;
            }
            else
            {
                slot = 8;
            }

            addItem(slot, item);
        }

        for (int i = 0; i < 8; i++)
        {
            addItem(i, _unaccessible);
        }

        if (!hasItem(8))
        {
            addItem(8, _offhand);
        }
    }
    public void closeInventory(boolean forceClose)
    {
        HashMap<Integer, Integer> layout = new HashMap<Integer, Integer>();

        for (int i = 0; i < _kit.getItems().length; i++)
        {
            ItemStack item = _kit.getItems()[i];

            if (item == null || item.getType() == Material.AIR)
                continue;

            int slot = getInventory().first(item);

            if (slot < 8)
            {
                layout = null;
                break;
            }

            if (slot == 8)
            {
                slot = 40;
            }
            else
            {
                if (slot > 35)
                {
                    slot += 9;
                }

                slot = (slot + 45) % 45;
            }

            layout.put(i, slot);
        }

        if (layout != null)
        {
            String oldPref = Preference.getPreference(getPlayer(), _kit.getKitLayout());
            String newPref = new Gson().toJson(layout);

            if (!oldPref.equals(newPref))
            {
                Preference.setPreference(getPlayer(), _kit.getKitLayout(), newPref);

                getPlayer().sendMessage(C.Blue + "Changed " + _kit.getName() + " item layout");
            }
        }
        else
        {
            getPlayer().sendMessage(C.Red + "Failed to change the layout");
        }

        getPlayer().setItemOnCursor(new ItemStack(Material.AIR));

        super.closeInventory(forceClose);
    }

    @Override
    public boolean onClick(ClickType clickType, int slot)
    {
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!isInventory(event.getView().getTopInventory()))
            return;

        Player player = (Player) event.getWhoClicked();

        if (event.getSlot() < 0 || event.isShiftClick() || event.getClick().isKeyboardClick())
        {
            player.sendMessage(C.Red + "Please don't click like that..");
            event.setCancelled(true);
        }

        if (!isInventory(event.getClickedInventory()))
        {
            player.sendMessage(C.Red + "That's not even in the kit layout");
            event.setCancelled(true);
        }

        if (UtilInv.isSimilar(event.getCurrentItem(), _unaccessible))
        {
            player.sendMessage(C.Red + "You cannot modify that slot");
            event.setCancelled(true);
        }

        if (UtilInv.isSimilar(event.getCurrentItem(), _offhand))
        {
            event.setCurrentItem(new ItemStack(Material.AIR));
            player.updateInventory();
        }
    }

    @EventHandler
    @Override
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if (!isInventory(event.getView().getTopInventory()))
            return;

        if (event.getNewItems().size() == 1)
        {
            InventoryClickEvent newEvent = new InventoryClickEvent(event.getView(), SlotType.CONTAINER,
                    event.getNewItems().keySet().iterator().next(), ClickType.LEFT, InventoryAction.DROP_ALL_SLOT, 0);

            onInventoryClick(newEvent);

            if (newEvent.isCancelled())
            {
                event.setCancelled(true);
            }
        }
        else
        {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(C.Red + "Please don't click like that...");
        }
    }
}
