package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilItem;
import me.libraryaddict.core.utils.UtilNumber;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandSetBlock extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandSetBlock(WorldManager worldManager) {
        super("setblock", Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        if (args.length != 3)
            return;

        completions.addAll(UtilItem.getCompletions(token, true));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length != 4) {
            player.sendMessage(C.Red + "Invalid number of parameters!");
            player.sendMessage(C.Red + "/" + alias + " X Y Z <Material>");

            return;
        }

        WorldInfo info = _worldManager.getWorld(player.getWorld());

        if (info == null && !rank.hasRank(Rank.MAPMAKER)) {
            player.sendMessage(C.Red + "Join a world!");
            return;
        }

        if (info != null && !info.isEditor(player)) {
            player.sendMessage(C.Red + "You can't build in this map!");
            return;
        }

        Location b = player.getLocation();

        for (int i = 0; i < 3; i++) {
            String arg = args[i];

            int loc = 0;

            if (arg.startsWith("~")) {
                switch (i) {
                    case 0:
                        loc = b.getBlockX();
                        break;
                    case 1:
                        loc = b.getBlockY();
                        break;
                    case 2:
                        loc = b.getBlockZ();
                        break;
                    default:
                        break;
                }

                arg = arg.substring(1);
            }

            if (arg.length() > 0) {
                if (!UtilNumber.isParsableInt(arg)) {
                    player.sendMessage(C.Red + "Cannot parse " + args[i] + " to a number!");
                    return;
                }

                loc += Integer.parseInt(arg);
            }

            switch (i) {
                case 0:
                    b.setX(loc);
                    break;
                case 1:
                    b.setY(loc);
                    break;
                case 2:
                    b.setZ(loc);
                    break;
                default:
                    break;
            }
        }

        Pair<Material, Short> item = UtilItem.getItem(args[3]);
        int dura = -1;

        if (item == null && args[3].contains(":")) {
            String[] split = args[3].split(":");

            item = UtilItem.getItem(split[0]);

            if (item == null) {
                player.sendMessage(C.Red + "Unable to find the item " + split[0]);
                return;
            }

            if (split.length != 2 || (!UtilNumber.isParsableInt(split[1]) && UtilItem
                    .getDura(item.getKey(), split[1]) == null)) {
                player.sendMessage(C.Red + "Unable to parse " + split[1]);
                return;
            }

            if (UtilNumber.isParsableInt(split[1])) {
                dura = Integer.parseInt(split[1]);
            } else {
                dura = UtilItem.getDura(item.getKey(), split[1]);
            }
        }

        if (item == null) {
            player.sendMessage(C.Red + "Unable to find the block " + args[3]);
            return;
        }

        if (b.getY() < 0 || b.getY() > 256) {
            player.sendMessage(C.Red + "Cannot set a block in the void!");
            return;
        }

        b.getBlock().setTypeIdAndData(item.getKey().getId(), (byte) (dura == -1 ? item.getValue() : dura), false);

        player.sendMessage(C.Gold + "Placed " + UtilItem
                .getName(item.getKey(), (byte) (dura == -1 ? item.getValue() : dura)) + " at " + b
                .getBlockX() + ", " + b.getBlockY() + ", " + b.getBlockZ() + ".");
    }
}
