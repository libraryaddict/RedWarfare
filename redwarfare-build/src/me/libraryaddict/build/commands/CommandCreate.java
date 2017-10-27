package me.libraryaddict.build.commands;

import me.libraryaddict.build.inventories.CreateMapInventoryName;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;

import java.util.Collection;

public class CommandCreate extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandCreate(WorldManager worldManager) {
        super("create", Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        String name = UtilString.join(args, " ");

        if (_worldManager.getCreatedMaps(player).size() >= _worldManager.getMaxMaps(rank)) {
            player.sendMessage(C.Red + "Delete maps before creating new maps");

            return;
        }

        String invalid = _worldManager.isValidName(name);

        if (invalid != null) {
            player.sendMessage(C.Red + invalid);
            return;
        }

        if (name.equalsIgnoreCase("hub") || name.equalsIgnoreCase("lobby") || name.equalsIgnoreCase("world")) {
            player.sendMessage(C.Red + "Those names are unique");
            return;
        }

        new CreateMapInventoryName(_worldManager, player).openInventory();
    }
}
