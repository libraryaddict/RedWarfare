package me.libraryaddict.arcade.game.survivalgames.commands;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.arcade.game.survivalgames.BountyManager;
import me.libraryaddict.arcade.game.survivalgames.SurvivalGames;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilNumber;

public class CommandBounty extends SimpleCommand
{
    private SurvivalGames _survivalGames;

    public CommandBounty(SurvivalGames survivalGames)
    {
        super("bounty", Rank.ALL);

        _survivalGames = survivalGames;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length == 0)
        {
            completions.addAll(getPlayers(token));
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (!_survivalGames.isLive())
        {
            player.sendMessage(C.Blue + "Now is not the right time to use that");
            return;
        }

        if (args.length != 2)
        {
            player.sendMessage(C.Blue + "/bounty <Player> <Amount>");
            return;
        }

        Player toBounty = Bukkit.getPlayer(args[0]);

        if (toBounty == null)
        {
            player.sendMessage(C.Blue + "The player '" + args[0] + "' cannot be found");
            return;
        }

        if (!_survivalGames.isAlive(toBounty))
        {
            player.sendMessage(C.Blue + "That player is not alive");
            return;
        }

        if (!UtilNumber.isParsableInt(args[1]))
        {
            player.sendMessage(C.Blue + "'" + args[1] + "' is not a number");
            return;
        }

        int bounty = Integer.parseInt(args[1]);

        if (bounty == 69)
            bounty = (int) Math.min(Currency.get(player, CurrencyType.POINT), 690);

        if (bounty <= 0)
        {
            player.sendMessage(C.Blue + "You cannot afford this bounty of 500,000 points");
            return;
        }

        if (bounty < 50)
        {
            player.sendMessage(C.Blue + "The minimum bounty is 50 points");
            return;
        }

        if (bounty > Currency.get(player, CurrencyType.POINT))
        {
            player.sendMessage(C.Blue + "You cannot afford this bounty of " + bounty + " points");
            return;
        }

        BountyManager bountyManager = _survivalGames.getBountyManager();

        if (bountyManager.getBountiesPlaced(player, toBounty) >= 5)
        {
            player.sendMessage(C.Blue + "You've placed too many bounties on this player");
            return;
        }

        bountyManager.addBounty(player, toBounty, bounty);

        _survivalGames.Announce(
                C.Blue + player.getName() + " has placed a bounty of " + bounty + " on " + toBounty.getName() + "'s head!");

        _survivalGames.displayPoints(toBounty, bountyManager.getKillworth(toBounty));
    }

}
