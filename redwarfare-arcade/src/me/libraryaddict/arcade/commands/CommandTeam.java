package me.libraryaddict.arcade.commands;

import java.util.Collection;
import java.util.Iterator;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;

public class CommandTeam extends SimpleCommand
{
    private GameManager _gameManager;

    public CommandTeam(GameManager gameManager)
    {
        super(new String[]
            {
                    "team"
            }, Rank.ALL);

        _gameManager = gameManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        Game game = _gameManager.getGame();

        if (!game.isPreGame())
        {
            player.sendMessage(C.Red + "Cannot pick a team after the game has started");
            return;
        }

        if (_gameManager.getManager().getLobby().isFrozen())
        {
            player.sendMessage(C.Red + "Too late to pick a team!");
            return;
        }

        if (game.getState() == GameState.PreMap)
        {
            player.sendMessage(C.Red + "An early beaver are you? Too early to pick a team!");
            return;
        }

        if (args.length == 0)
        {
            FancyMessage message = new FancyMessage(C.Blue + C.Bold + "Teams: ");

            Iterator<GameTeam> itel = game.getTeams().iterator();

            while (itel.hasNext())
            {
                GameTeam team = itel.next();

                message.then(team.getColoring() + team.getName());
                message.command("team " + team.getName());

                if (itel.hasNext())
                {
                    message.then(C.Blue + ", ");
                }
            }

            message.then(C.Blue + ".");

            message.send(player);
            return;
        }

        GameTeam toJoin = null;

        for (GameTeam team : game.getTeams())
        {
            if (!team.getName().split(" ")[0].equalsIgnoreCase(args[0]))
                continue;

            toJoin = team;
            break;
        }

        if (toJoin == null)
        {
            player.sendMessage(C.Red + "Unknown team: '" + UtilString.join(args, " ") + "'");
            return;
        }

        int maxPlayers = (int) Math.ceil(UtilPlayer.getPlayers().size() / (double) game.getTeams().size());

        if (toJoin.getPlayers().size() >= maxPlayers)
        {
            player.sendMessage(C.Red + "Not enough room in that team for you to join!");
            return;
        }

        GameTeam existing = game.getTeam(player);

        if (existing != null)
        {
            existing.removeFromTeam(player);
        }

        toJoin.addToTeam(player);

        player.sendMessage(C.Gold + "You have joined " + toJoin.getColoring() + toJoin.getName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);
    }

}
