package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.AnvilInventory;
import org.bukkit.entity.Player;

public class ImportMapInventory extends AnvilInventory {
    private String _download;
    private WorldManager _worldManager;

    public ImportMapInventory(WorldManager worldManager, Player player, String download) {
        super(player, "Map Name");

        _worldManager = worldManager;
        _download = download;
    }

    @Override
    public void onMessage(String message) {
        String invalid = _worldManager.isValidName(message);

        if (invalid == null) {
            return;
        }

        getPlayer().sendMessage(C.Red + invalid);
    }

    @Override
    public void onSave(String message) {
        String invalid = _worldManager.isValidName(message);

        if (invalid != null) {
            getPlayer().sendMessage(C.Red + invalid);
            return;
        }

        new ImportMapInventoryGenerator(_worldManager, getPlayer(), _download, message.trim()).openInventory();
    }
}