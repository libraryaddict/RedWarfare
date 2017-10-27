package me.libraryaddict.arcade.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandTeamChat extends SimpleCommand
{
    public CommandTeamChat()
    {
        super(new String[]
            {
                    "t"
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
        player.chat("@" + UtilString.join(args, " "));
    }
}
