package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandSetInfo extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandSetInfo(WorldManager worldManager) {
        super(new String[]{"setinfo"}, Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        if (args.length != 0)
            return;

        completions.add("name");
        completions.add("desc");
        completions.add("authors");
        completions.add("author");
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        WorldInfo info = _worldManager.getWorld(player.getWorld());

        if (info == null) {
            player.sendMessage(C.Red + "You are not in a world");
            return;
        }

        if (!info.isAdmin(player)) {
            player.sendMessage(C.Red + "You don't have permission to do that");
            return;
        }

        if (args.length <= 1) {
            player.sendMessage(C.Red + "Incorrect args");
            return;
        }

        String string = UtilString.join(1, args, " ");

        if (string.length() < 4) {
            player.sendMessage(C.Red + "Not enough information");
            return;
        }

        MapInfo data = info.getData();

        if (args[0].equalsIgnoreCase("name")) {
            String invalid = _worldManager.isValidName(string);

            if (invalid != null) {
                player.sendMessage(C.Red + invalid);
                return;
            }

            data.setName(string);

            data.save();

            info.Announce(C.Gold + player.getName() + " has changed the map's name to: " + C.Yellow + string);
        } else if (args[0].equalsIgnoreCase("author") || args[0].equalsIgnoreCase("authors")) {
            info.setAuthors(string);

            info.Announce(C.Gold + player.getName() + " has changed the authors to: " + C.Yellow + string);
        } else if (args[0].equalsIgnoreCase("desc") || args[0].equalsIgnoreCase("description")) {
            info.setDescription(string);

            info.Announce(C.Gold + player.getName() + " has changed the map's description to: " + C.Yellow + string);
        } else {
            player.sendMessage(C.Red + "Unknown argument '" + args[0] + "'");
            return;
        }

        info.getData().save();
    }
}
