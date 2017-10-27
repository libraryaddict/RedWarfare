package me.libraryaddict.build.commands;

import me.libraryaddict.build.inventories.ImportMapInventory;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilString;
import org.apache.commons.validator.routines.UrlValidator;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandImport extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandImport(WorldManager worldManager) {
        super("import", Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length == 0) {
            player.sendMessage(C.Red + "No download link provided");
            return;
        }

        if (_worldManager.getCreatedMaps(player).size() >= _worldManager.getMaxMaps(rank)) {
            player.sendMessage(C.Red + "Delete maps before importing new maps");

            return;
        }

        if (!Recharge.canUse(player, "Import World")) {
            player.sendMessage(C.Red + "You're importing too many worlds! Please wait a bit!");
            return;
        }

        if (!Recharge.canUse(player, "Load World")) {
            player.sendMessage(C.Red + "You're loading too many worlds! Please wait a bit!");
            return;
        }

        String url = UtilString.join(args, " ");

        String[] customSchemes = {"http", "https"};

        UrlValidator customValidator = new UrlValidator(customSchemes);

        if (!customValidator.isValid(url)) {
            player.sendMessage(C.Red + "Url was not a valid url!");
            return;
        }

        new ImportMapInventory(_worldManager, player, url).openInventory();
    }
}
