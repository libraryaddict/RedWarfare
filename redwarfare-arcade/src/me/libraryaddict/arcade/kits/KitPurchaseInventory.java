package me.libraryaddict.arcade.kits;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.player.types.Owned;

public class KitPurchaseInventory extends BasicInventory
{
    public KitPurchaseInventory(Player player, ArcadeManager manager, Kit kit)
    {
        super(player, "Purchase " + kit.getName(), 27);

        addItem(4, new ItemBuilder(kit.getIcon()).setTitle(C.Gold + kit.getName())
                .addLore(C.DAqua + "Price: " + C.Aqua + kit.getPrice() + " credits").build());

        addButton(20, new ItemBuilder(Material.WOOL, 1, (short) 5).setTitle(C.DGreen + C.Bold + "CONFIRM").build(), new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                if (Currency.get(getPlayer(), CurrencyType.CREDIT) < kit.getPrice())
                {
                    getPlayer().sendMessage(C.DRed + "Required: " + C.Red + kit.getPrice() + " credits." + C.DRed + " You have: "
                            + C.Red + Currency.get(getPlayer(), CurrencyType.CREDIT) + " credits");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, 0);
                    return true;
                }

                Currency.add(getPlayer(), CurrencyType.CREDIT, "Purchased " + manager.getGame().getName() + " kit " + kit.getName(),
                        -kit.getPrice());

                Owned.setOwned(getPlayer(), manager.getGame().getName() + ".Kit." + kit.getName());

                getPlayer().sendMessage(C.Blue + "Purchased kit " + kit.getName() + " for " + kit.getPrice() + " credits!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);

                new KitInventory(getPlayer(), manager).openInventory();
                return true;
            }
        });

        addButton(24, new ItemBuilder(Material.WOOL, 1, (short) 14).setTitle(C.DRed + C.Bold + "CANCEL").build(), new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                new KitInventory(getPlayer(), manager).openInventory();
                return true;
            }
        });
    }

}
