package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.AnvilInventory;
import org.bukkit.entity.Player;

public class CreateMapDescriptionInventory extends AnvilInventory {
    private String _generation;
    private String _mapGenerator;
    private String _mapName;
    private WorldManager _worldManager;

    public CreateMapDescriptionInventory(WorldManager worldManager, Player player, String mapName, String mapGenerator,
            String mapGenerationSettings) {
        super(player, "Short Description");

        _worldManager = worldManager;
        _mapName = mapName;
        _mapGenerator = mapGenerator;
        _generation = mapGenerationSettings;
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onSave(String message) {
        if (message.length() < 3 || message.equalsIgnoreCase("Short Description"))
            message = "";

        String finalMessage = message;

        getPlayer().sendMessage(C.Red + "Eh, good enough. You can change this later if you wish");

        closeInventory();

        _worldManager.createWorld(getPlayer(), _mapName, _mapGenerator, _generation, finalMessage);
    }
}
