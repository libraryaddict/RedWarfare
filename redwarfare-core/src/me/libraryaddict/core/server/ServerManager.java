package me.libraryaddict.core.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.server.commands.CommandJoinServer;
import me.libraryaddict.core.server.commands.CommandPaste;
import me.libraryaddict.core.server.commands.CommandServer;
import me.libraryaddict.core.server.commands.CommandStop;
import me.libraryaddict.core.server.commands.CommandThreads;
import me.libraryaddict.core.server.commands.CommandVersion;
import me.libraryaddict.core.server.listeners.RedisListenerServerMerge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.redis.operations.MysqlFetchVersion;
import me.libraryaddict.redis.operations.RedisListenerUpdate;
import me.libraryaddict.redis.operations.RedisSwitchServer;

public class ServerManager extends MiniPlugin
{
    private static String _serverName = "lobby";

    public static String getServerName()
    {
        return _serverName;
    }

    private String _fileName;
    private ServerType _gameType;
    private String _ip;
    private boolean _merging;
    private boolean _needToUpdate;
    private boolean _public;
    private long _shutdownStarted;
    private boolean _shuttingDown;

    private String _spinner;

    private String _version;

    public ServerManager(JavaPlugin plugin, CommandManager commandManager)
    {
        super(plugin, "Server Manager");

        try
        {
            for (String fileName : new String[]
                {
                        "Arcade.jar", "Build.jar", "Hub.jar"
                })
            {
                File file = new File("plugins/" + fileName);

                if (!file.exists())
                    continue;

                _fileName = file.getName();
                _version = UtilFile.getSha(file);
            }
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        while (true)
        {
            try
            {
                URL whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

                _ip = in.readLine();
                in.close();

                break;
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }

        if (plugin.getName().contains("Hub"))
        {
            _serverName = "Hub";
            _gameType = ServerType.Hub;
        }
        else if (plugin.getName().contains("Arcade"))
        {
            _serverName = "Arcade";
            _gameType = ServerType.SearchAndDestroy;
        }
        else if (plugin.getName().contains("Build"))
        {
            _serverName = "Build";
            _gameType = ServerType.Build;
        }

        _serverName += "-" + UUID.randomUUID().toString();

        if (new File("ServerInfo").exists())
        {
            _public = true;

            try
            {
                Properties properties = new Properties();

                FileInputStream fileInput = new FileInputStream(new File("ServerInfo"));

                properties.load(fileInput);

                _serverName = properties.getProperty("ServerName", _serverName);
                _gameType = ServerType.valueOf(properties.getProperty("ServerType", "SearchAndDestroy"));

                if (_gameType == null)
                {
                    System.out.println("Unrecognized gametype: " + properties.getProperty("ServerType"));
                    _gameType = ServerType.SearchAndDestroy;
                }

                _spinner = properties.getProperty("Spinner");
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }

        commandManager.registerCommand(new CommandServer(this));
        commandManager.registerCommand(new CommandJoinServer(getPlugin()));
        commandManager.registerCommand(new CommandStop(this));
        commandManager.registerCommand(new CommandVersion(plugin, this));
        commandManager.registerCommand(new CommandPaste(plugin));
        commandManager.registerCommand(new CommandThreads());

        if (!isTestServer())
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    new RedisListenerUpdate(_spinner, new Runnable()
                    {
                        public void run()
                        {
                            new BukkitRunnable()
                            {
                                public void run()
                                {
                                    checkUpdate();
                                }
                            }.runTask(getPlugin());
                        }
                    });
                }
            }.runTaskAsynchronously(getPlugin());

            new BukkitRunnable()
            {
                public void run()
                {
                    new RedisListenerServerMerge(ServerManager.this);
                }
            }.runTaskAsynchronously(getPlugin());
        }

        UtilError.setServer(getServerName());

        System.out.println("Enabled server: " + getServerName());
    }

    public void checkUpdate()
    {
        if (isUpdateReady() || isTestServer())
            return;

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchVersion fetchVersion = new MysqlFetchVersion(_fileName);

                if (!fetchVersion.isSuccess())
                    return;

                if (!fetchVersion.isOld(getInternalVersion()))
                    return;

                synchronized (ServerManager.this)
                {
                    _needToUpdate = true;
                }
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public void doMerge(String message)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                MergeEvent mergeEvent = new MergeEvent();

                Bukkit.getPluginManager().callEvent(mergeEvent);

                if (mergeEvent.isCancelled())
                    return;

                _merging = true;
                Bukkit.broadcastMessage(C.Gold + "This server is being merged..");

                ArrayList<UUID> players = new ArrayList<UUID>();

                UtilPlayer.getPlayers().stream().forEach(player -> players.add(player.getUniqueId()));

                int i = 0;

                new BukkitRunnable()
                {
                    public void run()
                    {
                        for (UUID uuid : players)
                        {
                            new RedisSwitchServer(uuid, message);
                        }
                    }
                }.runTaskLaterAsynchronously(getPlugin(), i++ / 3);

                new BukkitRunnable()
                {
                    public void run()
                    {
                        shutdown(C.Gold + "Server was merged");
                    }
                }.runTaskLater(getPlugin(), (10 * 20) + i);

            }
        }.runTask(getPlugin());
    }

    public ServerType getGameType()
    {
        return _gameType;
    }

    public String getInternalVersion()
    {
        return _version;
    }

    public String getIP()
    {
        return _ip;
    }

    public boolean isMerging()
    {
        return _merging;
    }

    public boolean isPublic()
    {
        return _public;
    }

    public boolean isShuttingDown()
    {
        synchronized (this)
        {
            return _shuttingDown;
        }
    }

    public boolean isTestServer()
    {
        return _spinner == null;
    }

    public boolean isUpdateReady()
    {
        synchronized (this)
        {
            return _needToUpdate;
        }
    }

    @EventHandler
    public void onAsyncLogin(AsyncPlayerPreLoginEvent event)
    {
        if (!isShuttingDown())
            return;

        event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
    }

    @EventHandler
    public void onAsyncLogin(PlayerLoginEvent event)
    {
        if (!isShuttingDown())
            return;

        event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
    }

    @EventHandler
    public void onShutdownTick(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        if (!isShuttingDown())
            return;

        if (!UtilTime.elasped(_shutdownStarted, 2000))
            return;

        Bukkit.shutdown();
    }

    public void shutdown(String reason)
    {
        if (isShuttingDown())
            return;

        synchronized (this)
        {
            _shuttingDown = true;
        }

        _shutdownStarted = System.currentTimeMillis();

        for (Player player : UtilPlayer.getPlayers())
        {
            player.kickPlayer(reason);
        }

        System.out.println("Now shutting down: " + reason);
    }

}
