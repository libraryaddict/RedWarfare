package me.libraryaddict.build.commands;

import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandSpawn extends SimpleCommand {
    public CommandSpawn() {
        super("spawn", Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        player.teleport(player.getWorld().getSpawnLocation());
    }
}
