package me.libraryaddict.core.server.commands;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.redis.operations.RedisSwitchServer;

public class CommandServer extends SimpleCommand
{
    private ServerManager _serverManager;

    public CommandServer(ServerManager serverManager)
    {
        super("server", Rank.ALL);

        _serverManager = serverManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.DGreen + "You are in the server " + ServerManager.getServerName());
            return;
        }

        UUID uuid = player.getUniqueId();
        String serverName = UtilString.join(args, " ");

        new BukkitRunnable()
        {
            public void run()
            {
                new RedisSwitchServer(uuid, serverName);
            }
        }.runTaskAsynchronously(_serverManager.getPlugin());
    }

}
