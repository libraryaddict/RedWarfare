package me.libraryaddict.build.types;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.build.database.MysqlFetchReviews;
import me.libraryaddict.build.database.MysqlSaveMapInfo;
import me.libraryaddict.build.database.RedisPublishMapInfo;
import me.libraryaddict.build.managers.BuildManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilNumber;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class MapInfo {
    /**
     * Is the world open to visit
     */
    private boolean _allowVisitors = true;
    /**
     * The average rating of the reviewers
     */
    private double _averageReview = 0;
    /**
     * The last version of the map that was backed up
     */
    private String _backedUpVersion;
    /**
     * Location of the last backup
     */
    private String _backup;
    /**
     * How often does it backup
     */
    private int _backupFrequency = 3;
    /**
     * Who can build in this map
     */
    private HashMap<Pair<UUID, String>, MapRank> _builders = new HashMap<Pair<UUID, String>, MapRank>();
    /**
     * When was this map first created
     */
    private Timestamp _created;
    /**
     * Was this map deleted
     */
    private boolean _deleted;
    /**
     * Location of the current file, or null
     */
    private String _fileLocation;
    /**
     * IP of the current file, or null
     */
    private String _ipLocation;
    /**
     * When was the last backup
     */
    private Timestamp _lastBackup = new Timestamp(System.currentTimeMillis());
    /**
     * Is this map being used
     */
    private String[] _locked = new String[2];

    /**
     * Who originally created this map
     */
    private Pair<UUID, String> _mapCreator;
    /**
     * The description of the map, intended as a short intro
     */
    private String _mapDesc = "No desc set";

    /**
     * The name of the map
     */
    private String _mapName;
    /**
     * The game/server type this map is intended for
     */
    private MapType _mapType = MapType.Unknown;
    /**
     * When was this map last modified
     */
    private Timestamp _modified = new Timestamp(System.currentTimeMillis());
    /**
     * Is the map ready to be released
     */
    private boolean _releasable;
    /**
     * How many people reviewed this map
     */
    private int _reviewers;
    /**
     * The unique identifier of this map
     */
    private UUID _uuid;

    public MapInfo(Player player, String mapName) {
        if (player != null)
            _mapCreator = Pair.of(player.getUniqueId(), player.getName());

        _mapName = mapName;
        _uuid = UUID.randomUUID();
        _created = new Timestamp(System.currentTimeMillis());
    }

    public MapInfo(ResultSet rs) throws SQLException {
        _uuid = UUID.fromString(rs.getString("uuid"));
        _mapName = rs.getString("name");
        _mapCreator = Pair.of(UUID.fromString(rs.getString("creator")), rs.getString("playername"));
        _created = rs.getTimestamp("time_created");
        _modified = rs.getTimestamp("last_modified");
        _mapType = MapType.valueOf(KeyMappings.getKey(rs.getInt("map_type")));
        _mapDesc = rs.getString("desc");
        _ipLocation = rs.getString("ip_loc");
        _fileLocation = rs.getString("file_loc");
        _backup = rs.getString("backup_loc");
        _backedUpVersion = rs.getString("backup_version");
        _deleted = rs.getBoolean("deleted");
        _backupFrequency = rs.getInt("backup_freq");
        _lastBackup = rs.getTimestamp("last_backup");
        _allowVisitors = rs.getBoolean("visitors");
        _releasable = rs.getBoolean("releasable");

        try {
            _reviewers = rs.getInt("reviewers");

            if (_reviewers > 0)
                _averageReview = rs.getInt("rating") / (double) getReviewers();
        }
        catch (Exception ex) {
        }
    }

    public MapInfo(String name, Player creator, String description, MapType mapType) {
        _mapName = name;
        _mapCreator = Pair.of(creator.getUniqueId(), creator.getName());
        _mapDesc = description;
        _mapType = mapType;
        _uuid = UUID.randomUUID();
        _created = new Timestamp(System.currentTimeMillis());
    }

    private MapInfo(String backedUpVersion, String backup, int backupFrequency,
            HashMap<Pair<UUID, String>, MapRank> builders, Timestamp created, boolean deleted, String fileLocation,
            String ipLocation, Timestamp lastBackup, Pair<UUID, String> mapCreator, String mapDesc, String mapName,
            MapType mapType, Timestamp modified, UUID uuid, String[] locked, boolean allowVisitors) {
        _backedUpVersion = backedUpVersion;
        _backup = backup;
        _backupFrequency = backupFrequency;
        _builders = builders;
        _created = created;
        _deleted = deleted;
        _fileLocation = fileLocation;
        _ipLocation = ipLocation;
        _lastBackup = lastBackup;
        _mapCreator = mapCreator;
        _mapDesc = mapDesc;
        _mapName = mapName;
        _mapType = mapType;
        _modified = modified;
        _uuid = uuid;
        _locked = locked;
        _allowVisitors = allowVisitors;
    }

    public void addBuilder(Pair<UUID, String> player, MapRank rank) {
        synchronized (this) {
            _builders.put(player, rank);
        }
    }

    public MapInfo clone() {
        synchronized (this) {
            MapInfo mapInfo = new MapInfo(null, null);

            try {
                for (Field field : MapInfo.class.getDeclaredFields()) {
                    if (field.getDeclaringClass() != MapInfo.class || !field.getName().startsWith("_"))
                        continue;

                    try {
                        field.setAccessible(true);

                        Object value = field.get(this);

                        if (value instanceof HashMap)
                            value = ((HashMap) value).clone();
                        else if (value instanceof Pair)
                            value = ((Pair) value).clone();

                        field.set(mapInfo, value);
                    }
                    catch (Exception ex) {
                        UtilError.handle(ex);
                    }
                }
            }
            catch (Exception ex) {
                UtilError.handle(ex);
            }

            return mapInfo;
        }
    }

    public void cloneFrom(MapInfo info) {
        this._backedUpVersion = info._backedUpVersion;
        this._backup = info._backup;
        this._backupFrequency = info._backupFrequency;
        this._builders = info._builders;
        this._deleted = info._deleted;
        this._fileLocation = info._fileLocation;
        this._ipLocation = info._ipLocation;
        this._lastBackup = info._lastBackup;
        this._locked = info._locked;
        this._mapDesc = info._mapDesc;
        this._mapName = info._mapName;
        this._mapType = info._mapType;
        this._modified = info._modified;
        this._mapCreator = info._mapCreator;

        System.out.println("Cloned MapInfo for " + getUUID());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MapInfo && ((MapInfo) obj).getUUID().equals(getUUID());
    }

    public String getBackup() {
        synchronized (this) {
            return _backup;
        }
    }

    public int getBackupFrequency() {
        synchronized (this) {
            return _backupFrequency;
        }
    }

    public String getBackupVersion() {
        synchronized (this) {
            return _backedUpVersion;
        }
    }

    public HashMap<Pair<UUID, String>, MapRank> getBuilders() {
        synchronized (this) {
            return (HashMap<Pair<UUID, String>, MapRank>) _builders.clone();
        }
    }

    private Pair<UUID, String> getCreator() {
        return _mapCreator;
    }

    public String getCreatorName() {
        synchronized (this) {
            return getCreator().getValue();
        }
    }

    public UUID getCreatorUUID() {
        synchronized (this) {
            return getCreator().getKey();
        }
    }

    public String getDescription() {
        synchronized (this) {
            return _mapDesc;
        }
    }

    public String getFileLoc() {
        synchronized (this) {
            return _fileLocation;
        }
    }

    public ItemBuilder getIcon() {
        ItemBuilder builder = new ItemBuilder(getMapType().getIcon());

        builder.setTitle(C.Blue + C.Bold + "Map: " + C.Aqua + getName());
        builder.addLore(C.Blue + C.Bold + "Type: " + C.Aqua + getMapType().getName());
        builder.addLore(C.Blue + C.Bold + "Creator: " + C.Aqua + getCreatorName());

        ArrayList<Pair<String, String>> builders = new ArrayList<Pair<String, String>>();

        for (Entry<Pair<UUID, String>, MapRank> player : _builders.entrySet()) {
            if (player.getValue() == null)
                continue;

            builders.add(Pair.of(player.getKey().getValue(), player.getValue().getColor()));
        }

        Collections.sort(builders, new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> o1, Pair<String, String> o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getKey(), o2.getKey());
            }
        });

        StringBuilder string = new StringBuilder();
        Iterator<Pair<String, String>> itel = builders.iterator();

        while (itel.hasNext()) {
            Pair<String, String> entry = itel.next();

            string.append(entry.getValue() + entry.getKey());

            if (itel.hasNext()) {
                if (string.length() > 150) {
                    string.append(C.Aqua + "...");
                    break;
                }

                string.append(C.Aqua + ", ");
            }
        }

        if (string.length() > 0) {
            builder.addLore(C.Blue + C.Bold + "Builders: " + string.toString());
        }

        builder.addLore(C.Blue + C.Bold + "Description: " + C.Aqua + (getDescription() == null ? "Not found" :
                getDescription()));
        builder.addLore(C.Blue + C.Bold + "Created: " + C.Aqua + UtilNumber
                .getTime(System.currentTimeMillis() - getTimeCreated().getTime(), TimeUnit.MILLISECONDS, 2) + " ago");

        if (!getUUID().equals(BuildManager.getMainHub())) {
            builder.addLore(
                    C.Blue + C.Bold + "Rated " + C.Aqua + C.Bold + ((int) (getRating() * 10D) / 10D) + C.Blue + C
                            .Bold + " by " + C.Aqua + C.Bold + getReviewers() + C.Blue + C.Bold + " players");

            if (isWorldLoaded()) {

                builder.addLore(C.DAqua + C.Bold + "World is being worked on");
            } else {
                builder.addLore(C.Blue + C.Bold + "Last Loaded: " + C.Aqua + UtilNumber
                        .getTime(System.currentTimeMillis() - getTimeModified().getTime(), TimeUnit.MILLISECONDS,
                                2) + " ago");
            }
        }

        return builder;
    }

    public String getIPLoc() {
        synchronized (this) {
            return _ipLocation;
        }
    }

    public Timestamp getLastBackup() {
        return _lastBackup;
    }

    public String getLoadedServer() {
        synchronized (this) {
            return _locked[0];
        }
    }

    public String[] getLocked() {
        synchronized (this) {
            return _locked.clone();
        }
    }

    public MapType getMapType() {
        synchronized (this) {
            return _mapType;
        }
    }

    public String getName() {
        synchronized (this) {
            return _mapName;
        }
    }

    public Pair<UUID, String> getPlayer(String playerName) {
        synchronized (this) {
            for (Pair<UUID, String> key : _builders.keySet()) {
                if (!key.getValue().equalsIgnoreCase(playerName))
                    continue;

                return key;
            }
        }

        return null;
    }

    public Pair<UUID, String> getPlayer(UUID player) {
        synchronized (this) {
            for (Pair<UUID, String> key : _builders.keySet()) {
                if (!key.getValue().equals(player))
                    continue;

                return key;
            }
        }

        return null;
    }

    public MapRank getRank(Pair<UUID, String> player) {
        synchronized (this) {
            return _builders.get(player);
        }
    }

    public MapRank getRank(Player player) {
        return getRank(Pair.of(player.getUniqueId(), player.getName()));
    }

    public double getRating() {
        synchronized (this) {
            return _averageReview;
        }
    }

    public int getReviewers() {
        synchronized (this) {
            return _reviewers;
        }
    }

    public Timestamp getTimeCreated() {
        synchronized (this) {
            return _created;
        }
    }

    public Timestamp getTimeModified() {
        synchronized (this) {
            return _modified;
        }
    }

    public UUID getUUID() {
        synchronized (this) {
            return _uuid;
        }
    }

    public File getZip() {
        synchronized (this) {
            return new File(new File("").getAbsoluteFile().getParentFile(),
                    "Maps Storage/" + getUUID().toString() + ".zip");
        }
    }

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    public boolean hasRank(Pair<UUID, String> player, MapRank rank) {
        synchronized (this) {
            if (isCreator(player))
                return true;

            MapRank hisRank = getRank(player);

            return hisRank != null ? hisRank.has(rank) : false;
        }
    }

    public boolean hasRank(Player player, MapRank rank) {
        return hasRank(Pair.of(player.getUniqueId(), player.getName()), rank);
    }

    public boolean isAllowVisitors() {
        synchronized (this) {
            return _allowVisitors;
        }
    }

    public boolean isCreator(Pair<UUID, String> player) {
        synchronized (this) {
            return getCreatorUUID().equals(player.getKey());
        }
    }

    public boolean isCreator(Player player) {
        synchronized (this) {
            return getCreatorUUID().equals(player.getUniqueId());
        }
    }

    public boolean isDeleted() {
        synchronized (this) {
            return _deleted;
        }
    }

    public boolean isFileInUse() {
        synchronized (this) {
            return _locked[1] != null;
        }
    }

    public boolean isInUse() {
        return isWorldLoaded() || isFileInUse();
    }

    public boolean isReleasable() {
        synchronized (this) {
            return _releasable;
        }
    }

    public boolean isWorldLoaded() {
        synchronized (this) {
            return _locked[0] != null;
        }
    }

    private void publish() {
        synchronized (this) {
            MapInfo info = clone();

            new Thread() {
                public void run() {
                    new RedisPublishMapInfo(info);
                }
            }.start();
        }
    }

    public void recalculateReviews() {
        MysqlFetchReviews fetchReviews = new MysqlFetchReviews(getUUID());

        synchronized (this) {
            _reviewers = fetchReviews.getReviewers();
            _averageReview = fetchReviews.getRating() / (double) fetchReviews.getReviewers();
        }
    }

    public void removeBuilder(Pair<UUID, String> player) {
        synchronized (this) {
            _builders.put(player, null);
        }
    }

    public void save() {
        publish();

        synchronized (this) {
            MapInfo info = clone();

            new Thread() {
                public void run() {
                    new MysqlSaveMapInfo(info);
                }
            }.start();
        }
    }

    public void setAllowVisitors(boolean visitors) {
        synchronized (this) {
            _allowVisitors = visitors;
        }
    }

    public void setBackedUp() {
        synchronized (this) {
            _lastBackup = new Timestamp(System.currentTimeMillis());
        }
    }

    public void setBackedUp(String backupLocation, String version) {
        synchronized (this) {
            _backup = backupLocation;
            _backedUpVersion = version;
        }
    }

    public void setBackupFrequency(int days) {
        synchronized (this) {
            _backupFrequency = days;
        }
    }

    public void setBuilders(ResultSet rs) throws SQLException {
        synchronized (this) {
            rs.beforeFirst();

            while (rs.next()) {
                _builders.put(Pair.of(UUID.fromString(rs.getString("player")), rs.getString("info")),
                        MapRank.valueOf(KeyMappings.getKey(rs.getInt("rank"))));
            }

            if (_builders.containsKey(_mapCreator)) {
                _builders.put(_mapCreator, null);
            }
        }
    }

    public void setCreator(Pair<UUID, String> creator) {
        synchronized (this) {
            _mapCreator = creator;

            _builders.remove(creator);
        }
    }

    public void setDeleted(boolean deleted) {
        _deleted = deleted;
    }

    public void setDescription(String desc) {
        _mapDesc = desc;

        if (_mapDesc != null && _mapDesc.length() > 100)
            _mapDesc = _mapDesc.substring(0, 100);
    }

    public void setFileInUse(String reason) {
        synchronized (this) {
            assert isFileInUse() != (reason != null);

            _locked[1] = reason;

            publish();
        }
    }

    public void setLocation(String ip, String fileLoc) {
        synchronized (this) {
            _ipLocation = ip;
            _fileLocation = fileLoc;
        }
    }

    public void setMapType(MapType mapType) {
        synchronized (this) {
            _mapType = mapType;
        }
    }

    public void setModified() {
        synchronized (this) {
            _modified = new Timestamp(System.currentTimeMillis());
        }
    }

    public void setName(String name) {
        synchronized (this) {
            _mapName = name;
        }
    }

    public void setReleasable(boolean releasable) {
        synchronized (this) {
            _releasable = releasable;
        }
    }

    public void setServerRunning(String owningServer) {
        synchronized (this) {
            assert isWorldLoaded() != (owningServer != null);

            System.out.println(
                    this.toString() + " " + this.hashCode() + " " + getUUID() + " setting loaded to: " + owningServer);

            _locked[0] = owningServer;

            publish();
        }
    }

    @Override
    public String toString() {
        return "MapInfo [_allowVisitors=" + _allowVisitors + ", _backedUpVersion=" + _backedUpVersion + ", _backup="
                + _backup + ", _backupFrequency=" + _backupFrequency + ", _builders=" + _builders + ", _created=" +
                _created + ", _deleted=" + _deleted + ", _fileLocation=" + _fileLocation + ", _ipLocation=" +
                _ipLocation + ", _lastBackup=" + _lastBackup + ", _locked=" + Arrays
                .toString(
                        _locked) + ", _mapCreator=" + _mapCreator + ", _mapDesc=" + _mapDesc + ", _mapName=" +
                _mapName + ", _mapType=" + _mapType + ", _modified=" + _modified + ", _uuid=" + _uuid + ", " +
                "_releasable=" + _releasable + ", _averageReview=" + _averageReview + ", _reviewers=" + _reviewers +
                "]";
    }
}
