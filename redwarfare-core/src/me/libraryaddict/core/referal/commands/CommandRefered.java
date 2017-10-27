package me.libraryaddict.core.referal.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlFetchReferals;
import me.libraryaddict.mysql.operations.MysqlFetchReferals.Referal;

public class CommandRefered extends SimpleCommand
{
    public CommandRefered()
    {
        super("refered", Rank.ALL);
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
        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchReferals fetchReferals = new MysqlFetchReferals(player.getUniqueId(), null, null);

                if (fetchReferals.getReferals().isEmpty())
                {
                    UtilPlayer.sendMessage(player, C.Red + "You have not refered any players");
                    return;
                }

                FancyMessage message = new FancyMessage(C.Gold + "Referals: ");

                Iterator<Referal> itel = fetchReferals.getReferals().iterator();

                while (itel.hasNext())
                {
                    Referal referal = itel.next();

                    message.then((referal.isCompleted() ? C.DGreen : C.Red) + referal.getRefered().getValue());

                    ArrayList<String> tooltip = new ArrayList<String>();

                    tooltip.add(C.Blue + "Refered: " + UtilTime.parse(referal.getReferedWhen()));

                    if (referal.isCompleted())
                    {
                        tooltip.add(C.Blue + "Completed: " + UtilTime.parse(referal.getReferCompleted()));
                    }
                    else
                    {
                        message.suggest("/deletereferal " + referal.getRefered().getValue());
                    }

                    message.tooltip(tooltip);

                    if (itel.hasNext())
                    {
                        message.then(C.Gold + ",");
                    }
                }

                UtilPlayer.sendMessage(player, message);
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
