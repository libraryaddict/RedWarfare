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
import me.libraryaddict.core.ranks.RankInfo;
import me.libraryaddict.core.ranks.mysql.MysqlFetchClaiment;
import me.libraryaddict.core.ranks.mysql.MysqlSetClaimed;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlUpdateRank;

public class CommandRedeemVIP extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandRedeemVIP(JavaPlugin plugin)
    {
        super(new String[]
            {
                    "redeemvip"
            }, Rank.ALL);

        _plugin = plugin;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        RankInfo info = rank.getRank(Rank.VIP);

        if (info != null && info.getExpires() == 0)
        {
            player.sendMessage(C.Red + "You already have unlimited VIP!");
            return;
        }

        UUID uuid = player.getUniqueId();

        if (args.length == 0)
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    MysqlFetchClaiment fetch = new MysqlFetchClaiment(uuid, null);

                    if (!fetch.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Error fetching data"));
                        return;
                    }

                    if (fetch.getCode() == null)
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("You do not have a code!"));
                        return;
                    }

                    if (fetch.getClaimed() != null)
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Your code has already been claimed!"));
                        return;
                    }

                    UtilPlayer.sendMessage(player, C.Blue + "Use /redeem " + fetch.getCode() + " to claim 6 months of VIP!");
                }
            }.runTaskAsynchronously(_plugin);

            return;
        }

        if (args[0].length() != 8)
        {
            player.sendMessage(C.Red + "This is not a valid code!");
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchClaiment fetch = new MysqlFetchClaiment(null, args[0]);

                if (!fetch.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Error fetching data"));
                    return;
                }

                if (fetch.getCode() == null)
                {
                    UtilPlayer.sendMessage(player, UtilError.format("That is not a valid code!"));
                    return;
                }

                if (fetch.getClaimed() != null)
                {
                    UtilPlayer.sendMessage(player, UtilError.format("This code has already been claimed!"));
                    return;
                }

                new MysqlSetClaimed(args[0], uuid);
                new MysqlUpdateRank(uuid, Rank.VIP, System.currentTimeMillis() + (UtilTime.MONTH * 6000));
            }
        }.runTaskAsynchronously(_plugin);
    }

}
