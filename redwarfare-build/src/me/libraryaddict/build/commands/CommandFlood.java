package me.libraryaddict.build.commands;

import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandFlood extends SimpleCommand {
    public CommandFlood() {
        super("Flood", Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias,
            String[] args) {// TODO Opens inventory where you click on the item you want it to be replaced to, then
        // click on the items to replace.
        // Probably like the block schemes.
    }
}
