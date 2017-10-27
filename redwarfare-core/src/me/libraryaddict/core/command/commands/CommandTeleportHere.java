package me.libraryaddict.core.command.commands;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandTeleportHere extends SimpleCommand
{
    public CommandTeleportHere()
    {
        super(new String[]
            {
                    "teleporthere", "tphere", "telehere"
            }, Rank.MOD);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0)
        {
            completions.addAll(getPlayers(token));

            if ("all".startsWith(token.toLowerCase()))
            {
                completions.add("All");
            }
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length != 1)
        {
            player.sendMessage(C.Red + "/" + alias + " <Player/All>");
            return;
        }

        ArrayList<Player> teleports = new ArrayList<Player>();

        if (args[0].equalsIgnoreCase("All"))
        {
            teleports.addAll(UtilPlayer.getPlayers());
            teleports.remove(player);
        }
        else
        {
            Player player1 = Bukkit.getPlayer(args[0]);

            if (player1 == null)
            {
                player.sendMessage(C.Red + "/" + alias + " <Player/All>");
                return;
            }

            teleports.add(player1);
        }

        for (Player teleporter : teleports)
        {
            teleporter.sendMessage(C.Blue + player.getName() + " teleported you");

            UtilPlayer.tele(teleporter, player.getLocation());
        }
    }

}
