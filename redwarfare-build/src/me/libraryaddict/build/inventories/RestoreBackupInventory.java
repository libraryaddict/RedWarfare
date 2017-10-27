package me.libraryaddict.build.inventories;

import me.libraryaddict.build.database.MysqlFetchMapBackups;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.RemoteFileManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RestoreBackupInventory extends BasicInventory {
    private ArrayList<Pair<String, Timestamp>> _backups;
    private MapInfo _mapInfo;
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public RestoreBackupInventory(Player player, WorldManager worldManager, RankManager rankManager, MapInfo info) {
        super(player, "Restore Backups");

        _rankManager = rankManager;
        _worldManager = worldManager;
        _mapInfo = info;

        buildLoading();

        new BukkitRunnable() {
            public void run() {
                MysqlFetchMapBackups fetchBackups = new MysqlFetchMapBackups(info);

                if (!fetchBackups.isSuccess())
                    return;

                for (int a = 0; a < 54; a++) {
                    int starter = a;

                    new BukkitRunnable() {
                        public void run() {
                            buildCaution(starter);
                        }
                    }.runTaskLater(worldManager.getPlugin(), a * 3);
                }

                new BukkitRunnable() {
                    public void run() {
                        _backups = fetchBackups.getBackups();

                        buildPage();
                    }
                }.runTaskLater(worldManager.getPlugin(), 165);
            }
        }.runTaskAsynchronously(worldManager.getPlugin());
    }

    public void buildCaution(int starter) {
        addItem(starter, (starter % 2 == 0 ? new ItemBuilder(Material.WOOL, 1, (short) 14) :
                new ItemBuilder(Material.REDSTONE_BLOCK)).setTitle((starter % 2 == 0 ? C.Red : C.DRed) + "CAUTION!")
                .addLore((starter % 2 != 0 ? C.Red : C.DRed) + "THIS WILL DELETE YOUR EXISTING WORLD!").build());
    }

    public void buildLoading() {
        for (int i = 0; i < 54; i++) {
            addItem(i, new ItemBuilder(Material.WOOL).setTitle(C.White + C.Bold + "LOADING").build());
        }
    }

    public void buildPage() {
        clear();

        if (_backups.isEmpty())
            addItem(0, new ItemBuilder(Material.BEDROCK).setTitle(C.White + "No backups found").build());

        int i = 0;

        for (Pair<String, Timestamp> backup : _backups) {
            ItemBuilder builder = new ItemBuilder(Material.EMPTY_MAP);
            builder.setTitle(C.Blue + "Map Backup");
            builder.addLore(C.Blue + "Age: " + C.Aqua + UtilNumber
                    .getTime(System.currentTimeMillis() - backup.getValue().getTime(), TimeUnit.MILLISECONDS, 2));
            builder.addLore("");
            builder.addLore(C.Gold + C.Bold + "Cost: " + C.Yellow + C.Bold + "25 credits");

            addButton(i++, builder.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (!_mapInfo.isCreator(getPlayer()) && !_rankManager.getRank(getPlayer()).hasRank(Rank.ADMIN)) {
                        getPlayer().sendMessage(C.Red + "You do not have permission to do this");
                        return true;
                    }

                    if (Currency.get(getPlayer(), CurrencyType.CREDIT) < 25) {
                        getPlayer().sendMessage(C.Red + "You cannot afford to restore a backup");
                        return true;
                    }

                    if (_mapInfo.isInUse()) {
                        getPlayer().sendMessage(C.Red + "The map cannot be modified while it is running!");
                        return true;
                    }

                    Currency.add(getPlayer(), CurrencyType.CREDIT, "Restored map backup", -25);

                    _mapInfo.setFileInUse("Restoring backup");

                    new BukkitRunnable() {
                        public void run() {
                            RemoteFileManager remoteManager = new RemoteFileManager(_worldManager.getIP(), _mapInfo);

                            try {
                                if (_mapInfo.getIPLoc() != null)
                                    remoteManager.deleteFile();

                                remoteManager.grabBackup(backup.getKey());

                                _mapInfo.save();

                                UtilPlayer.sendMessage(getPlayer(), C.Gold + "Backup has been restored!");

                                _mapInfo.setFileInUse(null);
                            }
                            catch (Exception ex) {
                                UtilError.handle(ex);

                                UtilPlayer.sendMessage(getPlayer(),
                                        UtilError.format("Unknown error while restoring backup"));
                            }
                        }
                    }.runTaskAsynchronously(_worldManager.getPlugin());

                    closeInventory();

                    return true;
                }
            });
        }
    }
}
