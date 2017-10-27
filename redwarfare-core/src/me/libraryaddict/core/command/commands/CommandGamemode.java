package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilNumber;

public class CommandGamemode extends SimpleCommand
{
    public CommandGamemode()
    {
        super(new String[]
            {
                    "gamemode", "gm"
            }, Rank.ADMIN);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0)
        {
            completions.addAll(getPlayers(token));
        }

        if (args.length < 2)
        {
            for (String s : new String[]
                {
                        "Survival", "Creative", "Adventure", "Spectator"
                })
            {
                if (s.toLowerCase().startsWith(token.toLowerCase()))
                {
                    completions.add(s);
                }
            }
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/" + alias + " <Survival, Creative, Adventure, Spectator>");
            player.sendMessage(C.Red + "/" + alias + " <Target> <Survival, Creative, Adventure, Spectator>");
            return;
        }

        Player toModify = player;
        GameMode gamemode;

        try
        {
            if (UtilNumber.isParsableInt(args[0]))
                gamemode = GameMode.getByValue(Integer.parseInt(args[0]));
            else
                gamemode = GameMode.valueOf(args[0].toUpperCase());
        }
        catch (Exception ex)
        {
            if (args.length < 2)
            {
                player.sendMessage(C.Red + "Unrecognized gamemode '" + args[0] + "'");
                return;
            }

            toModify = Bukkit.getPlayer(args[0]);

            if (toModify == null)
            {
                player.sendMessage(C.Red + "Unknown player '" + args[0] + "'");
                return;
            }

            try
            {
                if (UtilNumber.isParsableInt(args[1]))
                    gamemode = GameMode.getByValue(Integer.parseInt(args[1]));
                else
                    gamemode = GameMode.valueOf(args[1].toUpperCase());
            }
            catch (Exception e)
            {
                player.sendMessage(C.Red + "Unrecognized gamemode '" + args[1] + "'");
                return;
            }
        }

        toModify.setGameMode(gamemode);

        toModify.sendMessage(C.Blue + "Gamemode changed to " + gamemode.name().toLowerCase());

        if (toModify != player)
            player.sendMessage(C.Blue + toModify.getName() + "'s gamemode changed to " + gamemode.name().toLowerCase());
    }

}
