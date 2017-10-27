package me.libraryaddict.core.bans.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.player.database.RedisNotifyMute;
import me.libraryaddict.core.player.types.Mute;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlFetchBanInfo;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.mysql.operations.MysqlSaveBanInfo;
import me.libraryaddict.network.BanInfo;
import me.libraryaddict.network.BanInfo.BanState;

public class CommandUnmute extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandUnmute(JavaPlugin plugin)
    {
        super(new String[]
            {
                    "unmute"
            }, Rank.MOD);

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
            player.sendMessage(C.Red + "/unmute <Player>");
            return;
        }

        String arg = args[0];

        if (arg.contains("-"))
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
                String uuidToUnmute = arg;

                if (!arg.contains("-"))
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

                    uuidToUnmute = fetchUUID.getUUID().toString();
                }

                MysqlFetchBanInfo fetchBans = new MysqlFetchBanInfo(uuidToUnmute);

                if (!fetchBans.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Database error"));
                    return;
                }

                ArrayList<BanInfo> previousBanInfo = fetchBans.getBanInfo();
                ArrayList<BanInfo> overwrittenBans = new ArrayList<BanInfo>();

                for (BanInfo info : previousBanInfo)
                {
                    if (info.isRemoved())
                        continue;

                    if (info.isBan())
                        continue;

                    if (!info.getBanner().equals(banner) && !rank.hasRank(Rank.MOD))
                    {
                        UtilPlayer.sendMessage(player,
                                UtilError.format("You do not have permission to override " + info.getBanner() + "'s mute"));
                        return;
                    }

                    overwrittenBans.add(info);
                }

                for (BanInfo info : overwrittenBans)
                {
                    UtilPlayer.sendMessage(player,
                            C.Red + "Muted: " + C.Gray + info.getBanned() + " " + C.Red + "Muted By: " + info.getBanner()
                                    + " Reason: " + C.Gray + info.getReason() + " " + C.Red + "When: " + C.Gray
                                    + UtilTime.parse(info.getBannedWhen()) + C.Red + "Expires: " + C.Gray
                                    + UtilTime.parse(info.getBanExpires()) + (info.isRemoved() ? C.DGreen + " Removed" : ""));

                    info.setBanState(BanState.OVERWRITTEN);

                    MysqlSaveBanInfo saveBan = new MysqlSaveBanInfo(info);

                    if (!saveBan.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("There was an error while overriding previous mutes"));
                    }
                }

                if (overwrittenBans.isEmpty())
                {
                    UtilPlayer.sendMessage(player, C.Red + "There are no mutes for that player");
                }
                else
                {
                    new RedisNotifyMute(new Mute(UUID.fromString(uuidToUnmute), null, null, -1));
                }
            }
        }.runTaskAsynchronously(_plugin);
    }

}
