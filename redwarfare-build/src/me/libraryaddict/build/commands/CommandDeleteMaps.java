package me.libraryaddict.build.commands;

import me.libraryaddict.build.inventories.DeletePublishedMapInventory;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class CommandDeleteMaps extends SimpleCommand {
    private JavaPlugin _plugin;
    private RankManager _rankManager;

    public CommandDeleteMaps(JavaPlugin plugin, RankManager rankManager) {
        super("deletemaps", Rank.ADMIN);

        _plugin = plugin;
        _rankManager = rankManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        new DeletePublishedMapInventory(_plugin, _rankManager, player).openInventory();
    }
}
