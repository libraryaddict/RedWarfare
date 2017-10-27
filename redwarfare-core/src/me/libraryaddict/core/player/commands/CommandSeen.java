package me.libraryaddict.core.player.commands;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.mysql.operations.MysqlFetchPlayerHistory;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;

public class CommandSeen extends SimpleCommand
{
    public CommandSeen()
    {
        super("seen", Rank.MOD);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/seen <Player>");
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchUUID fetchUUID = new MysqlFetchUUID(args[0]);

                if (fetchUUID.getUUID() == null)
                {
                    UtilPlayer.sendMessage(player, C.Red + "Cannot find the player '" + args[0] + "'");
                    return;
                }

                MysqlFetchPlayerHistory fetchHistory = new MysqlFetchPlayerHistory(fetchUUID.getUUID());

                UtilPlayer
                        .sendMessage(player,
                                C.Blue + "Last saw " + fetchHistory.getHistory().getName() + " "
                                        + UtilNumber.getTime(
                                                System.currentTimeMillis() - fetchHistory.getHistory().getLastJoined(),
                                                TimeUnit.MILLISECONDS)
                                        + " ago");
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
