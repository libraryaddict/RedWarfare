package me.libraryaddict.core.preference;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.player.types.Owned;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.network.Pref;

public class PreferenceItem
{
    private int _cost;
    private CurrencyType _currencyType;
    private Pref[] _imcompatible;
    private ItemStack _item;
    private String _name;
    private Pref<Boolean> _pref;
    private Rank _requires;

    public PreferenceItem(String name, Pref<Boolean> pref, ItemStack item, CurrencyType currencyType, int cost, Pref... prefs)
    {
        this(name, pref, item, prefs);

        _currencyType = currencyType;
        _cost = cost;
    }

    public PreferenceItem(String name, Pref<Boolean> pref, ItemStack item, Pref... prefs)
    {
        _imcompatible = prefs;
        _name = name;
        _pref = pref;
        _item = new ItemBuilder(item).build();
    }

    public PreferenceItem(String name, Pref<Boolean> pref, ItemStack item, Rank rank, Pref... prefs)
    {
        this(name, pref, item, prefs);

        _requires = rank;
    }

    public int getCost()
    {
        return _cost;
    }

    public ItemStack getIcon()
    {
        return _item;
    }

    public Pref[] getImcompatible()
    {
        return _imcompatible;
    }

    public String getName()
    {
        return _name;
    }

    public Pref<Boolean> getPref()
    {
        return _pref;
    }

    public Rank getRank()
    {
        return _requires;
    }

    public CurrencyType getType()
    {
        return _currencyType;
    }

    public boolean hasPurchased(Player player)
    {
        return Owned.has(player, "Preference." + getName());
    }

    public boolean owns(Player player, PlayerRank playerRank)
    {
        if (getType() == null && getRank() == null)
            return true;

        if (getRank() != null && playerRank.hasRank(getRank()))
            return true;

        return hasPurchased(player);
    }

    public void setPurchased(Player player)
    {
        Owned.setOwned(player, "Preference." + getName());
    }
}
