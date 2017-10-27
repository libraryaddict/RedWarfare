package me.libraryaddict.core.referal.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.referal.database.MysqlDeleteReferal;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.mysql.operations.MysqlFetchReferals;
import me.libraryaddict.mysql.operations.MysqlFetchReferals.Referal;

public class CommandDeleteReferal extends SimpleCommand
{
    public CommandDeleteReferal()
    {
        super("deletereferal", Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0)
            completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/deletereferal <Player>");
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchReferals fetchReferals = new MysqlFetchReferals(player.getUniqueId(), null, null, false);

                for (String name : args)
                {
                    Referal ref = null;

                    for (Referal referal : fetchReferals.getReferals())
                    {
                        if (!referal.getRefered().getValue().equalsIgnoreCase(name))
                            continue;

                        ref = referal;
                        break;
                    }

                    if (ref == null)
                    {
                        UtilPlayer.sendMessage(player, C.Red + "You are not refering the player '" + name + "'");
                        continue;
                    }

                    if (ref.isCompleted())
                    {
                        UtilPlayer.sendMessage(player, C.Red + "This referal has already been completed");
                        continue;
                    }

                    new MysqlDeleteReferal(ref.getRefered().getKey());

                    UtilPlayer.sendMessage(player,
                            C.Red + "You are no longer refering the player " + ref.getRefered().getValue());
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
