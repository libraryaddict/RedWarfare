package me.libraryaddict.arcade.kits;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.preference.Preference;

public class KitInventory extends BasicInventory
{
    private ArcadeManager _arcadeManager;

    public KitInventory(Player player, ArcadeManager arcadeManager)
    {
        super(player, C.DBlue + "Pick a kit");

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

            if (!kit.ownsKit(getPlayer()))
            {
                if (kit.getPrice() > 0 && kit.getKitAvailibility() == KitAvailibility.Purchase)
                {
                    builder.addLore(C.DRed + C.Bold + "LOCKED " + C.Aqua + "Purchase for " + kit.getPrice() + " credits");
                    builder.addLore(C.Aqua + "Click to purchase");
                }
                else
                {
                    builder.addLore(C.DRed + C.Bold + "LOCKED");
                }
            }

            for (String string : kit.getDescription())
            {
                builder.addLore(C.Yellow + string);
            }

            items.add(Pair.of(builder.build(), new IButton()
            {

                @Override
                public boolean onClick(ClickType clickType)
                {
                    if (_arcadeManager.getGame().getForceKit() != null)
                    {
                        getPlayer().sendMessage(C.Red + "Forced kits!");
                        return true;
                    }

                    if (kit.ownsKit(getPlayer()))
                    {
                        _arcadeManager.getGame().chooseKit(getPlayer(), kit, false);
                    }
                    else if (kit.getKitAvailibility() == KitAvailibility.Purchase && kit.getPrice() > 0)
                    {
                        new KitPurchaseInventory(getPlayer(), _arcadeManager, kit).openInventory();
                    }

                    return true;
                }
            }));
        }

        while (items.size() % 9 != 0)
            items.add(null);

        while (items.size() % 9 != 8)
            items.add(null);

        items.add(Pair.of(new ItemBuilder(Material.WRITTEN_BOOK).setTitle(C.Red + "Save Kit").setHideInfo().build(), new IButton()
        {

            @Override
            public boolean onClick(ClickType clickType)
            {
                Kit kit = _arcadeManager.getGame().getKit(getPlayer());

                if (!kit.ownsKit(getPlayer()))
                {
                    getPlayer().sendMessage(C.Red + "You do not own that kit!");
                    return true;
                }

                Preference.setPreference(getPlayer(), _arcadeManager.getGame().getSaveKit(), kit.getName());

                getPlayer().sendMessage(C.Blue + "Saved default kit to " + kit.getName());

                return true;
            }
        }));

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
