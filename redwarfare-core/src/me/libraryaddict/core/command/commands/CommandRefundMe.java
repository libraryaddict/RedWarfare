package me.libraryaddict.core.command.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.network.Pref;

public class CommandRefundMe extends SimpleCommand {
    public static Pref<Boolean> REFUND = new Pref<Boolean>("Refund.Me", true);

    public CommandRefundMe() {
        super("refundme", Rank.VIP);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        Preference.setPreference(player, REFUND, !Preference.getPreference(player, REFUND));

        FancyMessage message = new FancyMessage(C.Gold + "Refund me: "
                + (Preference.getPreference(player, CommandRefundMe.REFUND) ? C.DGreen + "Yes" : C.DRed + "No"))
                        .command("/refundme");
        message.send(player);
    }

}
