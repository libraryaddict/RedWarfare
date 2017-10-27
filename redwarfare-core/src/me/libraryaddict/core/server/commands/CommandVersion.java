package me.libraryaddict.core.server.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.server.ServerManager;

public class CommandVersion extends SimpleCommand
{
    private ServerManager _serverManager;
    private String _version;

    public CommandVersion(JavaPlugin plugin, ServerManager serverManager)
    {
        super("version", Rank.MOD);

        _version = plugin.getDescription().getVersion();
        _serverManager = serverManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        player.sendMessage(_version);

        if (_serverManager.isUpdateReady())
        {
            player.sendMessage(C.Gold + "This server is outdated");
        }
        else
        {
            player.sendMessage(C.Gold + "Running the latest version!");
        }
    }

}
