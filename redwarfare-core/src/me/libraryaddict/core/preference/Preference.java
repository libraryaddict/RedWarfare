package me.libraryaddict.core.preference;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.player.events.PreferenceSetEvent;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.network.Pref;

public class Preference
{
    private static PlayerDataManager _dataManager;

    private static PlayerData get(Player player)
    {
        return _dataManager.getData(player);
    }

    public static <T> T getPreference(Player player, Pref<T> pref)
    {
        return (T) get(player).getPrefs().getPref(pref, pref.getDefault());
    }

    public static boolean hasPreference(Player player, Pref pref)
    {
        return get(player).getPrefs().hasPref(pref);
    }

    public static void init(PlayerDataManager dataManager)
    {
        _dataManager = dataManager;
    }

    public static <T> void setPreference(Player player, Pref<T> pref, T newOption)
    {
        PlayerData playerData = get(player);

        if (Objects.equals(pref.getDefault(), newOption))
        {
            if (!playerData.getPrefs().hasPref(pref))
            {
                return;
            }

            playerData.getPrefs().removePref(pref);
        }
        else
        {
            playerData.getPrefs().setPref(pref, newOption);
        }

        playerData.save();

        Bukkit.getPluginManager().callEvent(new PreferenceSetEvent(player, pref, newOption));
    }
}
