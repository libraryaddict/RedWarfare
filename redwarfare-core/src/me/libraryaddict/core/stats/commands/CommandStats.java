package me.libraryaddict.core.stats.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.stats.StatsManager;
import me.libraryaddict.core.utils.LineFormat;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilText;
import me.libraryaddict.mysql.operations.MysqlLoadPlayerStats;
import me.libraryaddict.network.PlayerStats;

public class CommandStats extends SimpleCommand {
    private StatsManager _statsManager;

    public CommandStats(StatsManager statsManager) {
        super(new String[]{"stats"}, Rank.ALL);

        _statsManager = statsManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        UUID uuid = player.getUniqueId();

        PlayerStats newStats = _statsManager.getStats(player).clone();

        new BukkitRunnable() {
            public void run() {
                MysqlLoadPlayerStats loadStats = new MysqlLoadPlayerStats(uuid);

                if (!loadStats.isSuccess()) {
                    UtilPlayer.sendMessage(player, UtilError.format("Error connecting to database"));
                    return;
                }

                UtilPlayer.sendMessage(player, C.Blue + "Now sending stats...");

                PlayerStats oldStats = loadStats.getStats();

                HashMap<String, Long> stats = oldStats.combine(newStats);

                for (Entry<String, Long> stat : stats.entrySet()) {
                    String toSend = C.Blue + "Stat: " + C.Aqua + stat.getKey() + C.Blue + " Value: " + C.Aqua;

                    if (stat.getKey().endsWith(".Time")) {
                        toSend += UtilNumber.getTime(stat.getValue(), TimeUnit.SECONDS);
                    } else {
                        toSend += stat.getValue();
                    }

                    UtilPlayer.sendMessage(player, toSend);
                }

                UtilPlayer.sendMessage(player, C.Blue + "======================");
            }
        }.runTask(_statsManager.getPlugin());
    }
}
