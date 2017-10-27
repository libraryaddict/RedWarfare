package me.libraryaddict.arcade.game.survivalgames.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.arcade.game.survivalgames.SurvivalGames;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;

public class CommandPoints extends SimpleCommand
{
    private SurvivalGames _survivalGames;

    public CommandPoints(SurvivalGames survivalGames)
    {
        super("points", Rank.ALL);

        _survivalGames = survivalGames;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        player.sendMessage(C.Blue + "You have " + Currency.get(player, CurrencyType.POINT) + " points");
    }

}
