package me.libraryaddict.core.preference;

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
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.network.Pref;

public class PreferenceInventory extends PageInventory
{
    private ArrayList<PreferenceItem> _preferences;
    private PlayerRank _rank;

    public PreferenceInventory(Player player, PlayerRank rank, ArrayList<PreferenceItem> preferences)
    {
        super(player, "Preferences", 54);

        _rank = rank;
        _preferences = preferences;

        buildPage();
    }

    private IButton buildButton(PreferenceItem pref)
    {
        return new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                if (!Recharge.canUse(getPlayer(), pref.getName() + " Click"))
                    return true;

                Recharge.use(getPlayer(), pref.getName() + " Click", 1000);

                if (pref.owns(getPlayer(), _rank))
                {
                    getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2, 2);

                    for (Pref p : pref.getImcompatible())
                    {
                        Preference.setPreference(getPlayer(), p, false);
                    }

                    Preference.setPreference(getPlayer(), pref.getPref(),
                            !(Boolean) Preference.getPreference(getPlayer(), pref.getPref()));

                    buildPage();
                }
                else
                {
                    if (pref.getType() == null)
                    {
                        getPlayer().sendMessage(C.Red + "You do not have the rank " + pref.getRank().getName() + "!");
                        return true;
                    }

                    if (Currency.get(getPlayer(), pref.getType()) < pref.getCost())
                    {
                        getPlayer().sendMessage(C.Red + "You can't afford to pay " + pref.getCost() + " "
                                + pref.getType().getName() + "s when you only have " + Currency.get(getPlayer(), pref.getType())
                                + " " + pref.getType().getName() + "s!");
                        return true;
                    }

                    Currency.add(getPlayer(), pref.getType(), "Purchased " + pref.getName(), -pref.getCost());

                    getPlayer().sendMessage(C.DGreen + "Purchased the preference " + pref.getName() + " for " + pref.getCost()
                            + " " + pref.getType().getName() + "s!");

                    pref.setPurchased(getPlayer());

                    getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2, 2);

                    buildPage();
                }

                return true;
            }
        };
    }

    public void buildPage()
    {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        for (int i = 0; i < _preferences.size(); i++)
        {
            PreferenceItem pref = _preferences.get(i);

            ItemBuilder builder = new ItemBuilder(pref.getIcon());

            if (pref.owns(getPlayer(), _rank))
            {
                builder.setTitle(C.Aqua + C.Bold + pref.getName());

                builder.addLore(C.Blue + C.Bold + "Toggled: "
                        + (Preference.getPreference(getPlayer(), pref.getPref()) ? C.DGreen + "Enabled" : C.DRed + "Disabled"));
            }
            else
            {
                builder.setTitle(C.Aqua + C.Bold + pref.getName() + C.White + " - " + C.DRed + C.Bold + "LOCKED");

                if (pref.getType() != null)
                {
                    builder.addLore(C.DBlue + pref.getCost() + " " + pref.getType().getName());
                }

                if (pref.getRank() != null)
                {
                    builder.addLore(C.DBlue + "Requires the rank " + pref.getRank().getName());
                }
            }

            items.add(Pair.of(builder.build(), buildButton(pref)));

            if (i % 5 == 0 && i > 0 && i + 1 < _preferences.size())
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
