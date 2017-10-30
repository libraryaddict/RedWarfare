package me.libraryaddict.core.player.commands;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilNumber;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Created by libraryaddict on 30/10/2017.
 */
public class CommandGiveCredits extends SimpleCommand {
    public CommandGiveCredits() {
        super(new String[]{"givecredits"}, Rank.ADMIN);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length == 0) {
            player.sendMessage(C.Gold + "/givecredits <Player> <Credits>");
            return;
        }

        Player p = player;

        if (args.length == 2) {
            p = Bukkit.getPlayer(args[0]);

            if (p == null) {
                player.sendMessage(C.Red + "Cannot find the player '" + args[0] + "'");
                return;
            }
        }

        if (!UtilNumber.isParsableInt(args[args.length == 1 ? 0 : 1])) {
            player.sendMessage(C.Red + "'" + args[args.length == 1 ? 0 : 1] + "' is not a number");
            return;
        }

        Currency.add(p.getUniqueId(), Currency.CurrencyType.CREDIT, "Added by command",
                Long.parseLong(args[args.length == 1 ? 0 : 1]), true);
    }
}
