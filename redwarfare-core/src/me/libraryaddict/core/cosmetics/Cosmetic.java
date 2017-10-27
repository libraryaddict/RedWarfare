package me.libraryaddict.core.cosmetics;

import java.util.ArrayList;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Owned;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.network.Pref;

public abstract class Cosmetic implements Listener
{
    private CentralManager _centralManager;
    private int _currencyRequired;
    private Currency.CurrencyType _currencyType;
    private Rank _lockedTo = Rank.ALL;
    private String _name;
    private Pref<Boolean> _toggled;

    public Cosmetic(String name)
    {
        _name = name;

        _toggled = new Pref<Boolean>("Cosmetic." + getName(), false);
    }

    public int getCost()
    {
        return _currencyRequired;
    }

    public Currency.CurrencyType getCurrencyType()
    {
        return _currencyType;
    }

    public abstract String[] getDescription();

    public abstract ItemStack getIcon();

    public CentralManager getManager()
    {
        return _centralManager;
    }

    public String getName()
    {
        return _name;
    }

    public ArrayList<Player> getPlayers()
    {
        if (!getManager().getCosmetics().isEnabled())
            return new ArrayList<Player>();

        return (ArrayList<Player>) UtilPlayer.getPlayers().stream().filter((player) -> Preference.getPreference(player, _toggled))
                .collect(Collectors.toList());
    }

    public Rank getRank()
    {
        return _lockedTo;
    }

    public Pref<Boolean> getToggled()
    {
        return _toggled;
    }

    public boolean hasCosmetic(Player player)
    {
        if (getRank() != null)
            return getManager().getRank().getRank(player).hasRank(getRank());

        return Owned.has(player, "Cosmetic." + getName());
    }

    public boolean isEnabled(Player player)
    {
        return getManager().getCosmetics().isEnabled() && Preference.getPreference(player, _toggled);
    }

    public void register(CentralManager centralManager)
    {
        _centralManager = centralManager;
    }

    public void setRequired(Rank rank)
    {
        _lockedTo = rank;
    }

    public void setRequiredCredits(int credits)
    {
        _currencyType = Currency.CurrencyType.CREDIT;
        _currencyRequired = credits;
    }

    public void setRequiredPoints(int credits)
    {
        _currencyType = Currency.CurrencyType.POINT;
        _currencyRequired = credits;
    }

    public void setRequiredTokens(int credits)
    {
        _currencyType = Currency.CurrencyType.TOKEN;
        _currencyRequired = credits;
    }

}
