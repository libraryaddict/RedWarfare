package me.libraryaddict.core.player.types;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.player.PlayerDataManager;

public class Currency
{
    public enum CurrencyType
    {
        CREDIT("Credit"),

        POINT("Point"),

        TOKEN("Token");

        private String _name;

        private CurrencyType(String name)
        {
            _name = name;
        }

        public String getName()
        {
            return _name;
        }
    }

    private static PlayerDataManager _dataManager;

    public static void add(Player player, CurrencyType item, String reason, long amount)
    {
        add(player.getUniqueId(), item, reason, amount);
    }

    public static void add(UUID uuid, CurrencyType item, String reason, long newPoints)
    {
        add(uuid, item, reason, newPoints, Bukkit.getPlayer(uuid) == null);
    }

    public static void add(UUID uuid, CurrencyType item, String reason, long newPoints, boolean notify)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                _dataManager.getMappings().addOwned(uuid, item, reason, newPoints, notify);
            }
        }.runTaskAsynchronously(_dataManager.getPlugin());
    }

    public static long get(Player player, CurrencyType item)
    {
        return get(player.getUniqueId(), item);
    }

    public static long get(UUID uuid, CurrencyType item)
    {
        return _dataManager.getMappings().getOwned(uuid, item);
    }

    public static void init(PlayerDataManager dataManager)
    {
        _dataManager = dataManager;
    }

    public static void set(Player player, CurrencyType item, long newAmount)
    {
        set(player.getUniqueId(), item, newAmount);
    }

    public static void set(UUID uuid, CurrencyType item, long newAmount)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                _dataManager.getMappings().setOwned(uuid, item, newAmount);
            }
        }.runTaskAsynchronously(_dataManager.getPlugin());
    }
}
