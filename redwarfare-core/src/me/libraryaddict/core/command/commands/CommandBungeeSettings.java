package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.mysql.operations.MysqlFetchBungeeSettings;
import me.libraryaddict.mysql.operations.MysqlSaveBungeeSettings;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.redis.operations.RedisNotifyBungeeSettings;
import net.md_5.bungee.api.ChatColor;

public class CommandBungeeSettings extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandBungeeSettings(JavaPlugin plugin)
    {
        super(new String[]
            {
                    "bungee", "bungeesettings", "serversettings"
            }, Rank.ADMIN);

        _plugin = plugin;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length != 0)
            return;

        for (String arg : new String[]
            {
                    "footer", "header", "maxplayers", "throttle", "totalplayers", "motd", "addplayer", "removeplayer", "whitelist"
            })
        {
            if (!arg.startsWith(token.toLowerCase()))
                continue;

            completions.add(arg);
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length < 1 || (!args[0].equalsIgnoreCase("protocol") && args.length < 2))
        {
            player.sendMessage(C.Red + "Not enough args");
            player.sendMessage(C.Blue + UtilString.join(new String[]
                {
                        "footer", "header", "maxplayers", "throttle", "totalplayers", "motd", "addplayer", "removeplayer",
                        "whitelist"
                }, ", ") + ".");
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchBungeeSettings fetchSettings = new MysqlFetchBungeeSettings();

                if (!fetchSettings.isSuccess())
                {
                    UtilPlayer.sendMessage(player, UtilError.format("Cannot connect to database"));
                    return;
                }

                BungeeSettings settings = fetchSettings.getSettings();
                String value = ChatColor.translateAlternateColorCodes('&', UtilString.join(1, args, " "));

                switch (args[0].toLowerCase())
                {
                case "footer":

                    settings.setFooter(value);

                    break;
                case "header":

                    settings.setHeader(value);

                    break;
                case "protocol":
                    if (args.length < 2)
                        settings.setProtocol("");
                    else
                        settings.setProtocol(value);

                    break;
                case "motd":
                    settings.setMotd(value);

                    break;
                case "maxplayers":

                    if (!UtilNumber.isParsableInt(value))
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("'" + value + "' cannot be parsed to a number"));
                    }

                    settings.setMaxPlayers(Integer.parseInt(value));

                    break;
                case "totalplayers":

                    if (!UtilNumber.isParsableInt(value))
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("'" + value + "' cannot be parsed to a number"));
                    }

                    settings.setTotalPlayers(Integer.parseInt(value));

                    break;
                case "throttle":

                    if (!UtilNumber.isParsableInt(value))
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("'" + value + "' cannot be parsed to a number"));
                    }

                    settings.setThrottle(Integer.parseInt(value));

                    break;
                case "whitelist":

                    if (!value.equalsIgnoreCase("on") && !value.equalsIgnoreCase("off") && !value.equalsIgnoreCase("true")
                            && !value.equalsIgnoreCase("enable") && !value.equalsIgnoreCase("disable")
                            && !value.equalsIgnoreCase("false"))
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Unrecognized option '" + value + "'"));
                        return;
                    }

                    settings.setWhitelist(
                            value.equalsIgnoreCase("on") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("enable"));
                    break;
                default:

                    UtilPlayer.sendMessage(player, UtilError.format("Cannot recognize the option '" + args[0] + "'"));

                    return;
                }

                new MysqlSaveBungeeSettings(settings);
                new RedisNotifyBungeeSettings(settings);

                UtilPlayer.sendMessage(player, C.Blue + "Set and saved bungee settings");
            }
        }.runTaskAsynchronously(_plugin);

    }

}
