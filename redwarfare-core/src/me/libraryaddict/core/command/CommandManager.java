package me.libraryaddict.core.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.commands.CommandBungeeSettings;
import me.libraryaddict.core.command.commands.CommandClearInventory;
import me.libraryaddict.core.command.commands.CommandGamemode;
import me.libraryaddict.core.command.commands.CommandGiveItem;
import me.libraryaddict.core.command.commands.CommandKick;
import me.libraryaddict.core.command.commands.CommandRefundMe;
import me.libraryaddict.core.command.commands.CommandStuck;
import me.libraryaddict.core.command.commands.CommandSudo;
import me.libraryaddict.core.command.commands.CommandTeleport;
import me.libraryaddict.core.command.commands.CommandTeleportAll;
import me.libraryaddict.core.command.commands.CommandTeleportHere;
import me.libraryaddict.core.command.commands.CommandTop;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandManager extends MiniPlugin {
    private ArrayList<String> _bypassCommands = new ArrayList<String>();
    private ArrayList<SimpleCommand> _commands = new ArrayList<SimpleCommand>();
    private ProtocolManager _protocolManager;
    private RankManager _rankManager;

    public CommandManager(JavaPlugin plugin) {
        super(plugin, "Command Manager");

        _protocolManager = ProtocolLibrary.getProtocolManager();

        _protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.TAB_COMPLETE) {
            private PacketConstructor _constructor = _protocolManager.createPacketConstructor(PacketType.Play.Server.TAB_COMPLETE,
                    (Object) new String[0]);

            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    event.setCancelled(true);

                    ArrayList<String> returns = onTabComplete(event.getPlayer(), event.getPacket().getStrings().read(0));

                    Collections.sort(returns, String.CASE_INSENSITIVE_ORDER);

                    if (returns.isEmpty())
                        return;

                    String[] arg = returns.toArray(new String[0]);

                    _protocolManager.sendServerPacket(event.getPlayer(), _constructor.createPacket((Object) arg));
                }
                catch (Exception e) {
                    UtilError.handle(e);
                }
            }
        });

        registerCommand(new CommandGamemode());
        registerCommand(new CommandTeleport());
        registerCommand(new CommandTeleportAll());
        registerCommand(new CommandTeleportHere());
        registerCommand(new CommandTop());
        registerCommand(new CommandBungeeSettings(plugin));
        registerCommand(new CommandKick(plugin));
        registerCommand(new CommandGiveItem());
        registerCommand(new CommandStuck());
        registerCommand(new CommandSudo(this));
        registerCommand(new CommandClearInventory());
        registerCommand(new CommandRefundMe());
    }

    public void addBypasses(ArrayList<String> bypasses) {
        _bypassCommands.addAll(bypasses);
    }

    public void addBypasses(String... bypasses) {
        _bypassCommands.addAll(Arrays.asList(bypasses));
    }

    public SimpleCommand getCommand(Class<? extends SimpleCommand> classFile) {
        for (SimpleCommand command : _commands) {
            if (command.getClass().isAssignableFrom(classFile))
                return command;
        }

        return null;
    }

    public SimpleCommand getCommand(String commandAlias) {
        for (SimpleCommand command : _commands) {
            if (command.isAlias(commandAlias))
                return command;
        }

        return null;
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        String alias = command.split(" ")[0].substring(1);

        if (_bypassCommands.contains(alias.toLowerCase())) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        String arg = command.substring(command.contains(" ") ? command.indexOf(" ") : command.length()).trim();
        String[] args = arg.isEmpty() ? new String[0] : arg.split(" ");

        for (SimpleCommand simpleCommand : _commands) {
            if (!simpleCommand.isAlias(alias))
                continue;

            PlayerRank rank = _rankManager.getRank(player);

            if (!simpleCommand.canUse(player, rank)) {
                player.sendMessage(C.DRed + "You do not have permission to use this command");
                return;
            }

            try {
                if (simpleCommand.isAdminCommand())
                    simpleCommand.log(player, args);

                simpleCommand.runCommand(player, rank, alias, args);
            }
            catch (Exception ex) {
                player.sendMessage(UtilError.format("There was an error while running the command"));
                UtilError.handle(ex);
            }

            return;
        }

        player.sendMessage(C.DRed + "Command not found");
    }

    private ArrayList<String> onTabComplete(Player player, String message) {
        String token = message.substring(message.lastIndexOf(" ") + 1);

        ArrayList<String> completions = new ArrayList<String>();

        if (!message.startsWith("/")) {
            for (Player p : UtilPlayer.getPlayers()) {
                if (p.getName().toLowerCase().startsWith(token.toLowerCase())) {
                    completions.add(p.getName());
                }
            }

            return completions;
        }

        String alias = message.split(" ")[0].substring(1);

        PlayerRank rank = _rankManager.getRank(player);

        for (SimpleCommand simpleCommand : _commands) {
            if (!simpleCommand.canUse(player, rank)) {
                continue;
            }

            if (!token.equals(message)) {
                if (!simpleCommand.isAlias(alias)) {
                    continue;
                }

                String arg = message.substring(message.split(" ")[0].length() + 1, message.length() - token.length()).trim();

                String[] args = arg.isEmpty() ? new String[0] : arg.split(" ");

                simpleCommand.onTab(player, rank, args, token, completions);
            }
            else {
                for (String s : simpleCommand.getAliasesStarting(alias)) {
                    completions.add("/" + s);
                }
            }
        }

        return completions;
    }

    public void registerCommand(SimpleCommand command) {
        for (String commandAlias : command.getAliases()) {
            if (getCommand(commandAlias) != null) {
                throw new IllegalArgumentException(
                        "The command '" + commandAlias + "' is already registered to " + getCommand(commandAlias));
            }
        }

        _commands.add(command);
        command.setPlugin(getPlugin());
    }

    public void setRankManager(RankManager rankManager) {
        _rankManager = rankManager;
    }

    public void unregisterCommand(SimpleCommand command) {
        _commands.remove(command);
        command.setPlugin(null);
    }
}
