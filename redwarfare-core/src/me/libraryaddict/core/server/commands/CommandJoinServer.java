package me.libraryaddict.core.server.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.redis.operations.RedisJoinServerType;

public class CommandJoinServer extends SimpleCommand
{
    private JavaPlugin _plugin;
    private ServerType[] _serverTypes;

    public CommandJoinServer(JavaPlugin plugin)
    {
        super(new String[0], Rank.ALL);

        ArrayList<String> commands = new ArrayList<String>();

        commands.add("join");

        ServerType[] serverType = new ServerType[]
            {
                    ServerType.SearchAndDestroy, ServerType.SurvivalGames, ServerType.Hub, ServerType.Build, ServerType.Disaster
            };

        for (ServerType type : serverType)
        {
            commands.add(type.getName().replaceAll(" ", ""));
            commands.add(type.getShortened().replaceAll(" ", ""));
        }

        setAliases(commands.toArray(new String[0]));

        _plugin = plugin;
        _serverTypes = serverType;
    }

    private ServerType getServer(String alias)
    {
        alias = alias.replaceAll(" ", "");

        for (ServerType serverType : _serverTypes)
        {
            if (!serverType.getName().replaceAll(" ", "").equalsIgnoreCase(alias)
                    && !serverType.getShortened().replaceAll(" ", "").equalsIgnoreCase(alias))
                continue;

            return serverType;
        }

        return null;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (!Recharge.canUse(player, "SwitchServer"))
            return;

        ServerType type = null;

        if (alias.equalsIgnoreCase("join"))
        {
            type = getServer(UtilString.join(args, " "));

            if (type == null && args.length > 0)
            {
                player.sendMessage(C.Red + "Unknown server '" + UtilString.join(args, " ") + "'");
            }

            if (args.length == 0 || type == null)
            {
                /*  FancyMessage message = new FancyMessage(C.Gold + C.Bold + "Servers: ");
                
                for (int i = 0; i < _serverTypes.length; i++)
                {
                    message.then(C.Gold + C.Bold + _serverTypes[i].getName());
                    message.command("/" + _serverTypes[i].getName().replace(" ", ""));
                    message.tooltip(C.Gold + "Join " + _serverTypes[i].getName());
                
                    if (i + 1 < _serverTypes.length)
                    {
                        message.then(C.Yellow + ", ");
                    }
                    else
                    {
                        message.then(C.Yellow + ".");
                    }
                }
                
                message.send(player);*/
                return;
            }
        }
        else
        {
            type = getServer(alias);
        }

        Recharge.use(player, "SwitchServer", 700);

        UUID uuid = player.getUniqueId();
        String server = type.getName();

        new BukkitRunnable()
        {
            public void run()
            {
                new RedisJoinServerType(uuid, server);
            }
        }.runTaskAsynchronously(_plugin);
    }

}
