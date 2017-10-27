package me.libraryaddict.core.stats;

import java.util.UUID;

import org.bukkit.entity.Player;

public class Stats
{
    private static StatsManager _statsManager;

    public static void add(Player player, String statName)
    {
        add(player, statName, 1);
    }

    public static void add(Player player, String statName, long statAmount)
    {
        _statsManager.addStat(player, statName, statAmount);
    }

    public static void add(UUID player, String statName)
    {
        add(player, statName, 1);
    }

    public static void add(UUID uuid, String statName, long statAmount)
    {
        _statsManager.addStat(uuid, statName, statAmount);
    }

    public static void decrease(Player player, String statName)
    {
        decrease(player, statName, 1);
    }

    public static void decrease(Player player, String statName, long statAmount)
    {
        add(player, statName, -statAmount);
    }

    public static void decrease(UUID player, String statName)
    {
        add(player, statName, -1);
    }

    public static long get(Player player, String statName)
    {
        return _statsManager.getStat(player, statName);
    }

    public static void timeEnd(Player player, String statName)
    {
        _statsManager.endTimer(player, statName);
    }

    public static void timeStart(Player player, String statName)
    {
        _statsManager.startTimer(player, statName);
    }

    public Stats(StatsManager statsManager)
    {
        _statsManager = statsManager;
    }
}
