package me.libraryaddict.core.redeem.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.redeem.RedeemCallback;
import me.libraryaddict.core.redeem.RedeemCode;
import me.libraryaddict.core.redeem.RedeemManager;
import me.libraryaddict.core.redeem.database.MysqlFetchRedeemCodes;
import me.libraryaddict.core.redeem.database.MysqlRedeemCode;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandRedeem extends SimpleCommand {
    private RedeemManager _redeemManager;

    public CommandRedeem(RedeemManager redeemManager) {
        super(new String[] {
                "redeem", "redeemvip"
        }, Rank.ALL);

        _redeemManager = redeemManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length != 1) {
            player.sendMessage(C.Red + "/redeem <Code>");
            player.sendMessage(
                    C.Blue + "Alternatively if you're looking to see what codes you own and who redeemed them, use /redeemed");
            return;
        }

        if (!Recharge.canUse(player, "Redeem Code")) {
            player.sendMessage(C.Red + "You're redeeming codes too fast!");
            return;
        }

        Recharge.use(player, "Redeem Code", 1500);

        new BukkitRunnable() {
            public void run() {
                MysqlFetchRedeemCodes fetchCode = new MysqlFetchRedeemCodes(args[0]);
                RedeemCode code = fetchCode.getCode();

                if (code == null) {
                    UtilPlayer.sendMessage(player, C.Red + "That code is invalid!");
                    return;
                }

                if (code.isRedeemed()) {
                    UtilPlayer.sendMessage(player, C.Red + "That code has already been redeemed!");
                    return;
                }

                new BukkitRunnable() {
                    public void run() {
                        if (!player.isOnline())
                            return;

                        RedeemCallback callback = _redeemManager.getCallback(code.getType());

                        if (callback == null) {
                            player.sendMessage(C.Red + "Error! Code is valid, but I don't know what to do with it!");
                            return;
                        }

                        if (!callback.canRedeem(player)) {
                            return;
                        }

                        Recharge.use(player, "Redeem Code", 60000);

                        callback.onRedeem(player);

                        new BukkitRunnable() {
                            public void run() {
                                new MysqlRedeemCode(code.getCode(), player.getUniqueId());
                            }
                        }.runTaskAsynchronously(getPlugin());
                    }
                }.runTask(getPlugin());
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
