package me.libraryaddict.core.redeem.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.redeem.RedeemCode;
import me.libraryaddict.core.redeem.database.MysqlFetchRedeemCodes;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandRedeemed extends SimpleCommand
{
    public CommandRedeemed()
    {
        super(new String[]
            {
                    "redeemed", "codes"
            }, Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchRedeemCodes fetchCodes = new MysqlFetchRedeemCodes(player.getUniqueId());

                if (!fetchCodes.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Error fetching from database"));
                    return;
                }

                if (fetchCodes.getCodes().isEmpty())
                {
                    UtilPlayer.sendMessage(player, C.Red + "You don't have any codes!");
                    return;
                }

                for (RedeemCode code : fetchCodes.getCodes())
                {
                    String message = C.Blue + code.getType() + C.Aqua + " - " + C.Blue + code.getCode();

                    if (code.isRedeemed())
                    {
                        message += " " + C.Gray + C.Bold + code.getRedeemer().getValue();
                    }

                    UtilPlayer.sendMessage(player, message);
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
