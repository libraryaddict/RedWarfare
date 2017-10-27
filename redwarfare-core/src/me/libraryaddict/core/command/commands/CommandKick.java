package me.libraryaddict.core.command.commands;

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
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.mysql.operations.MysqlFetchPlayerHistory;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.network.PlayerHistory;
import me.libraryaddict.redis.operations.RedisKickPlayer;

public class CommandKick extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandKick(JavaPlugin plugin)
    {
        super("kick", Rank.MOD);

        _plugin = plugin;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/kick <Player/IP>");
            return;
        }

        String arg = args[0];

        if (arg.contains("."))
        {
            try
            {
                assert arg.split(".").length == 4;

                for (String s : arg.split("."))
                {
                    Integer.parseInt(s);
                }
            }
            catch (Exception ex)
            {
                player.sendMessage(UtilError.format("'" + arg + "' is not a valid IP"));
                return;
            }
        }
        else if (arg.contains("-"))
        {
            try
            {
                UUID.fromString(arg);
            }
            catch (Exception ex)
            {
                player.sendMessage(UtilError.format("'" + arg + "' is not a valid UUID"));
                return;
            }
        }

        String banner = player.getName();

        new BukkitRunnable()
        {
            public void run()
            {
                String name = arg;
                String ipOrUUIDToBan = arg;

                if (!arg.contains(".") && !arg.contains("-"))
                {
                    MysqlFetchUUID fetchUUID = new MysqlFetchUUID(arg);

                    if (!fetchUUID.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Database error"));
                        return;
                    }

                    if (fetchUUID.getUUID() == null)
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("'" + arg + "' is not a valid player"));
                        return;
                    }

                    ipOrUUIDToBan = fetchUUID.getUUID().toString();
                }

                if (ipOrUUIDToBan.contains("-"))
                {
                    MysqlFetchPlayerHistory fetchHistory = new MysqlFetchPlayerHistory(UUID.fromString(ipOrUUIDToBan));

                    if (fetchHistory.isSuccess())
                    {
                        PlayerHistory history = fetchHistory.getHistory();

                        name = history.getName();
                    }
                }

                String reason = UtilString.join(1, args, " ");

                new RedisKickPlayer(ipOrUUIDToBan, C.Red + "You were kicked by " + banner + "\n" + reason);

                UtilPlayer.sendMessage(player, C.Red + "You have kicked " + name);
            }
        }.runTaskAsynchronously(_plugin);
    }

}
