package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandTop extends SimpleCommand
{

    public CommandTop()
    {
        super(new String[]
            {
                    "top", "up"
            }, Rank.MOD);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (rank.hasRank(Rank.MOD))
        {
            completions.addAll(getPlayers(token));
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        boolean isStaff = rank.hasRank(Rank.MOD);

        if ((args.length > 0 && !isStaff) || args.length > 1)
        {
            player.sendMessage(C.Red + "/top");

            if (isStaff)
            {
                player.sendMessage(C.Red + "/top <Player>");
            }

            return;
        }

        Player teleporter = args.length == 0 ? player : Bukkit.getPlayer(args[0]);

        if (teleporter == null)
        {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
            return;
        }

        Location loc = teleporter.getLocation();

        int y = teleporter.getWorld().getHighestBlockYAt(loc);

        if (y < loc.getY())
        {
            player.sendMessage(C.Red + (teleporter == player ? "You're" : "They're") + " already at the highest point");
            return;
        }

        if (player != teleporter)
        {
            teleporter.sendMessage(C.Red + player.getName() + " teleported you");
        }

        loc.setY(y);

        UtilPlayer.tele(teleporter, loc);
    }

}
