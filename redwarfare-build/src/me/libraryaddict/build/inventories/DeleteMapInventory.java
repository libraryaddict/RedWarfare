package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilNumber;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class DeleteMapInventory extends BasicInventory {
    private MapInfo _mapInfo;
    private BasicInventory _previousInventory;
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public DeleteMapInventory(Player player, WorldManager worldManager, RankManager rankManager, MapInfo mapInfo,
            BasicInventory previousInventory) {
        super(player, "Delete map", 27);

        _mapInfo = mapInfo;
        _rankManager = rankManager;
        _worldManager = worldManager;
        _previousInventory = previousInventory;

        for (int i = 15; i >= 0; i--) {
            if (i > 0)
                buildCooldown(i);
            else
                buildPage();
        }
    }

    public void buildCooldown(int i) {
        Iterator<Integer> layout = new ItemLayout("XXX XOX XXX", "XXX XXX XXX", "XXO XXX OXX").getSlots().iterator();

        ItemBuilder builder = new ItemBuilder(Material.EMPTY_MAP);

        builder.setTitle(C.Blue + C.Bold + "Map: " + C.Aqua + _mapInfo.getName());
        builder.addLore(C.Blue + C.Bold + "Type: " + C.Aqua + _mapInfo.getMapType().getName());
        builder.addLore(C.Blue + C.Bold + "Creator: " + C.Aqua + _mapInfo.getCreatorName());
        builder.addLore(C.Blue + C.Bold + "Description: " + C.Aqua + (_mapInfo.getDescription() == null ? "Not found" :
                _mapInfo.getDescription()));
        builder.addLore(C.Blue + C.Bold + "Created: " + C.Aqua + UtilNumber
                .getTime(System.currentTimeMillis() - _mapInfo.getTimeCreated().getTime(),
                        TimeUnit.MILLISECONDS) + " ago");

        addItem(layout.next(), builder.build());
        addItem(layout.next(),
                new ItemBuilder(Material.WOOL, 1, (short) 14).setTitle(C.Red + "Are you sure? (" + i + ")").build());
        addItem(layout.next(),
                new ItemBuilder(Material.WOOL, 1, (short) 14).setTitle(C.Red + "Are you sure? (" + i + ")").build());
    }

    public void buildPage() {
        Iterator<Integer> layout = new ItemLayout("XXX XOX XXX", "XXX XXX XXX", "XXO XXX OXX").getSlots().iterator();

        ItemBuilder builder = new ItemBuilder(Material.EMPTY_MAP);

        builder.setTitle(C.Blue + C.Bold + "Map: " + C.DAqua + _mapInfo.getName());
        builder.addLore(C.Blue + C.Bold + "Type: " + C.Aqua + _mapInfo.getMapType().getName());
        builder.addLore(C.Blue + C.Bold + "Creator: " + C.Aqua + _mapInfo.getCreatorName());
        builder.addLore(C.Blue + C.Bold + "Description: " + C.Aqua + (_mapInfo.getDescription() == null ? "Not found" :
                _mapInfo.getDescription()));
        builder.addLore(C.Blue + C.Bold + "Created: " + UtilNumber
                .getTime(System.currentTimeMillis() - _mapInfo.getTimeCreated().getTime(), TimeUnit.MILLISECONDS, 2));

        addItem(layout.next(), builder.build());

        addButton(layout.next(), new ItemBuilder(Material.REDSTONE).setTitle(C.Red + "Cancel").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                new MapOptionInventory(_worldManager, _rankManager, getPlayer(), _mapInfo, _previousInventory)
                        .openInventory();

                return true;
            }
        });

        addButton(layout.next(), new ItemBuilder(Material.WOOL, 1, (short) 13).setTitle(C.DGreen + "Delete").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        if (_mapInfo.isFileInUse()) {
                            getPlayer().sendMessage(C.Red + "The map's files are in use!");
                            return true;
                        }

                        if (_mapInfo.isWorldLoaded()) {
                            getPlayer().sendMessage(C.Red + "The map is currently loaded!");
                            return true;
                        }

                        _worldManager.deleteMap(getPlayer(), _mapInfo);

                        getPlayer().sendMessage(C.Red + "You have deleted the map " + _mapInfo.getName());

                        closeInventory();

                        return true;
                    }
                });
    }
}
