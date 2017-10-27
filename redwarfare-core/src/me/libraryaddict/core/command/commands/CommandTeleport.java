package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandTeleport extends CommandBaseTeleport
{
    public CommandTeleport()
    {
        super(new String[]
            {
                    "teleport", "tp", "tele", "goto"
            }, new Rank[]
            {
                    Rank.MOD
            }, TeleportType.values());
    }

    @Override
    public boolean hasPermission(Player player, PlayerRank rank, TeleportType teleportType)
    {
        return rank.hasRank(Rank.MOD);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0 && (hasPermission(player, rank, TeleportType.PLAYER_TO_LOCATION)
                || hasPermission(player, rank, TeleportType.TO_PLAYER)
                || hasPermission(player, rank, TeleportType.PLAYER_TO_PLAYER)))
        {
            completions.addAll(getPlayers(token));
        }
        else if (args.length == 1 && hasPermission(player, rank, TeleportType.PLAYER_TO_PLAYER))
        {
            completions.addAll(getPlayers(token));
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        try
        {
            Player teleporter = getTeleporter(player, rank, alias, args);

            if (teleporter == null)
                return;

            Location destination = getDestination(player, rank, alias, args);

            if (destination == null)
                return;

            if (player != teleporter)
            {
                teleporter.sendMessage(C.Blue + player.getName() + " teleported you");
            }

            UtilPlayer.tele(teleporter, destination);
        }
        catch (Exception ex)
        {
            sendInfo(player, rank, alias);
        }
    }
}
