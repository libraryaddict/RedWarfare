package me.libraryaddict.build.inventories;

import me.libraryaddict.core.inventory.AnvilInventory;
import org.bukkit.entity.Player;

public class SearchMapCreatorInventory extends AnvilInventory {
    private SelectMapInventory _mapInventory;

    public SearchMapCreatorInventory(Player player, SelectMapInventory mapInventory) {
        super(player, mapInventory.getCreator().isEmpty() ? "Creator Name" : mapInventory.getCreator());

        _mapInventory = mapInventory;
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onSave(String message) {
        if (message.trim().equals("Creator Name"))
            message = "";

        _mapInventory.setCreator(message.trim());
        _mapInventory.buildPage();

        _mapInventory.openInventory();
    }
}
