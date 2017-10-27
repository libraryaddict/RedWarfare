package me.libraryaddict.core.referal.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.referal.database.MysqlAddReferal;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.mysql.operations.MysqlFetchReferals;
import me.libraryaddict.mysql.operations.MysqlFetchReferals.Referal;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.network.FetchUUID;

public class CommandRefer extends SimpleCommand
{
    public CommandRefer()
    {
        super("refer", Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/refer <Player>");
            return;
        }

        if (!Recharge.canUse(player, "Refer Player"))
        {
            player.sendMessage(C.Red + "Wait a second between refering players");
            return;
        }

        Recharge.use(player, "Refer Player", 1000);

        new BukkitRunnable()
        {
            public void run()
            {
                for (String name : args)
                {
                    MysqlFetchUUID fetchUUID = new MysqlFetchUUID(name);

                    if (!fetchUUID.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Error with the database 1 '" + name + "'"));
                        continue;
                    }

                    if (fetchUUID.getUUID() != null)
                    {
                        UtilPlayer.sendMessage(player, C.Red + fetchUUID.getName() + " has already joined the server!");
                        continue;
                    }

                    MysqlFetchReferals fetchReferals = new MysqlFetchReferals(null, null, name);

                    if (!fetchReferals.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Error with the database 2 '" + name + "'"));
                        continue;
                    }

                    for (Referal referal : fetchReferals.getReferals())
                    {
                        UtilPlayer.sendMessage(player, C.Red + referal.getRefered().getValue() + " is already refered by "
                                + referal.getReferer().getValue() + "!");
                    }

                    if (!fetchReferals.getReferals().isEmpty())
                        continue;

                    FetchUUID findUUID = new FetchUUID(name);

                    if (findUUID.getUUID() == null)
                    {
                        UtilPlayer.sendMessage(player, C.Red + "'" + name + "' is not a valid player!");
                    }

                    fetchReferals = new MysqlFetchReferals(null, findUUID.getUUID(), null);

                    if (!fetchReferals.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Error with the database 3 '" + name + "'"));
                        continue;
                    }

                    for (Referal referal : fetchReferals.getReferals())
                    {
                        UtilPlayer.sendMessage(player,
                                C.Red + findUUID.getName() + " is already refered by " + referal.getReferer().getValue() + "!");
                    }

                    if (!fetchReferals.getReferals().isEmpty())
                        continue;

                    fetchReferals = new MysqlFetchReferals(player.getUniqueId(), null, null, true);

                    if (fetchReferals.getReferals().size() > 40)
                    {
                        UtilPlayer.sendMessage(player,
                                C.Red + "You have refered too many players! Remove some of your referals by using /refered");
                        return;
                    }

                    MysqlAddReferal addReferal = new MysqlAddReferal(player.getUniqueId(), findUUID.getUUID(),
                            findUUID.getName());

                    if (!addReferal.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Error with the database 4 '" + name +"'"));
                        continue;
                    }

                    UtilPlayer.sendMessage(player, C.Blue + findUUID.getName() + " is now being refered by you!");
                    UtilPlayer.sendMessage(player, C.Blue + "Once they hit 1 hour playtime, you will both receive tokens!");
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
