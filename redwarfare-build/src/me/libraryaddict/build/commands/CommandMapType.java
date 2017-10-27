package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapType;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

public class CommandMapType extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandMapType(WorldManager worldManager) {
        super(new String[]{"maptype", "worldtype", "gametype"}, Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        token = token.toLowerCase();

        for (MapType serverType : MapType.values()) {
            if (serverType.name().toLowerCase().startsWith(token))
                completions.add(serverType.name());
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length == 0) {
            ArrayList<String> names = new ArrayList<String>();
            for (MapType serverType : MapType.values()) {
                names.add(serverType.name());
            }

            player.sendMessage(C.Red + "/" + alias + " " + UtilString.join(names, ", "));
            return;
        }

        WorldInfo info = _worldManager.getWorld(player.getWorld());

        if (info == null) {
            player.sendMessage(C.Red + "Please join a map first");
            return;
        }

        if (!info.isAdmin(player)) {
            player.sendMessage(C.Red + "You do not have permission to do that in this world");
            return;
        }

        for (MapType mapType : MapType.values()) {
            if (mapType.name().equalsIgnoreCase(args[0])) {
                info.setMapType(player, mapType);

                return;
            }
        }

        player.sendMessage(C.Red + "Unknown maptype '" + args[0] + "'");
    }
}
