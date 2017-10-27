package me.libraryaddict.core.bans.commands;

import java.util.ArrayList;
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
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlFetchBanInfo;
import me.libraryaddict.mysql.operations.MysqlFetchPlayerHistory;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.network.BanInfo;
import me.libraryaddict.network.PlayerHistory;

public class CommandBanInfo extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandBanInfo(JavaPlugin plugin)
    {
        super(new String[]
            {
                    "baninfo", "muteinfo"
            }, Rank.MOD);

        _plugin = plugin;
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
        if (args.length != 1)
        {
            player.sendMessage(C.Red + "/" + alias + " <Player/IP>");
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

        BukkitRunnable runnable = new BukkitRunnable()
        {
            public void run()
            {
                String playerName = arg;
                String newArg = arg;

                if (!arg.contains("."))
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

                    newArg = fetchUUID.getUUID().toString();
                }

                MysqlFetchBanInfo fetchInfo = new MysqlFetchBanInfo(newArg);

                if (fetchInfo.isSuccess() && !arg.contains("."))
                {
                    MysqlFetchPlayerHistory fetchHistory = new MysqlFetchPlayerHistory(UUID.fromString(newArg));

                    if (fetchHistory.isSuccess())
                    {
                        PlayerHistory history = fetchHistory.getHistory();

                        if (history.getName() != null)
                        {
                            playerName = history.getName();
                        }

                        /*	if (UtilTime.elasped(history.getLastJoined(), UtilNumber.MONTH * 1000))
                        	{
                        		new BukkitRunnable()
                        		{
                        			public void run()
                        			{
                        				player.sendMessage(C.Red + "'" + playerName + "' has not !");
                        			}
                        		}.runTask(_plugin);
                        
                        		return;
                        	}*/
                    }
                }

                if (!fetchInfo.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Error connecting to database"));

                    return;
                }

                ArrayList<BanInfo> banInfo = fetchInfo.getBanInfo();

                if (banInfo.isEmpty())
                {
                    UtilPlayer.sendMessage(player, C.Red + "No bans or mutes recorded for " + playerName);

                    return;
                }

                for (BanInfo info : banInfo)
                {
                    UtilPlayer.sendMessage(player,
                            C.Red + (info.isBan() ? "Banned: " : "Muted: ") + C.Gray + info.getBanned() + " " + C.Red
                                    + (info.isBan() ? "Banned" : "Muted") + " By: " + info.getBanner() + " Reason: " + C.Gray
                                    + info.getReason() + " " + C.Red + "When: " + C.Gray + UtilTime.parse(info.getBannedWhen())
                                    + C.Red + "Expires: " + C.Gray + UtilTime.parse(info.getBanExpires())
                                    + (info.isRemoved() ? C.DGreen + " Removed" : ""));
                }
            }
        };

        runnable.runTaskAsynchronously(_plugin);
    }

}
