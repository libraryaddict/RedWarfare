package me.libraryaddict.arcade.commands;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.arcade.managers.LobbyManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandGame extends SimpleCommand
{
    private ArcadeManager _arcadeManager;

    public CommandGame(ArcadeManager arcadeManager)
    {
        super("game", Rank.MOD);

        _arcadeManager = arcadeManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0)
        {
            for (String s : new String[]
                {
                        "forcestop", "start", "stop", "set"
                })
            {
                if (s.startsWith(token.toLowerCase()))
                {
                    completions.add(s);
                }
            }
        }
        else if (args.length == 1)
        {
            if (args[0].equalsIgnoreCase("set"))
            {
                for (ServerType game : ServerType.values())
                {
                    if (game.getName().toLowerCase().startsWith(token.toLowerCase()))
                    {
                        completions.add(game.getName());
                    }
                }
            }
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/game <Start/Stop/Set>");
            return;
        }

        if (args[0].equalsIgnoreCase("set"))
        {
            if (args.length == 1)
            {
                player.sendMessage(C.Red + "/game set <Game>");
                return;
            }

            if (!_arcadeManager.getServer().isTestServer())
            {
                player.sendMessage(C.Red + "You cannot set the game on a production server");
                return;
            }

            for (ServerType display : ServerType.values())
            {
                if (!display.isGame())
                    continue;

                if (!display.getName().replaceAll(" ", "").equalsIgnoreCase(UtilString.join(1, args, "")))
                {
                    continue;
                }

                Bukkit.broadcastMessage(C.Blue + C.Bold + player.getName() + " set next game to " + display.getName());
                _arcadeManager.getGameManager().setNextGame(display);
                return;
            }

            player.sendMessage(C.Red + "Unknown gametype '" + UtilString.join(1, args, " ") + "'");
        }
        else if (args[0].equalsIgnoreCase("stop"))
        {
            if (_arcadeManager.getGame().getState() != GameState.Live)
                return;

            _arcadeManager.getGame().Announce(C.Blue + player.getName() + " ended the game");
            _arcadeManager.getGameManager().endGame();
        }
        else if (args[0].equalsIgnoreCase("forcestop"))
        {
            _arcadeManager.getGame().setState(GameState.Dead);

            player.sendMessage(C.Blue + "Forced the game state to dead");
        }
        else if (args[0].equalsIgnoreCase("start"))
        {
            GameState state = _arcadeManager.getGame().getState();

            if (!state.isPreGame())
            {
                player.sendMessage(C.Red + "Game has already started");
                return;
            }

            LobbyManager lobby = _arcadeManager.getLobby();
            
            lobby.setWaiting(0);

            if (lobby.getCountdown() > 30)
            {
                lobby.setTime(30);
            }
            else if (lobby.getCountdown() > 10)
            {
                lobby.setTime(10);
            }
            else
            {
                lobby.setTime(0);
            }

            player.sendMessage(C.Blue + "Changed the game time");
        }
        else
        {
            player.sendMessage(C.Red + "/game <Start/Stop/Set>");
            return;
        }
    }

}
