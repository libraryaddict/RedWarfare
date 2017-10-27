package me.libraryaddict.core.bans.commands;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlFetchBanInfo;
import me.libraryaddict.mysql.operations.MysqlFetchPlayerHistory;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.mysql.operations.MysqlSaveBanInfo;
import me.libraryaddict.network.BanInfo;
import me.libraryaddict.network.BanInfo.BanState;
import me.libraryaddict.network.PlayerHistory;

public class CommandMute extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandMute(JavaPlugin plugin)
    {
        super("mute", Rank.MOD);

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
            player.sendMessage(C.Red + "/mute <Player>");
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

        long expires = 0;

        if (args.length > 1)
        {
            expires = UtilNumber.parseNumber(args[1]);

            if (expires == -1)
                expires = 0;
        }

        String banner = player.getName();
        long muteExpires = (expires * 1000) + (expires == 0 ? 0 : System.currentTimeMillis());

        new BukkitRunnable()
        {
            public void run()
            {
                String name = arg;
                String uuidToMute = arg;

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

                    uuidToMute = fetchUUID.getUUID().toString();
                }

                MysqlFetchBanInfo fetchBans = new MysqlFetchBanInfo(uuidToMute);

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

                    if (!info.getBanner().equals(banner) && !rank.hasRank(Rank.ADMIN))
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

                if (uuidToMute.contains("-"))
                {
                    MysqlFetchPlayerHistory fetchHistory = new MysqlFetchPlayerHistory(UUID.fromString(uuidToMute));

                    if (fetchHistory.isSuccess())
                    {
                        PlayerHistory history = fetchHistory.getHistory();

                        name = history.getName();
                    }
                }

                String reason = UtilString.join(muteExpires == 0 ? 1 : 2, args, " ");

                BanInfo banInfo = new BanInfo(uuidToMute, banner, reason, new Timestamp(muteExpires), false);

                MysqlSaveBanInfo saveBan = new MysqlSaveBanInfo(banInfo);

                if (!saveBan.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Error while muting the player"));
                    return;
                }

                UtilPlayer.sendMessage(player, C.Red + "You have muted " + name + " for" + (muteExpires == 0 ? "ever"
                        : " " + UtilNumber.getTime(muteExpires - System.currentTimeMillis(), TimeUnit.MILLISECONDS)));

                new RedisNotifyMute(new Mute(UUID.fromString(uuidToMute), banner, reason, muteExpires));
            }
        }.runTaskAsynchronously(_plugin);
    }

}
