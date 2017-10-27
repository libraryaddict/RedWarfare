package me.libraryaddict.build.parsers;

import me.libraryaddict.build.customdata.BorderCustomData;
import me.libraryaddict.build.customdata.GameHubCustomData;
import me.libraryaddict.build.customdata.WorldCustomData;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapType;
import me.libraryaddict.build.types.RemoteFileManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.nbt.ChunkHandle;
import me.libraryaddict.core.nbt.WorldStorage;
import me.libraryaddict.core.nbt.types.CompoundTag;
import me.libraryaddict.core.nbt.types.NbtIo;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilTime;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class BasicParser {
    private YamlConfiguration _config = new YamlConfiguration();
    private int _deletedChunks;
    private File _folder;
    private Iterator<ChunkHandle> _itel;
    private long _lastPrinted = System.currentTimeMillis();
    private int _processedChunks;
    private int _stage;
    private WorldStorage _storage;
    private ArrayList<Pair<Integer, Integer>> _toKeep;
    private int _totalChunks;
    private WorldInfo _worldInfo;
    private File _zip;

    public BasicParser(WorldInfo worldInfo, UUID publisher) {
        _worldInfo = worldInfo;
        _folder = new File("Parser/" + getMapInfo().getUUID());
        _zip = new File("Parser/" + getMapInfo().getUUID().toString() + ".zip");

        _config.set("Publisher", publisher.toString());
        _config.set("Published", UtilTime.parse(System.currentTimeMillis()));
    }

    private void finishUp() {
        getInfo().Announce(C.Gold + "The map has finished parsing. Now saving");

        try {
            _config.save(new File(_folder, "config.yml"));
        }
        catch (IOException ex) {
            UtilError.handle(ex);
        }

        getInfo().Announce(C.Gold + "Creating the final result..");

        UtilFile.delete(_zip);

        UtilFile.createZip(_folder, _zip);
        UtilFile.delete(_folder);

        getInfo().Announce(C.Gold + "Saved parsed map!");
        getInfo().Announce(C.Gold + "Now adding to map rotation..");

        MapType type = getInfo().getData().getMapType();

        String zipFile;

        if (type == MapType.Hub || type.getCustomData() == GameHubCustomData.class) {
            zipFile = "ServerFiles/Hubs/" + type.getServerType().getName() + ".zip";
        } else {
            zipFile = "ServerFiles/Maps/" + type.getServerType().getName() + "/" + getInfo().getData().getUUID()
                    .toString() + ".zip";
        }

        try {
            new RemoteFileManager().putFile(_zip, null, zipFile);
        }
        catch (Exception e) {
            UtilError.handle(e);

            getInfo().Announce(C.Red + "Error while uploading the zip to the main server");
        }

        getInfo().Announce(C.Gold + "Added!");

        _zip.delete();

        _stage++;
    }

    public YamlConfiguration getConfig() {
        return _config;
    }

    public WorldCustomData getData() {
        return getInfo().getCustomData();
    }

    public WorldInfo getInfo() {
        return _worldInfo;
    }

    public MapInfo getMapInfo() {
        return getInfo().getData();
    }

    public World getWorld() {
        return getInfo().getWorld();
    }

    public boolean isFinished() {
        return _stage == 7;
    }

    private void parseMap() {
        if (getMapInfo().getMapType() != MapType.Unknown) {
            getInfo().Announce(C.Gold + "Now parsing the world");

            _stage++;
        } else {
            _stage = 7;
        }
    }

    private void prepareToDeleteChunks() {
        File regions = new File(_folder, "region");

        if (getInfo().getCustomData() instanceof BorderCustomData && regions.exists() && regions.isDirectory()) {
            getInfo().Announce(C.Gold + "Now deleting unused chunks..");

            _storage = WorldStorage.load(regions);

            _toKeep = ((BorderCustomData) getInfo().getCustomData()).getChunksToKeep();

            ArrayList<ChunkHandle> chunks = _storage.getAllChunks();

            _itel = chunks.iterator();
            _totalChunks = chunks.size();

            _stage++;
        } else {
            _stage += 2;
        }
    }

    private void saveConfig() {
        getInfo().Announce(C.Gold + "Saving the config");

        getConfig().set("Name", getInfo().getData().getName());
        getConfig().set("Author", getInfo().getAuthor());
        getConfig().set("Description", getInfo().getDescription());

        getInfo().getCustomData().saveConfig(getConfig());

        HashMap<String, ArrayList<String>> custom = new HashMap<String, ArrayList<String>>();

        saveConfig(custom);

        for (String key : custom.keySet()) {
            getConfig().set("Custom." + key, custom.get(key));
        }

        _stage++;
    }

    public void saveConfig(HashMap<String, ArrayList<String>> hashMap) {
    }

    private void saveWorld() {
        getInfo().Announce(C.Gold + "Saving the world");

        getInfo().saveWorld();

        UtilFile.copyFile(getInfo().getWorldFolder(), _folder);

        _stage++;
    }

    private void startModifying() {
        getInfo().Announce(C.Gold + "Now trimming and modifying the files..");

        // Trim world, remove unwanted thingys
        for (File f : _folder.listFiles()) {
            if (f.getName().equals("level.dat") || f.getName().equals("region")) {
                continue;
            }

            UtilFile.delete(f);
        }

        try {
            File dataFile = new File(_folder, "level.dat");

            CompoundTag root = NbtIo.readCompressed(new FileInputStream(dataFile));

            CompoundTag data = root.getCompound("Data");
            data.putString("generatorName", "flat");
            data.putString("generatorOptions", "3;minecraft:air;1;minecraft:air");
            data.putInt("GameType", 0);

            NbtIo.writeCompressed(root, new FileOutputStream(dataFile));
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        _stage++;
    }

    public void tick() {
        switch (_stage) {
            case 0:
                saveConfig();
                break;
            case 1:
                saveWorld();
                break;
            case 2:
                parseMap();
                break;
            case 3:
                startModifying();
                break;
            case 4:
                prepareToDeleteChunks();
                break;
            case 5:
                tickDelete();
                break;
            case 6:
                finishUp();
                break;
        }
    }

    private void tickDelete() {
        int tick = 0;

        while (tick++ < 5 && _itel.hasNext()) {
            ChunkHandle chunk = _itel.next();
            _processedChunks++;

            if (UtilTime.elasped(_lastPrinted, 4000)) {
                _lastPrinted = System.currentTimeMillis();
                getInfo().Announce(C.Gold + _processedChunks + "/" + _totalChunks + " chunks processed");
            }

            Pair<Integer, Integer> pair = Pair.of(chunk.getChunkX(), chunk.getChunkZ());

            if (_toKeep.contains(pair))
                continue;

            chunk.delete();
            _deletedChunks++;
        }

        if (_itel.hasNext())
            return;

        _stage++;

        _storage.close();

        getInfo().Announce(C.Gold + "Deleted " + _deletedChunks + " chunks");
    }
}
