package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandTeleportAll extends CommandBaseTeleport
{
    public CommandTeleportAll()
    {
        super(new String[]
            {
                    "teleportall", "tpall", "teleall"
            }, new Rank[]
            {
                    Rank.ADMIN
            }, TeleportType.TO_LOCATION, TeleportType.TO_PLAYER);
    }

    @Override
    public boolean hasPermission(Player player, PlayerRank rank, TeleportType teleportType)
    {
        return rank.hasRank(Rank.ADMIN);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0)
        {
            completions.addAll(getPlayers(token));
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        try
        {
            Location destination = getDestination(player, rank, alias, args);

            if (destination == null)
                return;

            for (Player teleporter : UtilPlayer.getPlayers())
            {
                if (teleporter != player)
                {
                    teleporter.sendMessage(C.Blue + player.getName() + " teleported you");
                }

                UtilPlayer.tele(teleporter, destination);
            }
        }
        catch (Exception ex)
        {
            sendInfo(player, rank, alias);
        }
    }
}
