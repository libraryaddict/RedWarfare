package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.build.types.RemoteFileManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class MapOptionInventory extends BasicInventory {
    private MapInfo _mapInfo;
    private BasicInventory _previousInventory;
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public MapOptionInventory(WorldManager worldManager, RankManager rankManager, Player player, MapInfo info,
            BasicInventory previousInventory) {
        super(player, info.getName() + " options", 27);

        _mapInfo = info;
        _worldManager = worldManager;
        _rankManager = rankManager;
        _previousInventory = previousInventory;

        buildPage();
    }

    public void buildPage() {
        Iterator<Integer> layout = new ItemLayout("OXX XXX XXX", "XXX XXX XXX", "OXO XOX OXO").getSlots().iterator();

        addButton(layout.next(), new ItemBuilder(Material.PAPER).setTitle(C.Blue + "Go Back").build(), new IButton() {

            @Override
            public boolean onClick(ClickType clickType) {
                _previousInventory.openInventory();

                return true;
            }
        });

        ItemBuilder backupButton = new ItemBuilder(Material.BEDROCK);

        ArrayList<Integer> days = new ArrayList<Integer>(Arrays.asList(0, 1, 3, 7, 14, 31));

        int day = _mapInfo.getBackupFrequency();

        backupButton
                .setTitle(C.Blue + (day == 0 ? C.Bold : "") + "Backup: " + C.Aqua + (day == 0 ? C.Bold : "") + "Never");

        for (int d = 1; d < days.size(); d++) {
            int a = days.get(d);

            backupButton.addLore(
                    C.Blue + (a == day ? C.Bold : "") + "Backup: " + C.Aqua + (a == day ? C.Bold : "") + (a == 0 ?
                            "Never" : a + " day" + (a == 1 ? "" : "s")));
        }

        backupButton.addLore("", C.Red + C.Bold + "Only the last 15 backups are stored!");

        addButton(layout.next(), backupButton.build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                if (!_mapInfo.isCreator(getPlayer())) {
                    getPlayer().sendMessage(C.Red + "You don't have permission to do this");
                    return true;
                }

                int backupDay = days.indexOf((Integer) day) + 1;
                backupDay = days.get(backupDay % days.size());

                _mapInfo.setBackupFrequency(backupDay);
                _mapInfo.save();

                getPlayer().sendMessage(C.Gold + "Changed map backup frequency to " + (backupDay == 0 ? "never" :
                        backupDay + " day" + (backupDay == 1 ? "" : "s")));

                buildPage();
                return true;
            }
        });

        addButton(layout.next(), new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3).setTitle(
                C.Aqua + C.Bold + "Visitors: " + (_mapInfo.isAllowVisitors() ? C.DGreen + "Allow" : C.Red + "Deny"))
                .build(), new IButton() {

            @Override
            public boolean onClick(ClickType clickType) {
                _mapInfo.setAllowVisitors(!_mapInfo.isAllowVisitors());

                buildPage();

                WorldInfo info = _worldManager.getWorld(_mapInfo);

                if (info != null && !_mapInfo.isAllowVisitors()) {
                    for (Player player : info.getWorld().getPlayers()) {
                        if (_mapInfo.hasRank(player, MapRank.VISITOR) || _rankManager.getRank(player)
                                .hasRank(Rank.BUILDER))
                            continue;

                        _worldManager.sendDefaultWorld(player);

                        player.sendMessage(C.Blue + getPlayer().getName() + " has disabled visitors");
                    }
                }

                return true;
            }
        });

        addButton(layout.next(), new ItemBuilder(Material.EMPTY_MAP).setTitle(C.Purple + "Export map").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        if (!_mapInfo.isCreator(getPlayer()) && !_rankManager.getRank(getPlayer())
                                .hasRank(Rank.ADMIN)) {
                            getPlayer().sendMessage(C.Red + "Trying to take liberties with a map you don't own?");
                            return true;
                        }

                        if (!Recharge.canUse(getPlayer(), "Export Map")) {
                            getPlayer().sendMessage(C.Red + "30 seconds between each map export!");
                            return true;
                        }

                        if (_mapInfo.isFileInUse()) {
                            getPlayer().sendMessage(C.Red + "The world cannot be exported at this time");
                        }

                        WorldInfo worldInfo = _worldManager.getWorld(getPlayer().getWorld());

                        if (worldInfo != null) {
                            worldInfo.Announce(C.Gold + "Saving world for export..");

                            worldInfo.saveMap(_worldManager.getIP());

                            UtilString.log("Saved map for export " + worldInfo.getData().getUUID()
                                    .toString() + " to " + _worldManager.getIP() + " and path" + worldInfo.getData()
                                    .getZip().getAbsolutePath());

                            worldInfo.Announce(C.Gold + "Saved world");
                        }

                        _mapInfo.setFileInUse("Export map");

                        Recharge.use(getPlayer(), "Export Map", 30000);

                        String ip = _worldManager.getIP();
                        String name = getPlayer().getName() + "-" + worldInfo.getData().getName()
                                .replaceAll("[^A-Za-z0-9]", "") + ".zip";
                        String remoteFile = "/home/files/web/download/" + name;

                        new BukkitRunnable() {
                            public void run() {
                                try {
                                    RemoteFileManager fileManager = new RemoteFileManager(ip);

                                    if (!Objects.equals(ip, worldInfo.getData().getIPLoc())) {
                                        fileManager.copyFileToLocal();
                                    }

                                    fileManager.exportMapToSite(worldInfo.getData().getFileLoc(), null, null, null,
                                            remoteFile);
                                }
                                catch (Exception e) {
                                    UtilPlayer.sendMessage(getPlayer(), UtilError.format("Error while exporting map!"));
                                    e.printStackTrace();
                                    return;
                                }

                                UtilPlayer.sendMessage(getPlayer(),
                                        C.Blue + "Download your map at http://www.redwarfare.com/download/" + name);
                                UtilPlayer.sendMessage(getPlayer(), C.Blue + "The link will expire in 60 minutes");
                            }
                        }.runTaskAsynchronously(_worldManager.getPlugin());

                        return true;
                    }
                });

        addButton(layout.next(), new ItemBuilder(Material.COMMAND).setTitle(C.Purple + "Restore backup").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        new RestoreBackupInventory(getPlayer(), _worldManager, _rankManager, _mapInfo).openInventory();

                        return true;
                    }
                });

        addButton(layout.next(), new ItemBuilder(Material.REDSTONE).setTitle(C.DRed + "Delete map").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        new DeleteMapInventory(getPlayer(), _worldManager, _rankManager, _mapInfo, _previousInventory)
                                .openInventory();
                        getPlayer().sendMessage(
                                C.Blue + "Are you sure you wish to delete the map " + C.Aqua + _mapInfo.getName());

                        return true;
                    }
                });
    }
}
