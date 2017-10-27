package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandSaveAll extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandSaveAll(WorldManager worldManager) {
        super("saveall", Rank.OWNER);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        _worldManager.saveWorlds();

        player.sendMessage(C.Gold + "Saved all worlds!");
    }
}
