package me.libraryaddict.core.scoreboard;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Predicate;

import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.utils.UtilPlayer;

public class ScoreboardManager extends MiniPlugin
{
    private boolean _established;
    private HashMap<String, FakeScoreboard> _scoreboards = new HashMap<String, FakeScoreboard>();

    public ScoreboardManager(JavaPlugin plugin)
    {
        super(plugin, "Scoreboard Manager");

        new BukkitRunnable()
        {
            public void run()
            {
                createMain();
            }
        }.runTask(getPlugin());
    }

    private void createMain()
    {
        if (_established)
            return;

        _established = true;

        createScoreboard("Main", (Player player) -> true);
    }

    public FakeScoreboard createScoreboard(String name, Predicate<Player> predicate)
    {
        if (getScoreboard(name) != null)
        {
            throw new IllegalArgumentException("FakeScoreboard " + name + " is already registered");
        }

        FakeScoreboard scoreboard = new FakeScoreboard(name, predicate);

        _scoreboards.put(name, scoreboard);

        System.out.print("Created " + (predicate == null ? "abstract " : "") + "scoreboard '" + name + "'");

        for (Player player : UtilPlayer.getPlayers())
        {
            getMainScoreboard().setScoreboard(player);
        }

        return scoreboard;
    }

    public void discardScoreboard(FakeScoreboard board)
    {
        _scoreboards.remove(board.getName());

        for (FakeScoreboard b : _scoreboards.values())
        {
            b.getChildren().remove(board);
        }

        for (FakeScoreboard b : board.getChildren())
        {
            discardScoreboard(b);
        }

        for (Player player : board.getApplicable())
        {
            getMainScoreboard().setScoreboard(player);
        }
    }

    public void discardScoreboards()
    {
        System.out.print("Discarded all scoreboards");

        _scoreboards.clear();
        _established = false;

        createMain();
    }

    public FakeScoreboard getMainScoreboard()
    {
        return getScoreboard("Main");
    }

    public FakeScoreboard getScoreboard(String name)
    {
        if (!_scoreboards.containsKey(name))
            return null;

        return _scoreboards.get(name);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        getMainScoreboard().setScoreboard(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        _scoreboards.remove(player.getName());
    }

    public boolean spawnBoundingBox(Player viewer, Player target)
    {
        FakeScoreboard board = getMainScoreboard().getSubScoreboard(viewer);

        for (FakeTeam team : board.getFakeTeams())
        {
            if (!team.getPlayers().contains(viewer.getName()))
                continue;
            
            return !team.canSeeInvisiblePlayers() || !team.getPlayers().contains(target.getName());
        }
        
        return true;
    }
}
