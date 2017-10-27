package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandParse extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandParse(WorldManager worldManager) {
        super(new String[]{"publish", "parse"}, Rank.ADMIN);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {

        WorldInfo info = _worldManager.getWorld(player.getWorld());

        if (info == null) {
            player.sendMessage(C.Red + "You are not in a map");
            return;
        }

        String warning = info.getCustomData().getWarningCode();
        String code = UtilString.join(args, " ");

        if (code.length() > 0) {
            if (warning != null) {
                if (!warning.equalsIgnoreCase(code)) {
                    player.sendMessage(C.Red + "Code incorrect!");
                    return;
                }
            } else {
                player.sendMessage(C.Red + "Code was not asked for! Did you really intend to do this command..?");
                return;
            }
        }

        if (warning != null) {
            player.sendMessage(
                    C.Red + "The border points are not on the same Y level, this means that there is a min and max "
                            + "height set in this world");
            player.sendMessage(C.Red + "As this is not normal, please confirm by using the follow command");
            player.sendMessage(C.Red + "/" + alias + " " + info.getCustomData().getWarningCode());
            return;
        }

        info.attemptParse(player);
    }
}
