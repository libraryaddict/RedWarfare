package me.libraryaddict.arcade.commands;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Predicate;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilItem;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandClearInventory extends SimpleCommand
{
    private ArcadeManager _arcadeManager;

    public CommandClearInventory(ArcadeManager arcadeManager)
    {
        super(new String[]
            {
                    "clearinventory", "clear", "clearinv", "ci"
            }, Rank.OWNER);

        _arcadeManager = arcadeManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        Predicate<Player> toReceive = null;

        if (rank.hasRank(Rank.OWNER) && args.length > 0)
        {
            if (args[0].equalsIgnoreCase("All"))
            {
                toReceive = p -> true;
            }
            else if (args.length > 1)
            {
                Player p = args.length > 1 ? Bukkit.getPlayer(args[0]) : null;

                for (GameTeam team : _arcadeManager.getGame().getTeams())
                {
                    if (!team.getName().split(" ")[0].equalsIgnoreCase(args[0]))
                        continue;

                    toReceive = arg -> team.isAlive(arg) && team.isInTeam(arg);
                }

                if (p != null && toReceive == null)
                {
                    toReceive = arg -> arg == p;
                }
            }
        }

        if (toReceive != null)
        {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        else
        {
            toReceive = p -> p == player;
        }

        Material toRemove = null;

        if (args.length > 0)
        {
            if (UtilItem.getItem(args[0]) != null)
            {
                toRemove = UtilItem.getItem(args[0]).getKey();
            }
            else
            {
                player.sendMessage(C.Red + "Unknown item '" + args[0] + "'");
            }
        }

        for (Player p : UtilPlayer.getPlayers())
        {
            if (!toReceive.apply(p))
                continue;

            if (toRemove == null)
            {
                p.sendMessage(C.Blue + "Cleared inventory!");

                UtilInv.clearInventory(p);
            }
            else
            {
                UtilInv.remove(p, toRemove);
            }
        }
    }
}
