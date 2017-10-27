package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;

public class CommandAddWorldFile extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandAddWorldFile(WorldManager worldManager) {
        super("addworldfile", Rank.OWNER);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        File file = new File(UtilString.join(args, " "));

        if (!file.exists()) {
            player.sendMessage(C.Red + "The path '" + file.getAbsolutePath() + "' does not exist!");
            return;
        }

        if (file.isFile() && !file.getName().endsWith(".zip")) {
            player.sendMessage(C.Red + "That is not a zip file!");
            return;
        }

        _worldManager.addWorld(player, file);
    }
}
