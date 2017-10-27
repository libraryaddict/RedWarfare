package me.libraryaddict.arcade.managers;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import me.libraryaddict.arcade.Arcade;
import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.events.WinEvent;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.core.C;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilFirework;
import me.libraryaddict.core.utils.UtilMath;

public class WinManager extends MiniPlugin
{
    private ArcadeManager _arcadeManager;
    private Color _lastColor;
    private ArrayList<UUID> _lastWinners = new ArrayList<UUID>();

    public WinManager(Arcade arcade, ArcadeManager arcadeManager)
    {
        super(arcade, "Win Manager");

        _arcadeManager = arcadeManager;
    }

    public Color getColor()
    {
        return _lastColor;
    }

    public Game getGame()
    {
        return getManager().getGame();
    }

    public ArrayList<UUID> getLastWinners()
    {
        return _lastWinners;
    }

    public ArcadeManager getManager()
    {
        return _arcadeManager;
    }

    @EventHandler
    public void onFast(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
        {
            return;
        }

        Game game = getManager().getGame();

        if (game.getState() != GameState.End)
            return;

        game.spawnFireworks();
    }

    @EventHandler
    public void onGameChange(GameStateEvent event)
    {
        if (event.getState() != GameState.PreMap)
            return;

        _lastWinners.clear();
        _lastColor = null;
    }

    @EventHandler
    public void onHalfSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
        {
            return;
        }

        Game game = getManager().getGame();

        if (game.getState() != GameState.End)
            return;

        UUID uuid = UtilMath.r(getLastWinners());

        if (uuid == null)
            return;

        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        Location loc = player.getLocation().add(UtilMath.rr(-15, 15), 0, UtilMath.rr(-15, 15));

        Firework firework = UtilFirework.spawnRandomFirework(loc, getColor());

        UtilEnt.velocity(firework, new Vector(UtilMath.rr(-0.05, 0.05), -UtilMath.rr(0.1), UtilMath.rr(-0.05, 0.05)), false);
    }

    @EventHandler
    public void onKill(DeathEvent event)
    {
        if (getGame().getCreditsKill() <= 0)
            return;

        if (event.getCombatLog().getKiller() instanceof Player)
        {
            Player player = (Player) event.getCombatLog().getKiller();

            player.sendMessage(
                    C.Gold + "Given " + getGame().getCreditsLose() + " credit" + (getGame().getCreditsLose() != 1 ? "s" : ""));

            Currency.add(player, Currency.CurrencyType.CREDIT, getGame().getName() + " Kill", getGame().getCreditsLose());
        }
    }

    @EventHandler
    public void onWin(WinEvent event)
    {
        if (getGame().getCreditsWin() > 0)
        {
            for (UUID winner : event.getWinners())
            {
                Player player = Bukkit.getPlayer(winner);

                if (player != null)
                {
                    player.sendMessage(C.Gold + "Given " + getGame().getCreditsWin() + " credit"
                            + (getGame().getCreditsWin() == 1 ? "" : "s"));
                }

                Currency.add(winner, Currency.CurrencyType.CREDIT, getGame().getName() + " Win", getGame().getCreditsWin());
            }
        }

        if (getGame().getCreditsLose() > 0)
        {
            for (UUID loser : event.getLosers())
            {
                Player player = Bukkit.getPlayer(loser);

                if (player != null)
                {
                    player.sendMessage(C.Gold + "Given " + getGame().getCreditsLose() + " credit"
                            + (getGame().getCreditsLose() != 1 ? "s" : ""));
                }

                Currency.add(loser, Currency.CurrencyType.CREDIT, getGame().getName() + " Lose", getGame().getCreditsLose());
            }
        }
    }

    public void setWin(ArrayList<Player> players)
    {
        _lastWinners.clear();

        for (Player player : players)
            _lastWinners.add(player.getUniqueId());

        _lastColor = getManager().getGame().getTeams().get(0).getColor();

        ArrayList<UUID> losers = new ArrayList<UUID>(getManager().getGame().getPlayed().keySet());
        losers.removeAll(_lastWinners);

        for (GameTeam teams : getGame().getTeams())
        {
            losers.removeAll(teams.getNoRewards());
        }

        WinEvent winEvent = new WinEvent(_lastWinners, losers);

        Bukkit.getPluginManager().callEvent(winEvent);

        String winString = C.Gold;

        for (int i = 0; i < players.size(); i++)
        {
            winString += players.get(i).getName();

            int left = players.size() - i;

            if (left == 2)
                winString += C.Yellow + " and " + C.Gold;
            else if (left > 2)
                winString += C.Yellow + ", " + C.Gold;
        }

        Bukkit.broadcastMessage(winString + " wins!");

        _arcadeManager.getGame().setState(GameState.End);
    }

    public void setWin(GameTeam team)
    {
        _lastWinners = (ArrayList<UUID>) team.getRewardable().clone();
        _lastColor = team.getColor();

        ArrayList<UUID> losers = new ArrayList<UUID>(getManager().getGame().getPlayed().keySet());
        losers.removeAll(_lastWinners);

        for (GameTeam teams : getGame().getTeams())
        {
            losers.removeAll(teams.getNoRewards());
        }

        WinEvent winEvent = new WinEvent(_lastWinners, losers);

        Bukkit.getPluginManager().callEvent(winEvent);

        Bukkit.broadcastMessage(team.getColoring() + team.getName() + " wins!");

        _arcadeManager.getGame().setState(GameState.End);
    }

    public void setWin(Player player)
    {
        _lastWinners.clear();

        if (player != null)
            _lastWinners.add(player.getUniqueId());

        _lastColor = getManager().getGame().getTeams().get(0).getColor();

        ArrayList<UUID> losers = new ArrayList<UUID>(getManager().getGame().getPlayed().keySet());
        losers.removeAll(_lastWinners);

        WinEvent winEvent = new WinEvent(_lastWinners, losers);

        Bukkit.getPluginManager().callEvent(winEvent);

        if (player != null)
            Bukkit.broadcastMessage(C.Gold + player.getName() + " wins!");
        else
            Bukkit.broadcastMessage(C.Gold + "No one won!");

        _arcadeManager.getGame().setState(GameState.End);
    }
}
