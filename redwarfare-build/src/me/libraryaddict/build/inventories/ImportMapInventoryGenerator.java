package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.nbt.types.CompoundTag;
import me.libraryaddict.core.nbt.types.NbtIo;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilPlayer;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class ImportMapInventoryGenerator extends BasicInventory {
    private String _download;
    private String _mapName;
    private WorldManager _worldManager;

    public ImportMapInventoryGenerator(WorldManager worldManager, Player player, String download, String name) {
        super(player, "Select Generator", 27);

        _worldManager = worldManager;
        _mapName = name;
        _download = download;

        buildPage();
    }

    private void buildPage() {
        Iterator<Integer> layout = new ItemLayout("XXX XOX XXX", "XXX XXX XXX", "XXO XOX OXX").getSlots().iterator();

        addButton(layout.next(), new ItemBuilder(Material.MAP).setTitle(C.DGreen + "Keep current generation").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        generateMap(null, null);

                        return true;
                    }
                });

        addButton(layout.next(), new ItemBuilder(Material.GLASS).setTitle(C.White + "Void World")
                        .addLore("This is a world that contains nothing but glass to spawn on", "",
                                C.Aqua + C.Bold + "YOU CAN KEEP THE CURRENT GENERATION WITH THE TOP OPTION!").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        generateMap("FLAT", "3;minecraft:air;1;minecraft:air");

                        return true;
                    }
                });

        addButton(layout.next(), new ItemBuilder(Material.GRASS).setTitle(C.White + "Flat World")
                        .addLore("This is a flatlands world", "",
                                C.Aqua + C.Bold + "YOU CAN KEEP THE CURRENT GENERATION WITH THE TOP OPTION!").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        generateMap("FLAT", "3;7,2*3,2;1;");

                        return true;
                    }
                });

        addButton(layout.next(), new ItemBuilder(Material.LOG).setTitle(C.White + "Natural World")
                        .addLore("This generates a natural world", "",
                                C.Aqua + C.Bold + "YOU CAN KEEP THE CURRENT GENERATION WITH THE TOP OPTION!").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        generateMap("NORMAL", "");

                        return true;
                    }
                });
    }

    private void generateMap(String type, String settings) {
        closeInventory();

        if (!Recharge.canUse(getPlayer(), "Import World"))
            return;

        getPlayer().sendMessage(C.Blue + "Please hold as we prepare your map!");

        Recharge.use(getPlayer(), "Import World", 60000);

        new BukkitRunnable() {
            public void run() {
                MapInfo mapInfo = new MapInfo(getPlayer(), _mapName);

                File zipFile = mapInfo.getZip();

                UtilFile.grabDownload(_download, zipFile);

                if (!zipFile.exists()) {
                    UtilPlayer.sendMessage(getPlayer(), C.Red + "Unable to download anything!");
                    UtilPlayer.sendMessage(getPlayer(),
                            C.Red + "If you are having trouble, try dropbox! Get the download link then replace the "
                                    + "=0 at the end with =1");
                    return;
                }

                if (!UtilFile.isZipFile(zipFile)) {
                    zipFile.delete();

                    UtilPlayer.sendMessage(getPlayer(),
                            C.Red + "That link was not a valid zip! You need to provide a direct link to the zip " +
                                    "file!");
                    UtilPlayer.sendMessage(getPlayer(),
                            C.Red + "If you are having trouble, try dropbox! Get the download link then replace the "
                                    + "=0 at the end with =1");
                    return;
                }

                try {
                    ZipFile file = new ZipFile(zipFile);
                    String nestedIn = null;

                    for (FileHeader header : ((ArrayList<FileHeader>) file.getFileHeaders())) {
                        if (header.getFileName().equals("level.dat")) {
                            nestedIn = header.getFileName();
                            break;
                        }

                        if (header.getFileName().endsWith("/level.dat")) {
                            nestedIn = header.getFileName()
                                    .substring(0, header.getFileName().length() - ("level.dat".length()));
                            break;
                        }
                    }

                    if (nestedIn == null) {
                        getPlayer().sendMessage(UtilError.format("Import failed", "Cannot locate level.dat!"));
                        zipFile.delete();
                        return;
                    }

                    if (!nestedIn.equals("level.dat")) {
                        File tempFolder = new File("Temp" + mapInfo.getUUID().toString());
                        tempFolder.mkdir();
                        UtilFile.extractZip(zipFile, tempFolder);

                        UtilFile.createZip(new File(tempFolder, nestedIn), zipFile);

                        UtilFile.delete(tempFolder);
                    }
                }
                catch (Exception ex) {
                    UtilError.handle(ex);
                }

                mapInfo.setLocation(_worldManager.getIP(), zipFile.getAbsolutePath());
                File level = new File(mapInfo.getUUID().toString() + "Level");

                try {
                    UtilFile.extractZipFile(zipFile, "level.dat", level);

                    if (!level.exists()) {
                        getPlayer().sendMessage(C.Red + "level.dat not found!");
                    }

                    CompoundTag root = NbtIo.readCompressed(new FileInputStream(level));

                    CompoundTag data = root.getCompound("Data");

                    if (type != null) {
                        data.putString("generatorName", type);
                        data.putString("generatorOptions", settings);
                    }

                    UtilFile.addZipFile(zipFile, "level.dat", level);

                    level.delete();

                    UtilFile.removeZipFile(zipFile, "uid.dat");
                }
                catch (Exception ex) {
                    level.delete();

                    UtilPlayer.sendMessage(getPlayer(),
                            UtilError.format("Import failed", "Error while examining the world!"));
                    UtilPlayer.sendMessage(getPlayer(), UtilError.format("Import failed",
                            "Please make sure that the region files and level.dat are intact!"));
                    return;
                }

                if (!zipFile.exists()) {
                    UtilPlayer.sendMessage(getPlayer(), UtilError.format("Import failed", "Something weird happened!"));
                    return;
                }

                mapInfo.save();

                new BukkitRunnable() {
                    public void run() {
                        _worldManager.getMaps().add(mapInfo);

                        _worldManager.getChannel().broadcast(
                                C.DGreen + "World> " + C.Aqua + getPlayer().getName() + " imported map " + mapInfo
                                        .getName());

                        _worldManager.loadWorld(getPlayer(), mapInfo);
                    }
                }.runTask(_worldManager.getPlugin());
            }
        }.runTaskAsynchronously(_worldManager.getPlugin());
    }
}
