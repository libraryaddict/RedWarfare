package me.libraryaddict.core.referal;

import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.referal.commands.CommandDeleteReferal;
import me.libraryaddict.core.referal.commands.CommandRefer;
import me.libraryaddict.core.referal.commands.CommandRefered;

public class ReferalManager extends MiniPlugin
{
    public ReferalManager(JavaPlugin plugin, CommandManager commandManager)
    {
        super(plugin, "Referal Manager");

        commandManager.registerCommand(new CommandRefer());
        commandManager.registerCommand(new CommandRefered());
        commandManager.registerCommand(new CommandDeleteReferal());
    }

}
