package me.libraryaddict.core.command.commands;

import java.util.Arrays;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public abstract class CommandBaseTeleport extends SimpleCommand
{
    protected enum TeleportType
    {
        PLAYER_TO_LOCATION(4, "<Player> X Y Z"), PLAYER_TO_PLAYER(2, "<Player> <Player>"), TO_LOCATION(3, "X Y Z"), TO_PLAYER(1,
                "<Player>");

        private int argsAmount;
        private String command;

        private TeleportType(int args, String usage)
        {
            this.argsAmount = args;
            command = usage;
        }
    }

    private Vector<TeleportType> _allowed;

    public CommandBaseTeleport(String commandAlias, Rank[] allowedRanks, TeleportType... allowed)
    {
        super(commandAlias, allowedRanks);

        _allowed = new Vector(Arrays.asList(allowed));
    }

    public CommandBaseTeleport(String[] commandAliases, Rank[] allowedRanks, TeleportType... allowed)
    {
        super(commandAliases, allowedRanks);

        _allowed = new Vector(Arrays.asList(allowed));
    }

    protected boolean canUse(Player player, PlayerRank rank, TeleportType teleType, String[] args)
    {
        return hasPermission(player, rank, teleType) && teleType.argsAmount == args.length && _allowed.contains(teleType);
    }

    public Location getDestination(Player player, PlayerRank rank, String alias, String[] args) throws Exception
    {
        if (canUse(player, rank, TeleportType.TO_PLAYER, args))
        {
            return getToPlayer(player, args);
        }
        else if (canUse(player, rank, TeleportType.PLAYER_TO_PLAYER, args))
        {
            return getPlayerToPlayer(player, args);
        }
        else if (canUse(player, rank, TeleportType.TO_LOCATION, args))
        {
            return getToLocation(player, args);
        }
        else if (canUse(player, rank, TeleportType.PLAYER_TO_LOCATION, args))
        {
            return getPlayerToLocation(player, args);
        }

        throw new Exception();
    }

    public Location getPlayerToLocation(Player player, String[] args)
    {
        args = Arrays.copyOfRange(args, 1, 4);

        Location teleportTo = parseLocation(player.getLocation(), args);

        if (teleportTo == null)
        {
            player.sendMessage(C.Red + "Unable to parse '" + UtilString.join(args, " ") + "' to a valid X Y Z");
            return null;
        }

        return teleportTo;
    }

    public Location getPlayerToPlayer(Player player, String[] args)
    {
        Player teleTo = Bukkit.getPlayer(args[1]);

        if (teleTo == null)
        {
            player.sendMessage(C.Red + "Player '" + args[1] + "' not found");
            return null;
        }

        return teleTo.getLocation();
    }

    public Player getTeleporter(Player player, PlayerRank rank, String alias, String[] args) throws Exception
    {
        if (canUse(player, rank, TeleportType.TO_PLAYER, args))
        {
            return getTeleporterToPlayer(player, args);
        }
        else if (canUse(player, rank, TeleportType.PLAYER_TO_PLAYER, args))
        {
            return getTeleporterPlayerToPlayer(player, args);
        }
        else if (canUse(player, rank, TeleportType.TO_LOCATION, args))
        {
            return getTeleporterToLocation(player, args);
        }
        else if (canUse(player, rank, TeleportType.PLAYER_TO_LOCATION, args))
        {
            return getTeleporterPlayerToLocation(player, args);
        }

        throw new Exception();
    }

    public Player getTeleporterPlayerToLocation(Player player, String[] args)
    {
        Player teleporter = Bukkit.getPlayer(args[0]);

        if (teleporter == null)
        {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
        }

        return teleporter;
    }

    public Player getTeleporterPlayerToPlayer(Player player, String[] args)
    {
        Player teleporter = Bukkit.getPlayer(args[0]);

        if (teleporter == null)
        {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
        }

        return teleporter;
    }

    public Player getTeleporterToLocation(Player player, String[] args)
    {
        return player;
    }

    public Player getTeleporterToPlayer(Player player, String[] args)
    {
        return player;
    }

    public Location getToLocation(Player player, String[] args)
    {
        Location teleportTo = parseLocation(player.getLocation(), args);

        if (teleportTo == null)
        {
            player.sendMessage(C.Red + "Unable to parse '" + UtilString.join(args, " ") + "' to a valid X Y Z");
        }

        return teleportTo;
    }

    public Location getToPlayer(Player player, String[] args)
    {
        Player teleTo = Bukkit.getPlayer(args[0]);

        if (teleTo == null)
        {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
            return null;
        }

        return teleTo.getLocation();
    }

    public abstract boolean hasPermission(Player player, PlayerRank rank, TeleportType teleportType);

    private Location parseLocation(Location loc, String... args)
    {
        try
        {
            assert args.length == 3;

            for (String arg : args)
            {
                if (arg.startsWith("~"))
                    arg = arg.substring(1);

                if (!arg.isEmpty())
                    Double.parseDouble(arg);
            }
        }
        catch (Exception ex)
        {
            return null;
        }

        Location newLoc = loc.clone();

        for (int i = 0; i < 3; i++)
        {
            String arg = args[i];

            double l = 0;

            if (arg.startsWith("~"))
            {
                arg = arg.substring(1);

                switch (i)
                {
                case 0:
                    l = loc.getX();
                    break;
                case 1:
                    l = loc.getY();
                    break;
                case 2:
                    l = loc.getZ();
                    break;
                }
            }

            if (!arg.isEmpty())
            {
                l += Double.parseDouble(arg);
            }

            switch (i)
            {
            case 0:
                newLoc.setX(l);
                break;
            case 1:
                newLoc.setY(l);
                break;
            case 2:
                newLoc.setZ(l);
                break;
            }
        }

        return newLoc;
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        sendInfo(player, rank, alias);
    }

    public void sendInfo(Player player, PlayerRank rank, String alias)
    {
        for (TeleportType teleType : _allowed)
        {
            if (!hasPermission(player, rank, teleType))
                continue;

            player.sendMessage(C.Red + "/" + alias + " " + teleType.command);
        }
    }
}
