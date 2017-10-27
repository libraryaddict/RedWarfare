package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilNumber;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandTime extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandTime(WorldManager worldManager) {
        super(new String[]{"time", "settime"}, Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        for (String s : new String[]{"sunset", "day", "night", "sunrise", "midnight", "noon"}) {
            if (!s.startsWith(token.toLowerCase())) {
                continue;
            }

            completions.add(s);
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        WorldInfo info = _worldManager.getWorld(player.getWorld());

        if (info == null ? !rank.hasRank(Rank.MAPMAKER) : !info.isEditor(player)) {
            player.sendMessage(C.Red + "You can't change the time in this map!");
            return;
        }

        if (args.length == 0) {
            player.sendMessage(C.Red + "/" + alias + " <Time>");
            return;
        }

        String arg = args[0].toLowerCase();
        World world = player.getWorld();

        if (UtilNumber.isParsableInt(arg)) {
            world.setTime(Integer.parseInt(arg));
        } else if (arg.equals("day") || arg.equals("noon")) {
            world.setTime(6000);
        } else if (arg.equals("night") || arg.equals("midnight")) {
            world.setTime(18000);
        } else if (arg.equals("sunrise")) {
            world.setTime(0);
        } else if (arg.equals("sunset")) {
            world.setTime(11615);
        } else {
            player.sendMessage(C.Red + "Unrecognized time: " + args[0]);
            return;
        }

        info.Announce(C.Gold + player.getName() + " changed the world time to " + world.getTime());
    }
}
