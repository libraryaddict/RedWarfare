package me.libraryaddict.arcade.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;

public class PlayerSpecInventory extends BasicInventory
{
    private ItemStack _offhand = new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (short) 3).setTitle(C.Aqua + "Offhand slot")
            .build();
    private ItemStack _unaccessible = new ItemBuilder(Material.COAL_BLOCK).setTitle(C.DRed + C.Bold + "Unaccessible slot")
            .build();
    private Player _viewed;

    public PlayerSpecInventory(Player player, Player viewed)
    {
        super(player, viewed.getName() + "'s Inventory", 5 * 9);

        _viewed = viewed;

        buildPage();
    }

    private void buildPage()
    {
        PlayerInventory inv = _viewed.getInventory();

        for (int i = 0; i < 36; i++)
        {
            ItemStack item = inv.getItem(i);

            if (item == null || item.getType() == Material.AIR)
                continue;

            int slot = i;

            if (slot < 9)
            {
                slot -= 9;
            }

            slot = (slot + 45) % 45;

            addItem(slot, item);
        }

        ItemStack offhand = inv.getItem(40);

        if (offhand != null && offhand.getType() != Material.AIR)
            addItem(8, offhand);
        else
            addItem(8, _offhand);

        int a = -1;

        for (EquipmentSlot slot : EquipmentSlot.values())
        {
            if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND)
                continue;

            a++;

            ItemStack item = UtilInv.getItem(_viewed, slot);

            if (item == null || item.getType() == Material.AIR)
            {
                item = new ItemBuilder(Material.THIN_GLASS, 1, (short) 3).setTitle(
                        C.Gray + "No " + slot.name().substring(0, 1) + slot.name().substring(1).toLowerCase() + " is being worn")
                        .build();
            }

            addItem(a, item);
        }

        for (int i = 4; i < 8; i++)
        {
            addItem(i, _unaccessible);
        }
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        if (_viewed == null || !_viewed.isOnline())
        {
            _viewed = null;
            return;
        }

        buildPage();
    }
}
