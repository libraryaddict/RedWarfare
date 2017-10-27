package me.libraryaddict.arcade.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.arcade.database.mysql.MysqlRefundKit;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;

public class CommandRefundKit extends SimpleCommand
{
    private GameManager _game;

    public CommandRefundKit(GameManager gameManager)
    {
        super("refundkit", Rank.OWNER);

        _game = gameManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        for (Kit kit : _game.getGame().getKits())
        {
            if (kit.getPrice() <= 0)
                continue;

            completions.add(kit.getName());
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/refundkit <Kit>");
            return;
        }

        Kit kit = _game.getGame().getKit(UtilString.join(args, " "));

        if (kit == null)
        {
            player.sendMessage(C.Red + "Kit " + UtilString.join(args, " ") + " not found");
            return;
        }

        int toRefund = kit.getPrice();
        String kitName = _game.getGame().getName() + ".Kit." + kit.getName();

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlRefundKit refundKit = new MysqlRefundKit(kitName, toRefund);

                if (!refundKit.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Error refunding the kit"));
                    return;
                }

                UtilPlayer.sendMessage(player, C.Blue + "Refunded " + refundKit.getRefund().size() + " players");
            }
        }.runTaskAsynchronously(_game.getManager().getPlugin());
    }

}
