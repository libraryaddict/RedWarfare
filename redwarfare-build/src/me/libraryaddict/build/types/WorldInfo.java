package me.libraryaddict.build.types;

import me.libraryaddict.build.customdata.WorldCustomData;
import me.libraryaddict.build.events.WorldInfoUnloadEvent;
import me.libraryaddict.build.importers.SGMapImport;
import me.libraryaddict.build.importers.SnDMapImport;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.parsers.BasicParser;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.chat.ChatChannel;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import net.lingala.zip4j.io.ZipInputStream;
import net.minecraft.server.v1_12_R1.ExceptionWorldConflict;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class WorldInfo implements Listener {
    /**
     * Who is credited with creation of this map
     */
    private String _authors = "Unknown";
    private ChatChannel _channel;
    /**
     * Maptype specific data, used by the plugins
     */
    private WorldCustomData _customWorldData;
    /**
     * Description of the map to be displayed to the players
     */
    private String _desc = "Description not set";
    private long _emptySince = System.currentTimeMillis();
    private long _lastSave = System.currentTimeMillis();
    /**
     * The map info this map is signed under
     */
    private MapInfo _mapInfo;
    private boolean _modified;
    /**
     * Parser to use when creating the final result, null if not being parsed
     */
    private BasicParser _parser;
    /**
     * Rank
     */
    private RankManager _rankManager;
    private String _serverName;
    private World _world;
    private File _worldFolder;

    public WorldInfo(MapInfo mapInfo, RankManager rankManager, String serverName) {
        _mapInfo = mapInfo;
        _rankManager = rankManager;
        _serverName = serverName;

        _channel = new ChatChannel(mapInfo.getUUID().toString()) {
            @Override
            public ArrayList<Player> getReceivers() {
                return new ArrayList<Player>(getWorld().getPlayers());
            }

            @Override
            public boolean isCrossServer() {
                return true;
            }

            @Override
            public boolean isValid() {
                return isLoaded();
            }
        };
    }

    public void Announce(String message) {
        for (Player player : getWorld().getPlayers()) {
            player.sendMessage(message);
        }
    }

    public void attemptParse(Player player) {
        if (!isAdmin(player)) {
            player.sendMessage(C.Red + "You don't have permission to parse this map");
            return;
        }

        if (getData().getMapType() == MapType.Unknown) {
            player.sendMessage(C.Red + "Unknown maptype, please set before parsing");
            return;
        }

        if (_parser != null) {
            player.sendMessage(C.Red + "This map is already being parsed");
            return;
        }

        String missing = getCustomData().getMissing();

        if (missing != null) {
            player.sendMessage(UtilError.format(missing));
            return;
        }

        _parser = getCustomData().createParser(player);

        Bukkit.getPluginManager().registerEvents(this, getPlugin());

        if (_parser.isFinished()) {
            Announce(C.Gold + player.getName() + " is parsing the map..");

            onParseFinish();
        } else {
            Announce(C.Gold + player
                    .getName() + " has started parsing this map, do not build until the parse has finished");
        }
    }

    public void createNewWorld(JavaPlugin plugin, String generator, String settings) {
        getData().setServerRunning(_serverName);

        _worldFolder = new File("worlds/" + getData().getUUID().toString());

        UtilFile.delete(getWorldFolder());

        WorldType worldType = WorldType.valueOf(generator);

        WorldCreator creator = new WorldCreator("worlds/" + _worldFolder.getName());
        creator.type(worldType);
        creator.generatorSettings(settings);

        _world = Bukkit.createWorld(creator);
        _world.setSpawnLocation(0, _world.getHighestBlockYAt(0, 0) + 1, 0);

        setupWorld();

        boolean foundBlock = false;

        for (Block block : UtilBlock.getBlocks(new Location(_world, -20, 0, -20), new Location(_world, 20, 10, 20))) {
            if (block.getType() == Material.AIR)
                continue;

            foundBlock = true;
            break;
        }

        if (!foundBlock) {
            for (Block block : UtilBlock.getBlocks(new Location(_world, -2, 0, -2), new Location(_world, 2, 0, 2))) {
                if (block.getType() != Material.AIR)
                    continue;

                block.setType(Material.GLASS);
            }
        }

        loadCustomConfig();
    }

    public String getAuthor() {
        return _authors;
    }

    public ChatChannel getChannel() {
        return _channel;
    }

    public WorldCustomData getCustomData() {
        return _customWorldData;
    }

    public MapInfo getData() {
        return _mapInfo;
    }

    public String getDescription() {
        return _desc;
    }

    public JavaPlugin getPlugin() {
        return _rankManager.getPlugin();
    }

    public World getWorld() {
        return _world;
    }

    public File getWorldFolder() {
        return _worldFolder;
    }

    public boolean isAdmin(Player player) {
        if (_parser != null) {
            player.sendMessage(C.Red + "You cannot modify the map while it is parsing");
            return false;
        }

        return _rankManager.getRank(player).hasRank(Rank.ADMIN) || getData().hasRank(player, MapRank.ADMIN);
    }

    public boolean isBuilder(Player player) {
        if (_parser != null) {
            player.sendMessage(C.Red + "You cannot modify the map while it is parsing");
            return false;
        }

        return _rankManager.getRank(player).hasRank(Rank.BUILDER) || getData().hasRank(player, MapRank.BUILDER);
    }

    public boolean isCreator(Player player) {
        return getData().isCreator(player);
    }

    public boolean isEditor(Player player) {
        if (_parser != null) {
            player.sendMessage(C.Red + "You cannot modify the map while it is parsing");
            return false;
        }

        return _rankManager.getRank(player).hasRank(Rank.BUILDER) || getData().hasRank(player, MapRank.EDITOR);
    }

    public boolean isEmpty() {
        boolean empty = getWorld().getPlayers().isEmpty();

        if (!empty) {
            _emptySince = System.currentTimeMillis();
            return false;
        }

        return UtilTime.elasped(_emptySince, 5000);
    }

    public boolean isLoaded() {
        return _world != null;
    }

    public boolean isModified() {
        return _modified;
    }

    public boolean isParsing() {
        return _parser != null;
    }

    public boolean isSaveReady() {
        return UtilTime.elasped(_lastSave, UtilTime.MINUTE * 30);
    }

    private void loadCustomConfig() {
        HashMap<String, ArrayList<String>> customData = new HashMap<String, ArrayList<String>>();
        YamlConfiguration config;

        try {
            config = YamlConfiguration.loadConfiguration(new File(_worldFolder, "build.yml"));

            ConfigurationSection custom = config.getConfigurationSection("Custom");

            if (custom != null) {
                for (String key : custom.getKeys(false)) {
                    customData.put(key, new ArrayList<String>(custom.getStringList(key)));
                }
            }
        }
        catch (Exception ex) {
            config = new YamlConfiguration();
        }

        if (_customWorldData != null) {
            HandlerList.unregisterAll(getCustomData());
            _customWorldData.unloadData();
        }

        _customWorldData = getData().getMapType().createData(this);

        _customWorldData.loadConfig(customData);
        _customWorldData.loadConfig(config);

        Bukkit.getPluginManager().registerEvents(getCustomData(), getPlugin());
    }

    public void loadData() {
        YamlConfiguration config = null;

        try {
            ZipInputStream stream = UtilFile.readInputFromZip(getData().getZip(), "build.yml");

            if (stream == null) {
                stream = UtilFile.readInputFromZip(getData().getZip(), "config.yml");

                if (stream != null) {
                    config = new SnDMapImport().readConfig(stream);

                    if (config == null) {
                        config = new SGMapImport()
                                .readConfig(UtilFile.readInputFromZip(getData().getZip(), "config.yml"));
                    }
                }
            } else {
                try {
                    config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                }
                catch (Exception ex) {
                }
            }
        }
        catch (Exception ex) {
        }

        if (config == null) {
            System.out.println("No build settings found for " + getData().getUUID().toString());
            return;
        }

        _authors = config.getString("Author", "Unknown Author");
        _desc = config.getString("Description", "Unknown Description");
    }

    public void loadWorld() {
        UtilString.log("Loaded map " + getData().getUUID().toString());

        getData().setServerRunning(_serverName);

        _worldFolder = new File("worlds/" + getData().getUUID().toString());

        UtilFile.extractZip(getData().getZip(), _worldFolder, true);

        new File(_worldFolder, "uid.dat").delete();

        WorldCreator creator = new WorldCreator("worlds/" + _worldFolder.getName());
        creator.type(WorldType.valueOf("FLAT"));
        creator.generatorSettings("3;minecraft:air;1;minecraft:air");

        _world = Bukkit.createWorld(creator);
        setupWorld();

        /* if (!new File(_worldFolder, "build.yml").exists())
        {
            new SnDMapImport().parse(getWorldFolder(), getWorld());
            new SGMapImport().parse(getWorldFolder(), getWorld());
        }*/

        loadCustomConfig();
        loadData();
    }

    public void onParseFinish() {
        HandlerList.unregisterAll(this);

        _parser = null;
    }

    @EventHandler
    public void onWorldUnload(WorldInfoUnloadEvent event) {
        if (event.getWorldInfo() != this)
            return;

        event.setCancelled(true);
    }

    public void removeBuilder(String string) {
        getData().removeBuilder(getData().getPlayer(string));
    }

    public void saveData() {
        File file = new File(_worldFolder, "build.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException e) {
                UtilError.handle(e);
            }
        }

        YamlConfiguration config = new YamlConfiguration();

        config.set("Author", getAuthor());
        config.set("Description", getDescription());

        ConfigurationSection custom = config.createSection("Custom");

        HashMap<String, ArrayList<String>> customData = new HashMap<String, ArrayList<String>>();

        getCustomData().saveConfig(customData);
        getCustomData().saveConfig(config);

        for (String key : customData.keySet()) {
            custom.set(key, customData.get(key));
        }

        try {
            config.save(file);
        }
        catch (IOException e) {
            UtilError.handle(e);
        }
    }

    public void saveMap(String ip) {
        saveData();

        saveWorld();

        getData().setFileInUse("Saving map");

        String hash = getData().getZip().exists() ? UtilFile.getSha(getData().getZip()) : getData().getBackupVersion();

        File tempWorldFolder = new File("TempWorld");

        UtilFile.delete(tempWorldFolder);

        UtilFile.copyFile(_worldFolder, tempWorldFolder);

        UtilFile.delete(new File(tempWorldFolder, "playerdata"));
        UtilFile.delete(new File(tempWorldFolder, "uid.dat"));

        File tempWorldZip = new File("TempWorld.zip");

        if (tempWorldZip.exists())
            tempWorldZip.delete();

        UtilFile.createZip(tempWorldFolder, tempWorldZip);

        UtilFile.delete(tempWorldFolder);

        UtilFile.moveFile(tempWorldZip, getData().getZip());

        String hash2 = UtilFile.getSha(getData().getZip());

        if (!Objects.equals(hash, hash2)) {
            getData().setModified();
        }

        getData().setLocation(ip, getData().getZip().getAbsolutePath());
        getData().save();

        getData().setFileInUse(null);
    }

    public void saveWorld() {
        WorldServer world = ((CraftWorld) getWorld()).getHandle();

        boolean savingDisabled = world.savingDisabled;
        world.savingDisabled = false;

        try {
            world.save(true, null);
        }
        catch (ExceptionWorldConflict e) {
            e.printStackTrace();
        }

        world.flushSave();

        world.savingDisabled = savingDisabled;
    }

    public void setAccessLevel(Player player, MapRank rank) {
        getData().addBuilder(Pair.of(player.getUniqueId(), player.getName()), rank);
    }

    public void setAuthors(String string) {
        _authors = string;
    }

    public void setDescription(String desc) {
        _desc = desc;

        getData().setDescription(desc);
    }

    public void setMapType(Player player, MapType mapType) {
        saveData();

        getCustomData().unloadData();

        Announce(C.Gold + player.getName() + " has changed the map type to " + mapType.name());

        getData().setMapType(mapType);

        loadCustomConfig();
    }

    private void setupWorld() {
        _world.setGameRuleValue("doDaylightCycle", "false");
        _world.setGameRuleValue("randomTickSpeed", "false");
        _world.setStorm(false);
        _world.setTime(6000);
    }

    @EventHandler
    public void tickParse(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        if (_parser == null)
            return;

        _parser.tick();

        if (!_parser.isFinished())
            return;

        onParseFinish();
    }

    public boolean unloadWorld(WorldManager worldManager, String ip, boolean force) {
        WorldInfoUnloadEvent unloadEvent = new WorldInfoUnloadEvent(this, force);

        Bukkit.getPluginManager().callEvent(unloadEvent);

        if (unloadEvent.isCancelled())
            return false;

        getData().setFileInUse("Unload world");

        HandlerList.unregisterAll(getCustomData());

        for (Player player : _world.getPlayers()) {
            worldManager.sendDefaultWorld(player);
        }

        saveMap(ip);

        getCustomData().unloadData();

        Bukkit.unloadWorld(_world, false);

        _world = null;

        getData().setServerRunning(null);

        worldManager.getChatManager().removeChannel(getChannel());

        UtilString
                .log("Unloaded map " + getData().getUUID().toString() + " to " + ip + " and path " + getData().getZip()
                        .getAbsolutePath());

        return true;
    }
}
