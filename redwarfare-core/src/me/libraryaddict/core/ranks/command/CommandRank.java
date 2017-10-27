package me.libraryaddict.core.ranks.command;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankInventory;
import me.libraryaddict.core.ranks.RankManager;

public class CommandRank extends SimpleCommand
{
    private PlayerDataManager _playerDataManager;
    private RankManager _rankManager;

    public CommandRank(RankManager rankManager, PlayerDataManager playerDataManager)
    {
        super("rank", Rank.ALL);

        _rankManager = rankManager;
        _playerDataManager = playerDataManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {

    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (rank.getInfo().isEmpty())
        {
            player.sendMessage(C.Red + "You don't have a rank sorry");
            return;
        }

        new RankInventory(player, _playerDataManager.getData(player), _rankManager);
    }

}
