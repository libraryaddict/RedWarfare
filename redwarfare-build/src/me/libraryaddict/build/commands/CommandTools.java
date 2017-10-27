package me.libraryaddict.build.commands;

import me.libraryaddict.build.inventories.ToolsInventory;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandTools extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandTools(WorldManager worldManager) {
        super(new String[]{"tools", "tool"}, Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        WorldInfo worldInfo = _worldManager.getWorld(player.getWorld());

        if (worldInfo == null) {
            player.sendMessage(C.Red + "Please join a map before using this command");
            return;
        }

        new ToolsInventory(player, worldInfo.getCustomData()).openInventory();
    }
}
