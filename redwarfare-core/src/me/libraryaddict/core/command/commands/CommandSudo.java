package me.libraryaddict.core.command.commands;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;

public class CommandSudo extends SimpleCommand
{
    private CommandManager _commandManager;

    public CommandSudo(CommandManager commandManager)
    {
        super("sudo", Rank.OWNER);

        _commandManager = commandManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        completions.addAll(getPlayers(token));

        if ("all".startsWith(token.toLowerCase()))
            completions.add("All");
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length < 2)
        {
            player.sendMessage(C.Red + "/" + alias + " <Target> <Action>");

            return;
        }

        boolean all = args[0].equalsIgnoreCase("All");

        Player toReceive = !all ? Bukkit.getPlayer(args[0]) : null;

        if (!all && toReceive == null)
        {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
            return;
        }

        args = Arrays.copyOfRange(args, 1, args.length);
        String toDo = UtilString.join(args, " ");
        SimpleCommand command = null;
        String commandAlias = null;

        if (toDo.startsWith("/"))
        {
            commandAlias = args[0].substring(1);
            command = _commandManager.getCommand(commandAlias);

            if (command == null)
            {
                player.sendMessage(C.Red + "Command '" + commandAlias + "' not found");
                return;
            }

            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (toReceive != null)
        {
            if (command != null)
            {
                command.runCommand(toReceive, rank, commandAlias, args);
            }
            else
            {
                toReceive.chat(toDo);
            }
        }
        else
        {
            for (Player p : UtilPlayer.getPlayers())
            {
                if (command != null)
                {
                    command.runCommand(p, rank, commandAlias, args);
                }
                else
                {
                    p.chat(toDo);
                }
            }
        }
    }

}
