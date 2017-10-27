package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Iterator;

public class CreateMapInventoryGenerator extends BasicInventory {
    private String _mapName;
    private WorldManager _worldManager;

    public CreateMapInventoryGenerator(WorldManager worldManager, Player player, String title) {
        super(player, "Select Generator", 9);

        _worldManager = worldManager;
        _mapName = title;

        buildPage();
    }

    private void buildPage() {
        Iterator<Integer> layout = new ItemLayout("XXO XOX OXX").getSlots().iterator();

        addButton(layout.next(), new ItemBuilder(Material.GLASS).setTitle(C.White + "Void World")
                .addLore("This is a world that contains nothing but glass to spawn on").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                new CreateMapDescriptionInventory(_worldManager, getPlayer(), _mapName, "FLAT",
                        "3;minecraft:air;1;minecraft:air").openInventory();

                return true;
            }
        });

        addButton(layout.next(),
                new ItemBuilder(Material.GRASS).setTitle(C.White + "Flat World").addLore("This is a flatlands world")
                        .build(), new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        new CreateMapDescriptionInventory(_worldManager, getPlayer(), _mapName, "FLAT", "3;7,2*3,2;1;")
                                .openInventory();

                        return true;
                    }
                });

        addButton(layout.next(), new ItemBuilder(Material.LOG).setTitle(C.White + "Natural World")
                .addLore("This generates a natural world").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                new CreateMapDescriptionInventory(_worldManager, getPlayer(), _mapName, "NORMAL", "").openInventory();

                return true;
            }
        });
    }
}
