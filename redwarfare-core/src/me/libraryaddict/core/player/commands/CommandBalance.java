package me.libraryaddict.core.player.commands;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;

public class CommandBalance extends SimpleCommand
{
    public CommandBalance()
    {
        super(new String[]
            {
                    "bal", "balance"
            }, Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Gold + "You have " + C.Yellow + Currency.get(player, CurrencyType.CREDIT) + " credits");
            player.sendMessage(C.DGreen + "You have " + C.Green + Currency.get(player, CurrencyType.POINT) + " points");
            player.sendMessage(C.DAqua + "You have " + C.Aqua + Currency.get(player, CurrencyType.TOKEN) + " tokens");
        }
        else
        {
            Player p = Bukkit.getPlayer(args[0]);

            if (p == null)
            {
                player.sendMessage(C.Red + "Cannot find the player '" + args[0] + "'");
                return;
            }

            player.sendMessage(C.Gold + p.getName() + " has " + C.Yellow + Currency.get(p, CurrencyType.CREDIT) + " credits");
            player.sendMessage(C.DGreen + p.getName() + " has " + C.Green + Currency.get(p, CurrencyType.POINT) + " points");
            player.sendMessage(C.DAqua + p.getName() + " has " + C.Aqua + Currency.get(p, CurrencyType.TOKEN) + " tokens");
        }
    }

}
