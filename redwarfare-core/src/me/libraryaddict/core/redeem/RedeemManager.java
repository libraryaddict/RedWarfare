package me.libraryaddict.core.redeem;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.redeem.commands.CommandRedeem;
import me.libraryaddict.core.redeem.commands.CommandRedeemed;
import me.libraryaddict.core.redeem.database.MysqlCreateRedeemCode;
import me.libraryaddict.core.redeem.redeemcallbacks.RankRedeemCallback;
import me.libraryaddict.core.utils.UtilTime;

/**
 * Deals with redeeming things for code, aka you need to add "Hooks"
 */
public class RedeemManager extends MiniPlugin
{
    private HashMap<String, RedeemCallback> _callbacks = new HashMap<String, RedeemCallback>();

    public RedeemManager(JavaPlugin plugin, CommandManager commandManager, RankManager rankManager)
    {
        super(plugin, "Redeem Manager");

        commandManager.registerCommand(new CommandRedeem(this));
        commandManager.registerCommand(new CommandRedeemed());

        registerCallbacks(rankManager);
    }

    public void addCallback(RedeemCallback callback)
    {
        _callbacks.put(callback.getName(), callback);
    }

    public void assignCode(Player player, RedeemCallback redeemCallback)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                MysqlCreateRedeemCode redeemCode = new MysqlCreateRedeemCode(player.getUniqueId(), redeemCallback.getName());

                new BukkitRunnable()
                {
                    public void run()
                    {
                        if (!redeemCode.isSuccess())
                        {
                            redeemCallback.onCodeAssignFailure(player);
                        }
                        else
                        {
                            redeemCallback.onCodeAssign(player, redeemCode.getCode());
                        }
                    }
                }.runTask(getPlugin());
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public RedeemCallback getCallback(String name)
    {
        return _callbacks.get(name);
    }

    private void registerCallbacks(RankManager rankManager)
    {
        addCallback(new RankRedeemCallback(rankManager, Rank.VIP, "VIP 1 Week", 50, UtilTime.WEEK));
        addCallback(new RankRedeemCallback(rankManager, Rank.VIP, "VIP 1 Month", 180, UtilTime.MONTH));
        addCallback(new RankRedeemCallback(rankManager, Rank.VIP, "VIP 3 Months", 450, UtilTime.MONTH * 3));
        addCallback(new RankRedeemCallback(rankManager, Rank.VIP, "VIP 6 Months", 750, UtilTime.MONTH * 6));
    }

}
