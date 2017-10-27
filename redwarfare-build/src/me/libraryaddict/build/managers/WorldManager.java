package me.libraryaddict.build.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.mojang.authlib.GameProfile;
import me.libraryaddict.build.database.*;
import me.libraryaddict.build.database.listeners.*;
import me.libraryaddict.build.types.*;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.censor.CensorManager;
import me.libraryaddict.core.chat.ChatChannel;
import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.chat.ChatManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.scoreboard.ScoreboardManager;
import me.libraryaddict.core.server.MergeEvent;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import me.libraryaddict.redis.operations.RedisSendMessage;
import me.libraryaddict.redis.operations.RedisSwitchServer;
import net.lingala.zip4j.io.ZipInputStream;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class WorldManager extends MiniPlugin {
    private ArrayList<MapInfo> _allMaps = new ArrayList<MapInfo>();
    private ChatChannel _buildChat;
    private HashMap<UUID, HashSet<BuildInfo>> _buildOffers = new HashMap<UUID, HashSet<BuildInfo>>();
    private CensorManager _censor;
    private ChatManager _chatManager;
    private HashMap<UUID, HashMap<UUID, GameProfile>> _fakePlayers = new HashMap<UUID, HashMap<UUID, GameProfile>>();
    private HashMap<UUID, ArrayList<UUID>> _fetching = new HashMap<UUID, ArrayList<UUID>>();
    private RankManager _rankManager;
    private ScoreboardManager _scoreboardManager;
    private ArrayList<Pair<Pair<UUID, UUID>, Long>> _sendToMap = new ArrayList<Pair<Pair<UUID, UUID>, Long>>();
    private ServerManager _serverManager;
    private HashMap<UUID, WorldInfo> _worlds = new HashMap<UUID, WorldInfo>();
    private BuildManager _buildManager;

    public WorldManager(JavaPlugin plugin, BuildManager buildManager, RankManager rankManager,
            ServerManager serverManager, CensorManager censorManager, ChatManager chatManager,
            ScoreboardManager scoreboardManager) {
        super(plugin, "World Manager");

        File file = new File(new File("").getAbsoluteFile().getParentFile(), "Maps Storage/");
        file.mkdirs();

        _buildManager = buildManager;
        _rankManager = rankManager;
        _serverManager = serverManager;
        _chatManager = chatManager;
        _scoreboardManager = scoreboardManager;

        _chatManager.addChannel(_buildChat = new ChatChannel("Build Chat") {
            @Override
            public ArrayList<Player> getReceivers() {
                return UtilPlayer.getPlayers();
            }

            @Override
            public boolean isCrossServer() {
                return true;
            }

            @Override
            public boolean isValid() {
                return true;
            }
        });

        MysqlFetchMapInfo fetchMaps = new MysqlFetchMapInfo();
        _allMaps = fetchMaps.getMaps();
        _censor = censorManager;

        new RedisListenerMapInfo(this, getPlugin());
        new RedisListenerAnnounceMapInfo(this, getPlugin());
        new RedisListenerServerRequest(this, getPlugin());
        new RedisListenerJoinMap(this, getPlugin());
        new RedisListenerGameProfile(this);

        new BukkitRunnable() {
            public void run() {
                try (Jedis redis = RedisManager.getRedis()) {
                    redis.publish(RedisKey.NOTIFY_REQUEST_MAP_INFO.getKey(), ServerManager.getServerName());
                }
                catch (Exception ex) {
                    UtilError.handle(ex);
                }
            }
        }.runTaskLater(getPlugin(), 1);

        new BukkitRunnable() {
            public void run() {
                // Attempt to create main world if not found
                getDefaultWorldInfo();
            }
        }.runTaskLater(getPlugin(), 6);
    }

    public void addOffer(BuildInfo info) {
        // You have received an offer.
        // If the server sent this build offer
        // The stuff the server handles is "Server Exists", "Here's an offer"
        // Otherwise the server handles "Can make offer", "Yes we were doing this"

        if (info.isCreateServer()) {
            if (Objects.equals(info.getOffer(), ServerManager.getServerName())) {
                loadMap(info);
            }

            return;
        }

        if (_buildOffers.containsKey(info.getUUID())) {
            if (info.isServerExists()) {
                System.out.println("There is already a server running " + info.getMap());

                _buildOffers.remove(info.getUUID());
                return;
            } else if (info.getOffer() != null) {
                System.out.println("Added build offer for " + info.getMap());

                _buildOffers.get(info.getUUID()).add(info);
                return;
            } else {
                System.out.println("Added new offer for " + info.getMap());

                info.setOffer(ServerManager.getServerName(), _worlds.size());
            }
        } else {
            if (info.getOffer() != null)
                return;

            if (info.isServerExists())
                return;

            if (_fetching.containsKey(info.getMap())) {
                if (info.hasInvoker()) {
                    _fetching.get(info.getMap()).add(info.getInvoker().getKey());
                }

                info.setHasServer(true);
            }

            info.setOffer(ServerManager.getServerName(), _worlds.size());
        }

        new BukkitRunnable() {
            public void run() {
                new RedisPublishBuildStatus(info);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public void addWorld(Player player, File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addWorld(player, f);
            }

            return;
        }

        MapInfo info;
        ZipInputStream stream = UtilFile.readInputFromZip(file, "build.yml");

        if (stream != null) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));

                String mapType = config.getString("MapType");

                if (mapType.equals("GameHub")) {
                    mapType = MapType.SearchAndDestroyHub.name();
                }

                info = new MapInfo(config.getString("Name"), player, "Imported Map", MapType.valueOf(mapType));
            }
            catch (Exception ex) {
                info = new MapInfo(player, file.getName().substring(0, file.getName().lastIndexOf(".")));
            }
        } else {
            info = new MapInfo(player, file.getName().substring(0, file.getName().lastIndexOf(".")));
        }

        info.setLocation(_serverManager.getIP(), file.getAbsolutePath());

        _allMaps.add(info);

        info.save();

        player.sendMessage(C.Red + "Added new world " + info.getName());
    }

    public void createWorld(BuildInfo buildInfo) {
        MapInfo mapInfo = getMap(buildInfo.getMap());

        if (mapInfo == null)
            return;

        WorldInfo info = new WorldInfo(mapInfo, _rankManager, ServerManager.getServerName());

        if (buildInfo.getDesc() != null) {
            System.out.println("Creating new world " + mapInfo.getUUID());

            info.setDescription(buildInfo.getDesc());
            info.createNewWorld(getPlugin(), buildInfo.getMapType(), buildInfo.getMapSettings());

            _buildChat.broadcast(
                    C.DGreen + "World> " + C.Aqua + info.getData().getCreatorName() + " created map " + info.getData()
                            .getName());
        } else {
            System.out.println("Loading world " + mapInfo.getUUID());

            if (!mapInfo.getZip().exists()) {
                if (buildInfo.hasInvoker()) {
                    new BukkitRunnable() {
                        public void run() {
                            new RedisSendMessage(buildInfo.getInvoker().getKey(),
                                    C.Red + "The zip file has gone missing! Restore one of your backups if you have "
                                            + "one!");
                        }
                    }.runTaskAsynchronously(getPlugin());
                }

                System.err.println("Couldn't find the file for " + mapInfo.getUUID() + "!");

                return;
            }

            info.loadWorld();

            new BukkitRunnable() {
                public void run() {
                    if (!info.isLoaded())
                        return;

                    List<Entity> entities = info.getWorld().getEntities();

                    if (entities.size() > 500) {
                        info.Announce(C.Gold + "Clearing entities..");

                        for (Entity ent : entities) {
                            ent.remove();
                        }

                        info.Announce(C.Gold + "Cleared!");
                    }
                }
            }.runTaskLater(getPlugin(), 200);

            String name;

            if (buildInfo.hasInvoker()) {
                name = buildInfo.getInvoker().getValue();
            } else {
                name = "Server";
            }

            _buildChat.broadcast(C.DGreen + "World> " + C.Aqua + name + " has loaded map " + info.getData().getName());
        }

        _worlds.put(mapInfo.getUUID(), info);

        _chatManager.addChannel(info.getChannel());

        if (_fetching.containsKey(buildInfo.getMap())) {
            ArrayList<UUID> players = _fetching.remove(buildInfo.getMap());

            Iterator<UUID> itel = players.iterator();

            while (itel.hasNext()) {
                UUID uuid = itel.next();

                Player player = Bukkit.getPlayer(uuid);

                if (player == null)
                    continue;

                itel.remove();

                player.teleport(info.getWorld().getSpawnLocation());
            }

            if (!players.isEmpty()) {
                new BukkitRunnable() {
                    public void run() {
                        for (UUID player : players) {
                            new RedisJoinMap(player, mapInfo.getUUID());
                            new RedisSwitchServer(player, ServerManager.getServerName());
                        }
                    }
                }.runTaskAsynchronously(getPlugin());
            }
        }
    }

    public void createWorld(Player player, String mapName, String generator, String generationSettings, String desc) {
        MapInfo mapInfo = new MapInfo(player, mapName);

        getMaps().add(mapInfo);

        mapInfo.save();

        BuildInfo buildInfo = new BuildInfo(player, mapInfo.getUUID(), desc, generator, generationSettings);

        loadWorld(player, buildInfo);
    }

    public void deleteMap(Player player, MapInfo info) {
        if (info.isInUse()) {
            player.sendMessage(C.Red + "The map cannot be deleted at this time, is it in use?");
            return;
        }

        info.setDeleted(true);

        info.save();

        _allMaps.remove(info);

        getChannel().broadcast(C.DGreen + "World> " + C.Aqua + player.getName() + " deleted map " + info.getName());
    }

    public void forceSaveWorlds() {
        for (WorldInfo world : _worlds.values()) {
            int a = 0;

            while (world.getData().isFileInUse() && a++ < 100) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            world.Announce(C.Gold + "Saving world..");

            world.saveMap(_serverManager.getIP());

            UtilString.log("Saved map " + world.getData().getUUID().toString() + " to " + _serverManager
                    .getIP() + " and path" + world.getData().getZip().getAbsolutePath());

            world.Announce(C.Gold + "Saved world");
        }
    }

    public void forceUnloadWorlds() {
        Iterator<Entry<UUID, WorldInfo>> itel = _worlds.entrySet().iterator();

        while (itel.hasNext()) {
            Entry<UUID, WorldInfo> entry = itel.next();

            WorldInfo info = entry.getValue();

            info.unloadWorld(this, _serverManager.getIP(), true);

            itel.remove();
        }
    }

    public ChatChannel getChannel() {
        return _buildChat;
    }

    public ChatManager getChatManager() {
        return _chatManager;
    }

    public ArrayList<MapInfo> getCreatedMaps(Player owner) {
        ArrayList<MapInfo> maps = new ArrayList<MapInfo>();

        for (MapInfo info : _allMaps) {
            if (!info.getCreatorUUID().equals(owner.getUniqueId()))
                continue;

            maps.add(info);
        }

        return maps;
    }

    public World getDefaultWorld() {
        WorldInfo defaultWorld = getDefaultWorldInfo();

        if (defaultWorld != null)
            return defaultWorld.getWorld();

        return Bukkit.getWorld("world");
    }

    public WorldInfo getDefaultWorldInfo() {
        MapInfo info = getMap(BuildManager.getMainHub());

        if (!info.isWorldLoaded() && getWorld(info) == null) {
            loadWorld(null, info);
        }

        WorldInfo worldInfo = getWorld(info);

        return worldInfo;
    }

    public HashMap<UUID, HashMap<UUID, GameProfile>> getFakePlayers() {
        return _fakePlayers;
    }

    public String getIP() {
        return _serverManager.getIP();
    }

    public HashMap<UUID, WorldInfo> getLoadedWorlds() {
        return _worlds;
    }

    public MapInfo getMap(UUID uuid) {
        for (MapInfo info : getMaps()) {
            if (!info.getUUID().equals(uuid))
                continue;

            return info;
        }

        System.out.println("Can't find " + uuid);
        return null;
    }

    public ArrayList<MapInfo> getMaps() {
        return _allMaps;
    }

    public ArrayList<MapInfo> getMaps(String mapName) {
        ArrayList<MapInfo> maps = new ArrayList<MapInfo>();

        for (MapInfo info : _allMaps) {
            if (!info.getName().equalsIgnoreCase(mapName) && !info.getUUID().toString().equalsIgnoreCase(mapName))
                continue;

            maps.add(info);
        }

        return maps;
    }

    public int getMaxMaps(PlayerRank rank) {
        if (rank.hasRank(Rank.OWNER))
            return 200;

        if (rank.hasRank(Rank.ADMIN))
            return 25;

        if (rank.hasRank(Rank.MAPMAKER))
            return 20;

        if (rank.hasRank(Rank.BUILDER))
            return 20;

        return 10;
    }

    private PacketContainer getPacket(GameProfile profile, PlayerInfoAction action) {

        PacketContainer playerInfo = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        playerInfo.getPlayerInfoAction().write(0, action);
        playerInfo.getPlayerInfoDataLists().write(0, Arrays.asList(
                new PlayerInfoData(WrappedGameProfile.fromHandle(profile), 0, NativeGameMode.CREATIVE,
                        WrappedChatComponent.fromText(""))));

        return playerInfo;
    }

    public ArrayList<Pair<Pair<UUID, UUID>, Long>> getSendToMap() {
        return _sendToMap;
    }

    public ServerManager getServerManager() {
        return _serverManager;
    }

    public WorldInfo getWorld(MapInfo map) {
        return _worlds.get(map.getUUID());
    }

    public WorldInfo getWorld(UUID map) {
        return _worlds.get(map);
    }

    public WorldInfo getWorld(World world) {
        for (WorldInfo info : _worlds.values()) {
            if (!info.getWorld().equals(world)) {
                continue;
            }

            return info;
        }

        return null;
    }

    public boolean hasPermissionOnWorld(Player player, World world) {
        WorldInfo info = getWorld(world);

        if (info.isBuilder(player)) {
            return true;
        }

        return false;
    }

    public String isValidName(String name) {
        // if (!name.matches("[\\w ]+"))
        // return false;

        if (_censor.isDirty(name))
            return "Map name contains dirty words!";

        if (name.length() > 30)
            return "Map name is too long!";

        if (name.equalsIgnoreCase("hub") || name.equalsIgnoreCase("lobby") || name.equalsIgnoreCase("mainworld") || name
                .equalsIgnoreCase("main world") || name.equalsIgnoreCase("world"))
            return "The name " + name + " is not permitted to be used!";

        if (name.length() < 3)
            return "Map name is too short!";

        return null;
    }

    public void loadMap(BuildInfo buildInfo) {
        String ip = _serverManager.getIP();
        UUID uuid = buildInfo.getMap();
        MapInfo mapInfo = getMap(uuid);

        if (mapInfo == null) {
            System.out.println("Error! Can't find the map for " + uuid);
            return;
        }

        if (getWorld(mapInfo) != null) {
            System.out.println("Error! Already running the map " + uuid);
            return;
        }

        if ((mapInfo.getBackup() == null && mapInfo.getIPLoc() == null) || (mapInfo.getIPLoc() != null && mapInfo
                .getIPLoc().equals(ip) && mapInfo.getFileLoc().equals(mapInfo.getZip().getAbsolutePath()))) {
            createWorld(buildInfo);
            return;
        }

        mapInfo.setFileInUse("Fetching map");

        new BukkitRunnable() {
            public void run() {
                if (mapInfo.getIPLoc() == null && mapInfo.getBackup() == null) {
                    System.err.println("Lost track of " + mapInfo.getUUID());
                    return;
                }

                try {
                    RemoteFileManager manager = new RemoteFileManager(ip, mapInfo);

                    if (mapInfo.getIPLoc() != null) {
                        manager.copyFileToLocal();
                    } else {
                        manager.grabBackup();
                    }

                    mapInfo.save();
                }
                catch (Exception ex) {
                    UtilError.handle(ex);

                    new BukkitRunnable() {
                        public void run() {
                            if (!_fetching.containsKey(mapInfo.getUUID()))
                                return;

                            mapInfo.setFileInUse(null);

                            ArrayList<UUID> players = _fetching.remove(mapInfo.getUUID());

                            for (UUID player : players) {
                                new RedisSendMessage(player,
                                        UtilError.format("Error grabbing map.. Please try again in a minute"));
                            }
                        }
                    }.runTask(getPlugin());

                    return;
                }

                new BukkitRunnable() {
                    public void run() {
                        if (!_fetching.containsKey(mapInfo.getUUID()))
                            return;

                        mapInfo.setFileInUse(null);

                        createWorld(buildInfo);
                    }
                }.runTask(getPlugin());
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public void loadWorld(Player player, BuildInfo buildInfo) {
        _fetching.put(buildInfo.getMap(), new ArrayList<UUID>());

        if (player != null) {
            _fetching.get(buildInfo.getMap()).add(player.getUniqueId());
        }

        _buildOffers.put(buildInfo.getUUID(), new HashSet<BuildInfo>());

        new BukkitRunnable() {
            public void run() {
                System.out.println("Asked for build offers for " + buildInfo.getMap());
                new RedisPublishBuildStatus(buildInfo);
            }
        }.runTaskAsynchronously(getPlugin());

        new BukkitRunnable() {
            public void run() {
                if (!_buildOffers.containsKey(buildInfo.getUUID())) {
                    System.out.println("Build offers was removed for " + buildInfo.getMap());

                    if (player != null) {
                        player.sendMessage(C.Red + "Looks like its already running!");
                    }

                    _fetching.remove(buildInfo.getMap());

                    return;
                }

                ArrayList<BuildInfo> info = new ArrayList(_buildOffers.remove(buildInfo.getUUID()));

                Collections.sort(info);

                if (info.isEmpty()) {
                    System.out.println("No Build offers for " + buildInfo.getMap());

                    if (player != null) {
                        player.sendMessage(C.Red + "Can't start the server! All servers are full?..");
                    }

                    _fetching.remove(buildInfo.getMap());
                    return;
                }

                BuildInfo offer = info.get(0);

                offer.setCreateServer(true);

                new BukkitRunnable() {
                    public void run() {
                        System.out.println("Asked " + offer.getOffer() + " to make " + buildInfo.getMap());
                        new RedisPublishBuildStatus(offer);
                    }
                }.runTaskAsynchronously(getPlugin());
            }
        }.runTaskLater(getPlugin(), 20);
    }

    public void loadWorld(Player player, MapInfo mapInfo) {
        WorldInfo info = getWorld(mapInfo);

        if (info != null) {
            if (player != null) {
                UtilPlayer.tele(player, info.getWorld().getSpawnLocation());
            }

            return;
        } else if (mapInfo.isWorldLoaded()) {
            if (player != null) {
                if (Objects.equals(mapInfo.getLoadedServer(), ServerManager.getServerName())) {
                    player.sendMessage(C.Red + "Unexpected error! World says its loaded, but its not!");
                    return;
                }

                UUID uuid = player.getUniqueId();
                String server = ServerManager.getServerName();

                new BukkitRunnable() {
                    public void run() {
                        new RedisJoinMap(uuid, mapInfo.getUUID());
                        new RedisSwitchServer(uuid, server);
                    }
                }.runTaskAsynchronously(getPlugin());
            }

            return;
        }

        if (player != null) {
            if (!Recharge.canUse(player, "Load World")) {
                player.sendMessage(C.Red + "You're loading too many worlds! Please wait a bit!");
                return;
            }

            Recharge.use(player, "Load World", 7000);
        }

        if (_fetching.containsKey(mapInfo.getUUID()))
            return;

        BuildInfo buildInfo = new BuildInfo(player, mapInfo.getUUID());

        loadWorld(player, buildInfo);
    }

    @EventHandler
    public void onBackupTimer(TimeEvent event) {
        if (event.getType() != TimeType.TEN_MIN)
            return;

        saveWorlds();
        scanToBackup();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(ChatEvent event) {
        event.setCancelled(true);

        String[] message = event.getMessage();
        boolean global = message[0].startsWith("@");

        if (global)
            event.removeFirstLetter();

        if (Preference.getPreference(event.getPlayer(), _buildManager.getGlobalChat()))
            global = !global;

        WorldInfo info = getWorld(event.getPlayer().getWorld());

        if (global || info == null) {
            event.setPrefix(C.DGreen + C.Bold + "GLOBAL " + C.Reset + event.getPrefix());

            _buildChat.handle(event);
        } else {
            event.setPrefix(C.Aqua + C.Bold + "WORLD " + C.Reset + event.getPrefix());

            info.getChannel().handle(event);

            if (info.getChannel().getReceivers().size() <= 1)
                event.getPlayer().sendMessage(C.Blue + "Your voice seems to echo in this empty world..");
        }
    }

    public void onGameProfile(LibsGameProfile libsProfile) {
        UUID joinedMap = libsProfile.getMapJoined();
        UUID mapLeft = libsProfile.getMapLeft();
        UUID uuid = libsProfile.getProfile().getId();
        GameProfile gameProfile = libsProfile.getProfile();

        if (joinedMap != null && getFakePlayers().containsKey(joinedMap) && getFakePlayers().get(joinedMap)
                .containsKey(uuid)) {
            return;
        }

        boolean newPlayer = true;

        // If the gameProfile isnt in 'leftMap' then ignore
        // Make sure that if 'joinedMap' is null, then player is in 'leftMap'
        if (mapLeft != null) {
            newPlayer = false;

            if (!getFakePlayers().containsKey(mapLeft) || !getFakePlayers().get(mapLeft).containsKey(uuid)) {
                return;
            }

            getFakePlayers().get(mapLeft).remove(uuid);

            if (getFakePlayers().get(mapLeft).isEmpty()) {
                getFakePlayers().remove(mapLeft);
            }
        }

        if (joinedMap == null) {
            if (!newPlayer) {
                PacketContainer packet = getPacket(gameProfile, PlayerInfoAction.REMOVE_PLAYER);

                for (Player p : UtilPlayer.getPlayers()) {
                    if (p.getUniqueId().equals(uuid))
                        continue;

                    UtilPlayer.sendPacket(p, packet);
                }
            }

            return;
        }

        MapInfo mapInfo = getMap(joinedMap);

        if (mapInfo == null)
            return;

        HashMap<UUID, GameProfile> hashmap = _fakePlayers.get(joinedMap);

        if (hashmap == null)
            _fakePlayers.put(joinedMap, hashmap = new HashMap<UUID, GameProfile>());

        hashmap.put(uuid, gameProfile);

        if (Bukkit.getPlayer(uuid) != null || !newPlayer)
            return;

        PacketContainer packet = getPacket(gameProfile, PlayerInfoAction.ADD_PLAYER);

        for (Player p : UtilPlayer.getPlayers()) {
            if (p.getUniqueId().equals(uuid))
                continue;

            UtilPlayer.sendPacket(p, packet);
        }
    }

    // @EventHandler
    public void onMerge(MergeEvent event) {
        if (_worlds.isEmpty())
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onScoreboardSecond(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        ArrayList<Entry<UUID, HashMap<UUID, GameProfile>>> worlds = new ArrayList<Entry<UUID, HashMap<UUID,
                GameProfile>>>(
                getFakePlayers().entrySet());

        Collections.sort(worlds, new Comparator<Entry<UUID, HashMap<UUID, GameProfile>>>() {
            @Override
            public int compare(Entry<UUID, HashMap<UUID, GameProfile>> o1, Entry<UUID, HashMap<UUID, GameProfile>> o2) {
                return Integer.compare(o2.getValue().size(), o1.getValue().size());
            }
        });

        ArrayList<String> lines = new ArrayList<String>();

        for (int i = 0; i < worlds.size() && i < 15; i++) {
            Entry<UUID, HashMap<UUID, GameProfile>> info = worlds.get(i);

            String name = getMap(info.getKey()).getName();

            if (name.length() > 15)
                name = name.substring(0, 15) + "...";

            lines.add(C.Gold + name + " " + C.Yellow + info.getValue().size());
        }

        _scoreboardManager.getMainScoreboard().setSidebar(lines);
    }

    public void saveWorlds() {
        for (WorldInfo world : _worlds.values()) {
            if (!world.isSaveReady())
                continue;

            if (world.getData().isFileInUse())
                continue;

            world.Announce(C.Gold + "Saving world..");

            world.saveMap(_serverManager.getIP());

            UtilString.log("Saved map " + world.getData().getUUID().toString() + " to " + getIP() + " and path" + world
                    .getData().getZip().getAbsolutePath());

            world.Announce(C.Gold + "Saved world");
        }
    }

    public void scanToBackup() {
        if (getDefaultWorldInfo() == null)
            return;

        ArrayList<MapInfo> toBackup = new ArrayList<MapInfo>();
        String date = new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime());

        String ip = _serverManager.getIP();

        for (MapInfo info : getMaps()) {
            if (info.getIPLoc() == null) {
                continue;
            }

            if (info.getBackupFrequency() == 0) {
                continue;
            }

            if (!UtilTime.elasped(info.getLastBackup(), UtilTime.DAY * 1000 * info.getBackupFrequency())) {
                continue;
            }

            if (info.isFileInUse()) {
                continue;
            }

            if (info.getBackup() != null && info.getBackup().equals(date)) {
                continue;
            }

            // If it has been backed up before and that backup was after it was last modified
            if (info.getLastBackup() != null && info.getTimeModified().getTime() < info.getLastBackup().getTime())
                continue;

            toBackup.add(info);

            info.setFileInUse("Backing up");
        }

        new BukkitRunnable() {
            public void run() {
                for (MapInfo info : toBackup) {
                    System.out.println("Attempting to backup " + info.getUUID() + " to " + date);

                    try {
                        boolean grabTheFile = !info.getIPLoc().equals(ip) || !info.getFileLoc()
                                .equals(info.getZip().getAbsolutePath());

                        if (grabTheFile) {
                            new RemoteFileManager(ip, info).copyFileToLocal();
                        }

                        String sha = UtilFile.getSha(info.getZip());

                        if (info.getBackupVersion() != null && info.getBackupVersion().equals(sha)) {
                            if (grabTheFile) {
                                info.save(); // Saves file location
                            }

                            continue;
                        }

                        if (!info.getZip().exists() || info.getZip().length() > 5e+7) {
                            if (grabTheFile) {
                                info.save(); // Saves file location
                            }

                            continue;
                        }

                        new RemoteFileManager(ip, info).addBackup(date, sha);

                        info.setBackedUp();

                        info.save();

                        MysqlFetchMapBackups fetchBackups = new MysqlFetchMapBackups(info);

                        ArrayList<Pair<String, Timestamp>> backups = fetchBackups.getBackups();
                        ArrayList<String> toDelete = new ArrayList<String>();

                        for (int i = 15; i < backups.size(); i++) {
                            toDelete.add(backups.get(i).getKey());
                        }

                        if (!backups.isEmpty()) {
                            new MysqlDeleteBackups(info, toDelete);
                        }
                    }
                    catch (Exception ex) {
                        System.err.println("Error while handling " + info.getUUID());
                        UtilError.handle(ex);
                    }
                }

                for (MapInfo info : toBackup) {
                    info.setFileInUse(null);
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public void sendDefaultWorld(Player player) {
        MapInfo info = getMap(BuildManager.getMainHub());

        if (!info.isWorldLoaded()) {
            System.out.println("Default world isn't loaded?..");
            loadWorld(null, info);
        }

        WorldInfo worldInfo = getWorld(info);

        if (worldInfo == null) {
            UUID playerUUID = player.getUniqueId();

            new BukkitRunnable() {
                public void run() {
                    new RedisJoinMap(playerUUID, info.getUUID());
                    new RedisSwitchServer(playerUUID, info.getLoadedServer());
                }
            }.runTaskAsynchronously(getPlugin());
        } else {
            player.teleport(worldInfo.getWorld().getSpawnLocation());
        }
    }

    public void unloadEmptyWorlds() {
        Iterator<Entry<UUID, WorldInfo>> itel = _worlds.entrySet().iterator();

        while (itel.hasNext()) {
            Entry<UUID, WorldInfo> entry = itel.next();

            WorldInfo info = entry.getValue();

            if (info.getData().getUUID().equals(BuildManager.getMainHub()))
                continue;

            if (!info.isEmpty())
                continue;

            if (!info.unloadWorld(this, _serverManager.getIP(), false))
                continue;

            itel.remove();

            getChannel().broadcast(C.DGreen + "World> " + C.Aqua + "Unloaded map " + info.getData().getName());
        }
    }

    public void updateInfo(MapInfo info) {
        for (MapInfo mapInfo : getMaps()) {
            if (!mapInfo.getUUID().equals(info.getUUID()))
                continue;

            mapInfo.cloneFrom(info);

            if (info.getUUID().equals(BuildManager.getMainHub()) && !info.isInUse()) {
                getDefaultWorldInfo();
            }
        }
    }
}
