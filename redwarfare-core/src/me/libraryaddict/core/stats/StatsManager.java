package me.libraryaddict.core.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.player.events.PlayerLoadEvent;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.stats.commands.CommandStats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.mysql.operations.MysqlSavePlayerStats;
import me.libraryaddict.network.PlayerStats;

public class StatsManager extends MiniPlugin
{
    private ArrayList<PlayerStats> _saving = new ArrayList<PlayerStats>();
    private HashMap<UUID, PlayerStats> _stats = new HashMap<UUID, PlayerStats>();
    private HashMap<UUID, HashMap<String, Long>> _timers = new HashMap<UUID, HashMap<String, Long>>();

    public StatsManager(JavaPlugin plugin, CommandManager command)
    {
        super(plugin, "Stats Manager");

        new Stats(this);

        command.registerCommand(new CommandStats(this));
    }

    public void addStat(Player player, String statName, long statModifier)
    {
        addStat(player.getUniqueId(), statName, statModifier);
    }

    public void addStat(UUID uuid, String statName, long statModifier)
    {
        // System.out.println("Adding stat '" + statName + "' to " + uuid + " and adding: " + statModifier);
        if (_stats.containsKey(uuid))
        {
            _stats.get(uuid).setStat(statName, getStat(uuid, statName) + statModifier);
        }
        else
        {
            PlayerStats tempStats = new PlayerStats(uuid);

            tempStats.setStat(statName, statModifier);

            _saving.add(tempStats);
        }
    }

    public void endTimer(Player player, String statName)
    {
        endTimer(player.getUniqueId(), statName);
    }

    public void endTimer(UUID uuid, String statName)
    {
        if (!_timers.containsKey(uuid) || !_timers.get(uuid).containsKey(statName))
        {
            UtilError.log("Tried to end timer for stat '" + statName + "' but it wasn't recording");
            return;
        }

        long elasped = (System.currentTimeMillis() - _timers.get(uuid).remove(statName)) / 1000;

        addStat(uuid, statName, elasped);
    }

    public long getStat(Player player, String statName)
    {
        return _stats.get(player.getUniqueId()).getStat(statName);
    }

    public long getStat(UUID uuid, String statName)
    {
        return _stats.get(uuid).getStat(statName);
    }

    public PlayerStats getStats(Player player)
    {
        HashMap<String, Long> map = _timers.remove(player.getUniqueId());

        if (map != null)
        {
            for (Entry<String, Long> entry : map.entrySet())
            {
                addStat(player, entry.getKey(), (System.currentTimeMillis() - entry.getValue()) / 1000);

                startTimer(player, entry.getKey());
            }
        }

        return _stats.get(player.getUniqueId());
    }

    @EventHandler
    public void onJoin(PlayerLoadEvent event)
    {
        Player player = event.getPlayer();

        _stats.put(player.getUniqueId(), new PlayerStats(player.getUniqueId()));

        Stats.timeStart(player, "Global.Time");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        HashMap<String, Long> map = _timers.remove(player.getUniqueId());

        if (map != null)
        {
            for (Entry<String, Long> entry : map.entrySet())
            {
                addStat(player, entry.getKey(), (System.currentTimeMillis() - entry.getValue()) / 1000);
            }
        }

        PlayerStats stats = _stats.remove(player.getUniqueId());

        _saving.add(stats.clone());
    }

    @EventHandler
    public void onRegularSave(TimeEvent event)
    {
        if (event.getType() != TimeType.TEN_MIN)
            return;

        for (UUID uuid : _stats.keySet())
        {
            HashMap<String, Long> map = _timers.remove(uuid);

            if (map != null)
            {
                for (Entry<String, Long> entry : map.entrySet())
                {
                    addStat(uuid, entry.getKey(), (System.currentTimeMillis() - entry.getValue()) / 1000);

                    startTimer(uuid, entry.getKey());
                }
            }

            PlayerStats stats = _stats.get(uuid);

            if (stats.isDirty())
            {
                _saving.add(stats.clone());
                stats.empty();
            }
        }
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        if (_saving.isEmpty())
            return;

        PlayerStats[] stats = _saving.toArray(new PlayerStats[0]);

        _saving.clear();

        if (stats.length == 0)
            return;

        new BukkitRunnable()
        {
            public void run()
            {
                new MysqlSavePlayerStats(stats);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public void startTimer(Player player, String statName)
    {
        startTimer(player.getUniqueId(), statName);
    }

    public void startTimer(UUID uuid, String statName)
    {
        if (!_timers.containsKey(uuid))
        {
            _timers.put(uuid, new HashMap<String, Long>());
        }

        if (_timers.containsKey(statName))
        {
            UtilError.log("Tried to start timer for stat '" + statName + "' but it was already running!");
        }

        _timers.get(uuid).put(statName, System.currentTimeMillis());
    }

}
