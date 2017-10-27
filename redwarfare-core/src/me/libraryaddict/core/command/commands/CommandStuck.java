package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;

public class CommandStuck extends SimpleCommand
{
    public CommandStuck()
    {
        super("stuck", Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        player.teleport(player.getLocation().add(0, 0.05, 0));
    }

}
