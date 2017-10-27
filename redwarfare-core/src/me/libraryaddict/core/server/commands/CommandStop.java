package me.libraryaddict.core.server.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.server.ServerManager;

public class CommandStop extends SimpleCommand
{
    private ServerManager _serverManager;

    public CommandStop(ServerManager serverManager)
    {
        super("stop", Rank.MOD);

        _serverManager = serverManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        _serverManager.shutdown(player.getName() + " closed the server");
    }

}
