package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.AnvilInventory;
import org.bukkit.entity.Player;

public class CreateMapInventoryName extends AnvilInventory {
    private WorldManager _worldManager;

    public CreateMapInventoryName(WorldManager worldManager, Player player) {
        super(player, "Map Name");

        _worldManager = worldManager;
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

        new CreateMapInventoryGenerator(_worldManager, getPlayer(), message.trim()).openInventory();
    }
}
