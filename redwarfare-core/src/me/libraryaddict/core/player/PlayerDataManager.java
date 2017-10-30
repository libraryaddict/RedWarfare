package me.libraryaddict.core.player;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.player.commands.CommandBalance;
import me.libraryaddict.core.player.commands.CommandGiveCredits;
import me.libraryaddict.core.player.commands.CommandHits;
import me.libraryaddict.core.player.commands.CommandSeen;
import me.libraryaddict.core.player.database.CurrencyTrackerListener;
import me.libraryaddict.core.player.events.PlayerLoadEvent;
import me.libraryaddict.core.player.events.PlayerUnloadEvent;
import me.libraryaddict.core.player.listener.RedisListenerMutePlayer;
import me.libraryaddict.core.player.listener.RedisListenerSavePlayer;
import me.libraryaddict.core.player.listener.RedisListenerSendMessage;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Owned;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.*;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.network.PlayerOwned;
import me.libraryaddict.redis.operations.RedisFetchPlayerData;
import me.libraryaddict.redis.operations.RedisSavePlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDataManager extends MiniPlugin {
    private boolean _allowJoins;
    private HashMap<UUID, PlayerData> _playerData = new HashMap<UUID, PlayerData>();
    private HashMap<UUID, PlayerOwned> _playerOwned = new HashMap<UUID, PlayerOwned>();
    private CurrencyTrackerListener _redisMapping;

    public PlayerDataManager(JavaPlugin plugin, CommandManager commandManager, ServerManager serverManager) {
        super(plugin, "Player Data Manager");

        if (!Bukkit.getOnlineMode()) { // Why bother when this server won't be on the main network
            new BukkitRunnable() {
                public void run() {
                    new RedisListenerSavePlayer(PlayerDataManager.this, ServerManager.getServerName());
                }
            }.runTaskAsynchronously(getPlugin());
        }

        new BukkitRunnable() {
            public void run() {
                new RedisListenerMutePlayer(PlayerDataManager.this);
            }
        }.runTaskAsynchronously(getPlugin());

        new BukkitRunnable() {
            public void run() {
                new RedisListenerSendMessage(getPlugin());
            }
        }.runTaskAsynchronously(getPlugin());

        _redisMapping = new CurrencyTrackerListener(getPlugin());

        new BukkitRunnable() {
            public void run() {
                _redisMapping.addListener();
            }
        }.runTaskAsynchronously(getPlugin());

        new BukkitRunnable() {
            public void run() {
                _allowJoins = true;
            }
        }.runTaskLater(getPlugin(), 5);

        Currency.init(this);
        Owned.init(this);
        Preference.init(this);

        commandManager.registerCommand(new CommandBalance());
        commandManager.registerCommand(new CommandHits(getPlugin()));
        commandManager.registerCommand(new CommandSeen());
        commandManager.registerCommand(new CommandGiveCredits());
    }

    public PlayerData getData(Player player) {
        synchronized (this) {
            return _playerData.get(player.getUniqueId());
        }
    }

    public CurrencyTrackerListener getMappings() {
        return _redisMapping;
    }

    public PlayerOwned getOwned(Player player) {
        return _playerOwned.get(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (getData(player) == null) {
            event.disallow(org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER,
                    UtilError.format("Error loading your data"));

            System.out.println("Error loading " + player.getUniqueId());
            return;
        }

        PlayerLoadEvent newEvent = new PlayerLoadEvent(player, getData(player));

        Bukkit.getPluginManager().callEvent(newEvent);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLoginResult(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED)
            return;

        PlayerUnloadEvent newEvent = new PlayerUnloadEvent(event.getPlayer(), getData(event.getPlayer()));

        Bukkit.getPluginManager().callEvent(newEvent);

        UUID uuid = event.getPlayer().getUniqueId();

        synchronized (this) {
            _playerData.remove(uuid);
        }

        _redisMapping.unloadOwned(uuid);
        _playerOwned.remove(uuid);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!_allowJoins) {
            event.disallow(Result.KICK_OTHER, "Server is still starting up");
            return;
        }

        UUID uuid = event.getUniqueId();
        PlayerData playerData;

        if (Bukkit.getOnlineMode()) {
            System.out.println("Working around bungee..");

            MysqlFetchBanInfo fetchInfo = new MysqlFetchBanInfo(uuid.toString());
            String banMessage = fetchInfo.isBanned();

            if (banMessage == null) {
                banMessage = new MysqlFetchBanInfo(uuid.toString()).isBanned();
            }

            if (banMessage != null) {
                event.disallow(Result.KICK_OTHER, banMessage);
                return;
            }

            MysqlLoadPlayerData fetchData = new MysqlLoadPlayerData(uuid);

            if (!fetchData.isSuccess() || fetchData.getPlayerData() == null) {
                event.disallow(Result.KICK_OTHER, "Unable to load PlayerData");
                return;
            }

            Pair<Pair<String, String>, Long> mute = fetchInfo.isMuted();

            playerData = fetchData.getPlayerData();

            if (mute != null) {
                playerData.setMute(mute.getKey().getKey(), mute.getKey().getValue(), mute.getValue());
            }
        } else {
            RedisFetchPlayerData thingy = new RedisFetchPlayerData(uuid);

            if (!thingy.isSuccess() || thingy.getPlayerData() == null) {
                event.disallow(Result.KICK_OTHER, "Unable to load PlayerData");
                return;
            }

            playerData = thingy.getPlayerData();
        }

        MysqlFetchCurrency thingy = new MysqlFetchCurrency(uuid);

        if (playerData == null || !thingy.isSuccess()) {
            event.disallow(Result.KICK_OTHER, "Unable to load PlayerData");
            return;
        }

        MysqlLoadOwned owned = new MysqlLoadOwned(uuid);

        if (!owned.isSuccess()) {
            event.disallow(Result.KICK_OTHER, "Unable to load PlayerData");
            return;
        }

        synchronized (this) {
            _playerData.put(uuid, playerData);
        }

        _playerOwned.put(uuid, owned.getOwned());
        _redisMapping.loadOwned(uuid, thingy.getItems());

        System.out.println("Fetched " + uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginResult(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() == Result.ALLOWED || _redisMapping == null)
            return;

        UUID uuid = event.getUniqueId();

        synchronized (this) {
            _playerData.remove(uuid);
        }

        _playerOwned.remove(uuid);
        _redisMapping.unloadOwned(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (Bukkit.getOnlineMode()) {
            PlayerData playerData = getData(event.getPlayer()).clone();

            new BukkitRunnable() {
                public void run() {
                    new MysqlSavePlayerData(playerData);
                }
            };
        }

        UUID uuid = event.getPlayer().getUniqueId();

        synchronized (this) {
            _playerData.remove(uuid);
        }

        _playerOwned.remove(uuid);
        _redisMapping.unloadOwned(uuid);
    }

    public void saveData(UUID uuid) {
        synchronized (this) {
            if (!_playerData.containsKey(uuid))
                return;
        }

        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            UtilError.log("Tried to save data for " + uuid.toString() + " but the player isn't online?");
            return;
        }

        PlayerData data;

        synchronized (this) {
            data = _playerData.get(uuid);
        }

        PlayerUnloadEvent event = new PlayerUnloadEvent(player, data);
        Bukkit.getPluginManager().callEvent(event);

        PlayerData newData = data.clone();

        new BukkitRunnable() {
            public void run() {
                long started = 0;

                while (event.isPending() && !UtilTime.elasped(started, 7000)) {
                    try {
                        Thread.sleep(30);
                    }
                    catch (InterruptedException e) {
                        UtilError.handle(e);
                    }
                }

                new RedisSavePlayerData(newData, true);
            }
        }.runTaskAsynchronously(getPlugin());
    }
}
