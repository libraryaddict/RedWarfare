package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CommandDelete extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandDelete(WorldManager worldManager) {
        super("delete", Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        for (MapInfo info : _worldManager.getMaps()) {
            if (!info.getName().toLowerCase().startsWith(token.toLowerCase())) {
                continue;
            }

            completions.add(info.getName());
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length == 0) {
            player.sendMessage(C.Red + "/delete <Map Name>");
            return;
        }

        String name = UtilString.join(args, " ");

        MapInfo info = null;

        ArrayList<MapInfo> maps = _worldManager.getMaps(name);

        if (maps.size() == 1) {
            info = maps.get(0);
        } else if (maps.size() > 1) {
            player.sendMessage(C.Blue + "Multiple maps found, click on the right one!");

            FancyMessage message = new FancyMessage(C.Blue + "Maps: ");

            Iterator<MapInfo> itel = maps.iterator();

            while (itel.hasNext()) {
                MapInfo worldInfo = itel.next();

                message.then((worldInfo.isCreator(player) ? C.Aqua : C.Red) + worldInfo.getName());

                String[] tooltip = new String[]{C.DGreen + "Map: " + C.Green + worldInfo.getName(),
                        C.DGreen + "Creator: " + C.Green + worldInfo.getCreatorName(),
                        C.DGreen + "Description: " + C.Green + worldInfo.getDescription()};
                message.tooltip(tooltip);

                message.command("/map " + worldInfo.getUUID().toString());

                if (itel.hasNext()) {
                    message.then(C.Blue + ", ");
                }
            }

            message.then(C.Blue + ".");
            message.send(player);

            return;
        }

        if (info == null) {
            player.sendMessage(C.Red + "Map '" + name + "' not found");
            return;
        }

        if (!info.isCreator(player)) {
            player.sendMessage(C.Red + "You don't have the rights to delete that map");
            return;
        }

        _worldManager.deleteMap(player, info);
    }
}
