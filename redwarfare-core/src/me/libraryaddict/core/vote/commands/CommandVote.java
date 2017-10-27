package me.libraryaddict.core.vote.commands;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.LineFormat;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilText;
import me.libraryaddict.core.vote.VoteManager;
import me.libraryaddict.mysql.operations.MysqlCheckCanVote;

public class CommandVote extends SimpleCommand {
    private VoteManager _voteManager;

    public CommandVote(VoteManager voteManager) {
        super(new String[]{"vote", "voting", "votes"}, Rank.ALL);

        _voteManager = voteManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        UUID uuid = player.getUniqueId();

        new BukkitRunnable() {
            public void run() {
                String[] sites = _voteManager.getVoteSites();
                String[] names = _voteManager.getVoteNames();

                MysqlCheckCanVote checkCanVote = new MysqlCheckCanVote(uuid, names);

                UtilPlayer.sendMessage(player, C.Blue + "Vote at these links to receive tokens");

                UtilPlayer.sendMessage(player, C.Blue + "======================");

                for (int i = 0; i < names.length; i++) {
                    String msg = sites[i];

                    if (checkCanVote.displayedVoted(names[i]))
                        msg += " " + C.DRed + C.Bold + "VOTED";

                    UtilPlayer.sendMessage(player, msg);
                }

                UtilPlayer.sendMessage(player, C.Blue + "======================");
            }
        }.runTaskAsynchronously(getPlugin());
    }
}
