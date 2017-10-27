package me.libraryaddict.build.commands;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandSkull extends SimpleCommand {
    public CommandSkull() {
        super(new String[]{"head", "skull"}, Rank.ALL);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length != 1) {
            player.sendMessage(C.Red + "/" + alias + " <Player Name>");
            return;
        }

        ItemBuilder builder = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3).setPlayerHead(args[0]);

        UtilInv.addItem(player, builder.build());
    }
}
