package me.libraryaddict.core.bans;

import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.bans.commands.CommandBan;
import me.libraryaddict.core.bans.commands.CommandBanInfo;
import me.libraryaddict.core.bans.commands.CommandFindAlts;
import me.libraryaddict.core.bans.commands.CommandMute;
import me.libraryaddict.core.bans.commands.CommandUnban;
import me.libraryaddict.core.bans.commands.CommandUnmute;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.plugin.MiniPlugin;

public class BanManager extends MiniPlugin
{
    public BanManager(JavaPlugin plugin, CommandManager command)
    {
        super(plugin, "Ban Manager");

        command.registerCommand(new CommandBan(plugin));
        command.registerCommand(new CommandBanInfo(plugin));
        command.registerCommand(new CommandUnban(plugin));
        command.registerCommand(new CommandMute(plugin));
        command.registerCommand(new CommandUnmute(plugin));
        command.registerCommand(new CommandFindAlts());
    }

}
