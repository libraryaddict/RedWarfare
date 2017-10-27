package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.BuildManager;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.build.types.MapType;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Iterator;

public class MainBuildInventory extends BasicInventory {
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public MainBuildInventory(Player player, WorldManager worldManager, RankManager rankManager) {
        super(player, "Build Inventory", 0);

        WorldInfo info = worldManager.getWorld(player.getWorld());

        if (info == null || (info.getData().getUUID().equals(BuildManager.getMainHub()) && !info.isBuilder(player))) {
            setSize(18);
        } else {
            if (info.isAdmin(player)) {
                setSize(45);
            } else {
                setSize(27);
            }
        }

        _worldManager = worldManager;
        _rankManager = rankManager;

        buildPage();
    }

    private void buildPage() {
        WorldInfo worldInfo = _worldManager.getWorld(getPlayer().getWorld());
        WorldInfo info;

        if (worldInfo != null && worldInfo.getData().getUUID().equals(BuildManager.getMainHub()) && !worldInfo
                .isBuilder(getPlayer()))
            info = null;
        else
            info = worldInfo;

        MapInfo mapInfo = info != null ? info.getData() : null;

        Iterator<Integer> layout = new ItemLayout("XXX XOX XXX", "OXO XXX OXO", "XOX XOX XOX", "XXO XXX OXX",
                "XOX XOX XOX").getSlots().iterator();

        if (info != null) {
            addItem(layout.next(), mapInfo.getIcon().build());
        } else {
            layout.next();
        }

        addButton(layout.next(), new ItemBuilder(Material.EMPTY_MAP).setTitle(C.Gold + "Select Map")
                .addLore("Look at all the maps in the build server!").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                new SelectMapInventory(_worldManager, _rankManager, getPlayer(), MainBuildInventory.this)
                        .openInventory();

                return true;
            }
        });

        addButton(layout.next(), new ItemBuilder(Material.RED_ROSE).setTitle(C.DAqua + "Loaded maps")
                .addLore("See which maps are currently being worked on!").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                new LoadedMapInventory(_worldManager, _rankManager, getPlayer()).openInventory();

                return true;
            }
        });

        addItem(layout.next(),
                new ItemBuilder(Material.WRITTEN_BOOK).setTitle(C.Aqua + C.Strike + "Tutorials" + C.Aqua + " Planned!")
                        .addLore("Learn how to manage a map, use worldedit and voxelsniper!").build());

        addButton(layout.next(), new ItemBuilder(Material.ANVIL).setTitle(C.Gold + "Create Map").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        if (_worldManager.getCreatedMaps(getPlayer()).size() >= _worldManager
                                .getMaxMaps(_rankManager.getRank(getPlayer()))) {
                            getPlayer().sendMessage(C.Red + "Delete maps before creating new maps");

                            return true;
                        }

                        new CreateMapInventoryName(_worldManager, getPlayer()).openInventory();

                        return true;
                    }
                });

        if (mapInfo != null && !mapInfo.getUUID().equals(BuildManager.getMainHub())) {
            addButton(layout.next(), new ItemBuilder(Material.NETHER_STAR).setTitle(C.Blue + "Review this map").build(),
                    new IButton() {

                        @Override
                        public boolean onClick(ClickType clickType) {
                            new ReviewMapInventory(getPlayer(), _worldManager, worldInfo).openInventory();

                            return true;
                        }
                    });
        } else {
            layout.next();
        }

        if (mapInfo != null) {
            addButton(layout.next(),
                    new ItemBuilder(Material.ENDER_PEARL).setTitle(C.White + "Go back to build hub").build(),
                    new IButton() {
                        @Override
                        public boolean onClick(ClickType clickType) {
                            _worldManager.sendDefaultWorld(getPlayer());

                            getPlayer().sendMessage(C.Gold + "You have been teleported to the build hub");

                            return true;
                        }
                    });
        } else {
            layout.next();
        }

        if (mapInfo != null && worldInfo.isEditor(getPlayer())) {
            addButton(layout.next(), new ItemBuilder(Material.WOOD_AXE).setTitle(C.DGreen + "Build Tools").build(),
                    new IButton() {
                        @Override
                        public boolean onClick(ClickType clickType) {
                            WorldInfo worldInfo = _worldManager.getWorld(getPlayer().getWorld());

                            if (worldInfo == null) {
                                getPlayer().sendMessage(C.Red + "Please join a map before using this command");
                            } else {
                                new ToolsInventory(getPlayer(), worldInfo.getCustomData()).openInventory();
                            }

                            return true;
                        }
                    });
        } else {
            layout.next();
        }

        if (worldInfo != null && worldInfo.isAdmin(getPlayer())) {
            if (info.isAdmin(getPlayer())) {
                addButton(layout.next(), new ItemBuilder(Material.WOOD_SPADE).setTitle(C.Gold + "Builders").build(),
                        new IButton() {
                            @Override
                            public boolean onClick(ClickType clickType) {
                                if (!worldInfo.isAdmin(getPlayer())) {
                                    getPlayer().sendMessage(C.Red + "You are not an admin in this world");
                                    return true;
                                }

                                new BuildersInventory(_rankManager, getPlayer(), mapInfo).openInventory();

                                return true;
                            }
                        });
            } else {
                layout.next();
            }

            addButton(layout.next(),
                    new ItemBuilder(Material.SKULL_ITEM, 1, (short) (mapInfo.isAllowVisitors() ? 3 : 0)).setTitle(
                            C.Aqua + C.Bold + "Visitors: " + (mapInfo.isAllowVisitors() ? C.DGreen + "Allow" :
                                    C.Red + "Deny")).build(), new IButton() {

                        @Override
                        public boolean onClick(ClickType clickType) {
                            mapInfo.setAllowVisitors(!mapInfo.isAllowVisitors());

                            buildPage();

                            WorldInfo info = _worldManager.getWorld(mapInfo);

                            if (info != null && !mapInfo.isAllowVisitors()) {
                                for (Player player : info.getWorld().getPlayers()) {
                                    if (mapInfo.hasRank(player, MapRank.VISITOR) || _rankManager.getRank(player)
                                            .hasRank(Rank.BUILDER))
                                        continue;

                                    _worldManager.sendDefaultWorld(player);

                                    player.sendMessage(C.Blue + getPlayer().getName() + " has disabled visitors");
                                }
                            }

                            return true;
                        }
                    });

            if (worldInfo.isCreator(getPlayer())) {
                addButton(layout.next(),
                        new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.Red + "Map Options").build(),
                        new IButton() {
                            @Override
                            public boolean onClick(ClickType clickType) {
                                if (!info.isCreator(getPlayer())) {
                                    getPlayer().sendMessage(C.Red + "You don't have the rights to access the options");
                                    return true;
                                }

                                getPlayer().sendMessage(
                                        C.Blue + "Set the options for the map " + C.Aqua + info.getData().getName());

                                new MapOptionInventory(_worldManager, _rankManager, getPlayer(), info.getData(),
                                        MainBuildInventory.this).openInventory();

                                return true;
                            }
                        });
            } else {
                layout.next();
            }

            if (worldInfo.isAdmin(getPlayer())) {
                addItem(layout.next(), new ItemBuilder(Material.COMMAND)
                        .setTitle(C.Aqua + C.Strike + "Test map" + C.Aqua + " Planned!")
                        .addLore("Test your map by planning a game with it!").build());
            } else {
                layout.next();
            }

            if (worldInfo.isCreator(getPlayer()) && mapInfo.getMapType() != MapType.Unknown) {
                addButton(layout.next(), new ItemBuilder(Material.COOKIE).setTitle(
                        C.Blue + C.Bold + "Releasable: " + (mapInfo.isReleasable() ? C.DGreen + C.Bold + "Yes!" :
                                C.DRed + C.Bold + "No!"))
                        .addLore(C.Aqua + "Toggle if the map is ready to be released or not!",
                                C.Gold + "Costs 10 credits!").build(), new IButton() {

                    @Override
                    public boolean onClick(ClickType clickType) {
                        mapInfo.setReleasable(!mapInfo.isReleasable());

                        if (mapInfo.isReleasable()) {
                            Currency.add(getPlayer(), CurrencyType.CREDIT, "Release Map", -10);

                            getPlayer().sendMessage(C.Gold + "10 credits removed");
                        }

                        mapInfo.save();

                        buildPage();

                        return true;
                    }
                });
            } else {
                layout.next();
            }
        }
    }
}
