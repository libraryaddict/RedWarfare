package me.libraryaddict.arcade.kits;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitLayoutSelectInventory extends BasicInventory
{
    private ArcadeManager _arcadeManager;

    public KitLayoutSelectInventory(Player player, ArcadeManager arcadeManager)
    {
        super(player, C.DBlue + "Modify a kit");

        _arcadeManager = arcadeManager;

        buildSelection();
    }

    private void buildSelection()
    {
        Kit[] kits = _arcadeManager.getGame().getKits();
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        for (Kit kit : kits)
        {
            ItemBuilder builder = new ItemBuilder(kit.getIcon());
            builder.setTitle(C.Gold + kit.getName());

            for (String string : kit.getDescription())
            {
                builder.addLore(C.Yellow + string);
            }

            items.add(Pair.of(builder.build(), new IButton()
            {

                @Override
                public boolean onClick(ClickType clickType)
                {
                    getPlayer().sendMessage(C.Red + "Close the inventory to save your layout");

                    new KitLayoutInventory(getPlayer(), kit).openInventory();
                    return true;
                }
            }));
        }

        setSize((int) Math.ceil(items.size() / 9D) * 9);

        int i = 0;

        for (Pair<ItemStack, IButton> pair : items)
        {
            if (pair != null)
            {
                addButton(i, pair.getKey(), pair.getValue());
            }

            i++;
        }
    }

}
