package me.libraryaddict.arcade.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.arcade.managers.LobbyManager;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandPickForcedKits extends SimpleCommand {
    private LobbyManager lobby;

    public CommandPickForcedKits(LobbyManager lobby) {
        super("pick", Rank.ALL);

        this.lobby = lobby;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        lobby.pickKit(player, UtilString.join(args, " "));
    }

}
