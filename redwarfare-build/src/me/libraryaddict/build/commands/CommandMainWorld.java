package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandMainWorld extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandMainWorld(WorldManager worldManager) {
        super(new String[]{"mainworld", "mainhub", "exitworld", "exit", "leave", "leaveworld"}, Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        World world = _worldManager.getDefaultWorld();

        player.teleport(world.getSpawnLocation());
    }
}
