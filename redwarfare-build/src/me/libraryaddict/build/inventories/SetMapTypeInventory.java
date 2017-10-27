package me.libraryaddict.build.inventories;

import me.libraryaddict.build.types.MapType;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class SetMapTypeInventory extends BasicInventory {
    private WorldInfo _info;

    public SetMapTypeInventory(Player player, WorldInfo info) {
        super(player, "Select map type", (int) (Math.ceil(MapType.values().length / 9D) * 9));

        _info = info;

        buildPages();
    }

    private void buildPages() {
        int i = 0;

        for (MapType mapType : MapType.values()) {
            ItemBuilder builder = new ItemBuilder(mapType.getIcon());
            builder.setTitle(C.Blue + "Map Type: " + mapType.getName());
            builder.addLore(C.Aqua + "Click to set map type");

            if (_info.getData().getMapType() == mapType) {
                builder.addDullEnchant();
            }

            addButton(i++, builder.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (!_info.isAdmin(getPlayer())) {
                        getPlayer().sendMessage(C.Red + "You do not have permission to do this");
                    } else {
                        _info.setMapType(getPlayer(), mapType);
                    }

                    closeInventory();

                    return true;
                }
            });
        }
    }
}
