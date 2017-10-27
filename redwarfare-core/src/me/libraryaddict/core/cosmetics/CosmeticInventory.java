package me.libraryaddict.core.cosmetics;

import java.util.ArrayList;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.preference.Preference;

public class CosmeticInventory extends PageInventory
{
    private ArrayList<Cosmetic> _cosmetics;

    public CosmeticInventory(ArrayList<Cosmetic> cosmetics, Player player)
    {
        super(player, "Cosmetics", 54);

        _cosmetics = cosmetics;

        buildPage();
    }

    public void buildPage()
    {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        for (int i = 0; i < _cosmetics.size(); i++)
        {
            Cosmetic cosm = _cosmetics.get(i);

            ItemBuilder builder = new ItemBuilder(cosm.getIcon());

            if (cosm.hasCosmetic(getPlayer()))
            {
                builder.setTitle(C.Aqua + C.Bold + cosm.getName() + C.White + " - " + C.DGreen + C.Bold + "UNLOCKED");
            }
            else
            {
                builder.setTitle(C.Aqua + C.Bold + cosm.getName() + C.White + " - " + C.DRed + C.Bold + "LOCKED");

                if (cosm.getCurrencyType() != null)
                {
                    builder.addLore(C.DBlue + cosm.getCost() + " " + cosm.getCurrencyType().getName());
                }

                if (cosm.getRank() != null)
                {
                    builder.addLore(C.DBlue + "Requires the rank " + cosm.getRank().getName());
                }
            }

            builder.addLore(cosm.getDescription());

            items.add(Pair.of(builder.build(), new IButton()
            {
                @Override
                public boolean onClick(ClickType clickType)
                {
                    if (cosm.hasCosmetic(getPlayer()))
                    {
                        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2, 2);

                        for (Cosmetic cosmetic : _cosmetics)
                        {
                            if (cosmetic == cosm)
                                continue;

                            Preference.setPreference(getPlayer(), cosmetic.getToggled(), false);
                        }

                        Preference.setPreference(getPlayer(), cosm.getToggled(),
                                !Preference.getPreference(getPlayer(), cosm.getToggled()));

                        getPlayer().sendMessage(
                                C.Blue + "Toggled: " + C.Aqua + Preference.getPreference(getPlayer(), cosm.getToggled()));

                        buildPage();
                    }
                    else
                    {
                        if (Currency.get(getPlayer(), cosm.getCurrencyType()) < cosm.getCost())
                        {
                            getPlayer().sendMessage(
                                    C.Red + "You can't afford to pay " + cosm.getCost() + " " + cosm.getCurrencyType().getName()
                                            + "s when you only have " + Currency.get(getPlayer(), cosm.getCurrencyType()) + " "
                                            + cosm.getCurrencyType().getName() + "s!");
                            return true;
                        }

                        Currency.add(getPlayer(), cosm.getCurrencyType(), "Purchased " + cosm.getName(), -cosm.getCost());

                        getPlayer().sendMessage(C.DGreen + "Purchased the cosmetic " + cosm.getName() + " for " + cosm.getCost()
                                + " " + cosm.getCurrencyType().getName() + "s!");

                        getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2, 2);

                        buildPage();
                    }

                    return true;
                }
            }));

            if ((i + 1) % 9 == 0)
            {
                for (int a = 0; a < 9; a++)
                {
                    items.add(null);
                }
            }
            else
            {
                items.add(null);
            }
        }

        setPages(items);
    }

}
