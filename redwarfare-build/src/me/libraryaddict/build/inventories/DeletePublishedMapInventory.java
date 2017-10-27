package me.libraryaddict.build.inventories;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import me.libraryaddict.build.customdata.GameHubCustomData;
import me.libraryaddict.build.database.MysqlFetchMapInfo;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapType;
import me.libraryaddict.build.types.RemoteFileManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilError;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DeletePublishedMapInventory extends PageInventory {
    private MapType _mapType;
    private JavaPlugin _plugin;
    private RankManager _rankManager;

    public DeletePublishedMapInventory(JavaPlugin plugin, RankManager rankManager, Player player) {
        super(player, "Published Maps");

        _plugin = plugin;
        _rankManager = rankManager;

        buildSelectPage();
    }

    private void buildMapsPage(Vector<LsEntry> files, ArrayList<MapInfo> fetchMaps) {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();
        int i = 0;

        for (LsEntry entry : files) {
            String name = entry.getFilename();

            if (!name.endsWith(".zip"))
                continue;

            name = name.replace(".zip", "");

            ItemBuilder builder = new ItemBuilder(Material.INK_SACK, 1, (short) ((i++) % 15));

            try {
                UUID uuid = UUID.fromString(name);
                MapInfo mapInfo = null;

                for (MapInfo info : fetchMaps) {
                    if (!info.getUUID().equals(uuid))
                        continue;

                    mapInfo = info;
                }

                if (mapInfo == null)
                    throw new Exception("Ex");

                name = mapInfo.getName();

                builder.addLore(C.Blue + "Creator: " + mapInfo.getCreatorName());
                builder.addLore(C.Blue + "Desc: " + mapInfo.getDescription());
            }
            catch (Exception ex) {
                builder.setType(Material.WOOL);
                builder.addLore(C.Red + "Old map!");
            }

            builder.setTitle(C.Blue + "Name: " + name);

            builder.addLore("");
            builder.addLore(C.DAqua + "CLICK TO DELETE");

            String finalName = name;

            items.add(Pair.of(builder.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (!_rankManager.getRank(getPlayer()).hasRank(Rank.ADMIN)) {
                        Thread.dumpStack();

                        getPlayer().sendMessage(C.Red + "How did you get into here...");

                        return true;
                    }

                    if (!Recharge.canUse(getPlayer(), "Delete Map")) {
                        getPlayer().sendMessage(C.Red + "You're deleting maps a bit fast..");
                        return true;
                    }

                    new BukkitRunnable() {
                        public void run() {
                            try {
                                new RemoteFileManager().deleteRemoteFile(null,
                                        "ServerFiles/Maps/" + _mapType.getServerType().getName() + "/" + entry
                                                .getFilename());
                            }
                            catch (Exception e) {
                                UtilError.handle(e);
                            }
                        }
                    }.runTaskAsynchronously(_plugin);

                    files.remove(entry);

                    buildMapsPage(files, fetchMaps);

                    getPlayer().sendMessage(C.Gold + "Deleted map " + finalName);

                    Recharge.use(getPlayer(), "Delete Map", 2000);

                    return true;
                }
            }));
        }

        Collections.sort(items, new Comparator<Pair<ItemStack, IButton>>() {
            @Override
            public int compare(Pair<ItemStack, IButton> o1, Pair<ItemStack, IButton> o2) {
                return o1.getKey().getItemMeta().getDisplayName()
                        .compareToIgnoreCase(o2.getKey().getItemMeta().getDisplayName());
            }
        });

        setPages(items);
    }

    private void buildSelectPage() {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        for (MapType mapType : MapType.values()) {
            if (!mapType.getServerType().isGame() || mapType.getCustomData() == GameHubCustomData.class)
                continue;

            ItemBuilder item = new ItemBuilder(mapType.getIcon());
            item.setTitle(mapType.getServerType().getName());

            items.add(Pair.of(item.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (_mapType != null)
                        return true;

                    _mapType = mapType;

                    rebuildMaps();

                    return true;
                }
            }));
        }

        setPages(items);
    }

    private void rebuildMaps() {
        new BukkitRunnable() {
            public void run() {
                try {
                    Vector<LsEntry> files = new RemoteFileManager()
                            .listFiles(null, "ServerFiles/Maps/" + _mapType.getServerType().getName() + "/");

                    ArrayList<MapInfo> fetchMaps = new MysqlFetchMapInfo(true).getMaps();

                    new BukkitRunnable() {
                        public void run() {
                            buildMapsPage(files, fetchMaps);
                        }
                    }.runTask(_plugin);
                }
                catch (Exception e) {
                    UtilError.handle(e);
                }
            }
        }.runTaskAsynchronously(_plugin);
    }
}
