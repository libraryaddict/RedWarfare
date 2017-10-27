package me.libraryaddict.build.inventories;

import me.libraryaddict.core.inventory.AnvilInventory;
import org.bukkit.entity.Player;

public class SearchMapNameInventory extends AnvilInventory {
    private SelectMapInventory _mapInventory;

    public SearchMapNameInventory(Player player, SelectMapInventory mapInventory) {
        super(player, mapInventory.getMapName().isEmpty() ? "Map Name" : mapInventory.getMapName());

        _mapInventory = mapInventory;
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onSave(String message) {
        if (message.trim().equals("Map Name"))
            message = "";

        _mapInventory.setMapName(message.trim());
        _mapInventory.buildPage();

        _mapInventory.openInventory();
    }
}
