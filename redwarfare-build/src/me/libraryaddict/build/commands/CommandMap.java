package me.libraryaddict.build.commands;

import me.libraryaddict.build.inventories.MainBuildInventory;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CommandMap extends SimpleCommand {
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public CommandMap(WorldManager worldManager, RankManager rankManager) {
        super(new String[]{"map", "maps"}, Rank.ALL);

        _worldManager = worldManager;
        _rankManager = rankManager;
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
            new MainBuildInventory(player, _worldManager, _rankManager);

            ItemStack openMaps = new ItemBuilder(Material.NETHER_STAR).setTitle(C.Gold + "Open Maps").build();

            if (!UtilInv.contains(player, openMaps)) {
                UtilInv.addItem(player, openMaps);
            }

            return;
        }

        String name = UtilString.join(args, " ");

        if (name.equalsIgnoreCase("lobby") || name.equalsIgnoreCase("hub") || name.equalsIgnoreCase("world")) {
            UtilPlayer.tele(player, _worldManager.getDefaultWorld().getSpawnLocation());
            return;
        }

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

                message.then((worldInfo.hasRank(player, MapRank.BUILDER) ? C.Aqua : C.Red) + worldInfo.getName());

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

        if (!info.isAllowVisitors() && !info.hasRank(player, MapRank.VISITOR) && !rank.hasRank(Rank.BUILDER)) {
            player.sendMessage(C.Red + "You don't have the rights to access that map");
            return;
        }

        _worldManager.loadWorld(player, info);
    }
}
