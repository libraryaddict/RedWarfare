package me.libraryaddict.arcade.managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.LootEvent;
import me.libraryaddict.arcade.events.WinEvent;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.searchanddestroy.KillstreakEvent;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.mysql.operations.MysqlLoadPlayerStats;
import me.libraryaddict.network.PlayerStats;

public class GameStatsManager extends MiniPlugin
{
    private ArcadeManager _manager;

    public GameStatsManager(JavaPlugin plugin, ArcadeManager arcadeManager)
    {
        super(plugin, "Game Stats Manager");

        _manager = arcadeManager;
    }

    public Game getGame()
    {
        return _manager.getGame();
    }

    public ArcadeManager getManager()
    {
        return _manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(DeathEvent event)
    {
        Player player = event.getPlayer();

        Stats.add(player, "Game." + getGame().getName() + "." + getGame().getKit(player).getName() + ".Deaths");
        Stats.add(player, "Game." + getGame().getName() + ".Deaths");

        if (event.getAttackType() != AttackType.QUIT && event.getAttackType() != AttackType.SUICIDE)
            return;

        Stats.add(player, "Game." + getGame().getName() + ".Suicides");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        Stats.timeStart(event.getPlayer(), "Game." + getGame().getName() + ".Time");
    }

    @EventHandler
    public void onKillstreak(KillstreakEvent event)
    {
        Player player = event.getPlayer();

        Stats.add(player, "Game." + getGame().getName() + "." + getGame().getKit(player).getName() + "."
                + (event.isAssist() ? "Assists" : "Kills"));
        Stats.add(player, "Game." + getGame().getName() + "." + (event.isAssist() ? "Assists" : "Kills"));

        UUID uuid = player.getUniqueId();
        String kitKS = "Game." + getGame().getName() + "." + getGame().getKit(player).getName() + ".Killstreak";
        String gameKS = "Game." + getGame().getName() + ".Killstreak";
        int killstreak = event.getKillstreak();

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlLoadPlayerStats loadStats = new MysqlLoadPlayerStats(uuid);

                if (!loadStats.isSuccess())
                {
                    return;
                }

                PlayerStats stats = loadStats.getStats();

                new BukkitRunnable()
                {
                    public void run()
                    {
                        // We only need to add 1 because this event fires every killstreak increase.
                        if (killstreak > stats.getStat(kitKS))
                        {
                            Stats.add(uuid, kitKS);
                        }

                        // We only need to add 1 because this event fires every killstreak increase.
                        if (killstreak > stats.getStat(gameKS))
                        {
                            Stats.add(uuid, gameKS);
                        }
                    }
                }.runTask(getPlugin());
            }
        }.runTaskAsynchronously(getPlugin());
    }

    @EventHandler
    public void onLoot(LootEvent event)
    {
        Stats.add(event.getPlayer(), "Game." + getGame().getName() + ".Looted");
    }

    @EventHandler
    public void onWin(WinEvent event)
    {
        Game game = getGame();
        HashMap<UUID, Kit> kits = game.getPlayed();

        for (UUID winner : event.getWinners())
        {
            Stats.add(winner, "Game." + game.getName() + "." + kits.get(winner).getName() + ".Wins");
            Stats.add(winner, "Game." + game.getName() + ".Wins");
        }

        for (UUID loser : event.getLosers())
        {
            Stats.add(loser, "Game." + game.getName() + ".Losses");
            Stats.add(loser, "Game." + game.getName() + "." + kits.get(loser).getName() + ".Losses");
        }
    }
}
