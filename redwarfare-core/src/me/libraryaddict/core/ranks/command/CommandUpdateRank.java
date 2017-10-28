package me.libraryaddict.core.ranks.command;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlFetchRankData;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.mysql.operations.MysqlUpdateRank;

public class CommandUpdateRank extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandUpdateRank(JavaPlugin plugin)
    {
        super(new String[]
            {
                    "updaterank", "giverank"
            }, Rank.ADMIN);

        _plugin = plugin;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 1)
        {
            for (Rank r : Rank.values())
            {
                if (r == Rank.ALL)
                    continue;

                if (r.name().toLowerCase().startsWith(token.toLowerCase()))
                {
                    completions.add(r.name());
                }
            }
        }
        else if (args.length == 0)
        {
            completions.addAll(getPlayers(token));
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length != 2 && args.length != 3)
        {
            player.sendMessage(C.Red + "/" + alias + " <Player> <Rank>");
            player.sendMessage(C.Red
                    + "To just add/remove their rank, don't provide a third parameter. To change when the rank expires, use 30d = 30 days, 1y = 1 year, perm = permanant. Etc.");
            return;
        }
        try {
            if (Rank.valueOf(args[1].toUpperCase()) == Rank.ALL) {
                player.sendMessage(C.Red + C.Bold + "Error " + C.Gray + "The player already owns rank 'ALL'");
                return;
            }
        } catch (IllegalArgumentException ex) {
            player.sendMessage(C.Red + C.Bold + "Error " + C.Gray + "The rank '" + args[1] + "' is not a valid rank");
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchUUID fetchUUID = new MysqlFetchUUID(args[0]);

                if (!fetchUUID.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Database error"));
                    return;
                }

                if (fetchUUID.getUUID() == null)
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Cannot find the player '" + args[0] + "'"));
                    return;
                }

                UUID uuid = fetchUUID.getUUID();

                MysqlFetchRankData fetchRank = new MysqlFetchRankData(uuid);

                if (!fetchRank.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Database error"));
                    return;
                }

                long expires = -1;
                long previous = -1;

                if (fetchRank.getRanks().containsKey(args[1].toUpperCase()))
                {
                    previous = fetchRank.getRanks().get(args[1].toUpperCase());

                    if (previous > 0 && UtilTime.elasped(previous))
                    {
                        previous = -1;
                    }
                }

                if (args.length > 2)
                {
                    if (!args[2].equalsIgnoreCase("perm"))
                    {
                        expires = UtilNumber.parseNumber(args[2]);

                        if (expires == -1)
                        {
                            UtilPlayer.sendMessage(player, UtilError.format("Unable to parse '" + args[2] + "'"));
                            return;
                        }
                    }
                    else
                    {
                        expires = 0;
                    }
                }
                else
                {
                    expires = previous == -1 ? 0 : -1;
                }

                new MysqlUpdateRank(uuid, Rank.valueOf(args[1].toUpperCase()), expires);

                if (expires < 0 || previous != -1)
                {
                    UtilPlayer.sendMessage(player,
                            C.Blue + (expires == -1 ? "Removed " : "Replaced ") + args[0] + "'s rank " + args[1].toUpperCase()
                                    + " which " + (previous == 0 ? "was perm"
                                            : "expired in " + UtilNumber.getTime(previous - System.currentTimeMillis())));
                }

                if (expires >= 0)
                {
                    UtilPlayer.sendMessage(player,
                            C.Blue + "Given " + args[0] + " the rank " + args[1].toUpperCase() + " which " + (expires == 0
                                    ? "is perm" : "expires in " + UtilNumber.getTime(expires - System.currentTimeMillis())));
                }
            }
        }.runTask(_plugin);
    }

}
