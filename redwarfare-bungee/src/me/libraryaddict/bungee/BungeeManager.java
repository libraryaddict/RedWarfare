package me.libraryaddict.bungee;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import me.libraryaddict.bungee.redislisteners.RedisBungeeSettingsListener;
import me.libraryaddict.bungee.redislisteners.RedisKickListener;
import me.libraryaddict.bungee.redislisteners.RedisListenerServerStatus;
import me.libraryaddict.bungee.redislisteners.RedisPlayerDataSaveListener;
import me.libraryaddict.bungee.redislisteners.RedisPlayerSwitchListener;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.MysqlManager;
import me.libraryaddict.mysql.operations.MysqlFetchBungeeSettings;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.network.BungeeStatus;
import me.libraryaddict.redis.RedisManager;
import me.libraryaddict.redis.operations.RedisFetchBungeeServers;
import me.libraryaddict.redis.operations.RedisSendMessage;
import net.md_5.bungee.api.Favicon;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeManager
{
    private Bungee _bungee;
    private ArrayList<BungeeStatus> _bungeeStatus = new ArrayList<BungeeStatus>();
    private Favicon _favIcon;
    private ArrayList<String> _hubs = new ArrayList<String>();
    private BungeeListener _listener;
    private String _name;
    private int _players;
    private ArrayList<Pair<String, Long>> _servers = new ArrayList<Pair<String, Long>>();
    private BungeeSettings _settings;
    private HashMap<String, Thread> _threads = new HashMap<String, Thread>();
    private int _totalPlayers;

    public BungeeManager(Bungee bungee)
    {
        _bungee = bungee;
        _name = bungee.getProxy().getConfigurationAdapter().getString("stats", "UnknownBungee");

        new RedisManager();
        new MysqlManager(6);

        _listener = new BungeeListener(this);

        bungee.getProxy().getPluginManager().registerListener(bungee, _listener);

        while (true)
        {
            MysqlFetchBungeeSettings fetchBungee = new MysqlFetchBungeeSettings();

            if (!fetchBungee.isSuccess())
            {
                try
                {
                    UtilError.log("Error fetching bungee settings.. Trying again in 5 seconds");
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    UtilError.handle(e);
                }

                continue;
            }

            setSettings(fetchBungee.getSettings());
            break;
        }

        RedisFetchBungeeServers bungeeServers = new RedisFetchBungeeServers();

        _bungeeStatus.addAll(bungeeServers.getStatus());

        for (BungeeStatus server : bungeeServers.getStatus())
        {
            if (server.getName().equals(getName()))
                continue;

            _totalPlayers += server.getPlayers();
        }

        bungee.getProxy().getScheduler().schedule(bungee, new Runnable()
        {
            private long _lastEdit;

            public void run()
            {
                synchronized (BungeeManager.this)
                {
                    _totalPlayers = 0;

                    Iterator<BungeeStatus> itel = _bungeeStatus.iterator();

                    while (itel.hasNext())
                    {
                        BungeeStatus status = itel.next();

                        if (UtilTime.elasped(status.getLastUpdated(), 30000))
                        {
                            itel.remove();
                            continue;
                        }

                        _totalPlayers += status.getPlayers();
                    }

                    int players = ProxyServer.getInstance().getOnlineCount();

                    if (players == _players && !UtilTime.elasped(_lastEdit, 10000))
                        return;

                    _lastEdit = System.currentTimeMillis();
                    _players = players;

                    updateStatus(players);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);

        new Thread()
        {
            public void run()
            {
                new RedisPlayerDataSaveListener(BungeeManager.this);
            }
        }.start();

        new Thread()
        {
            public void run()
            {
                new RedisBungeeSettingsListener(BungeeManager.this);
            }
        }.start();

        new Thread()
        {
            public void run()
            {
                new RedisKickListener(BungeeManager.this);
            }
        }.start();

        new Thread()
        {
            public void run()
            {
                new RedisPlayerSwitchListener(BungeeManager.this);
            }
        }.start();

        new RedisListenerServerStatus(this);
    }

    public void addStatus(BungeeStatus status)
    {
        synchronized (this)
        {
            _bungeeStatus.remove(status);
            _bungeeStatus.add(status);
        }
    }

    public Favicon getFavicon()
    {
        synchronized (this)
        {
            return _favIcon;
        }
    }

    public String getHub()
    {
        synchronized (_hubs)
        {
            return UtilMath.r(_hubs);
        }
    }

    public BungeeListener getListener()
    {
        return _listener;
    }

    public String getName()
    {
        return _name;
    }

    public Plugin getPlugin()
    {
        return _bungee;
    }

    public BungeeSettings getSettings()
    {
        synchronized (this)
        {
            return _settings;
        }
    }

    public int getTotalPlayers()
    {
        synchronized (this)
        {
            return _totalPlayers + _players;
        }
    }

    public boolean isHub(String hub)
    {
        synchronized (_hubs)
        {
            return _hubs.contains(hub);
        }
    }

    public void kickPlayer(String uuidOrIP, String message)
    {
        if (uuidOrIP.contains("-"))
        {
            ProxiedPlayer player = _bungee.getProxy().getPlayer(UUID.fromString(uuidOrIP));

            if (player != null)
            {
                player.disconnect(message);
            }
        }
        else
        {
            for (ProxiedPlayer player : _bungee.getProxy().getPlayers())
            {
                String ip = player.getAddress().getAddress().getHostAddress();

                if (!Objects.equals(uuidOrIP, ip))
                    continue;

                player.disconnect(message);
            }
        }
    }

    public void notify(String uuid)
    {
        synchronized (_threads)
        {
            if (!_threads.containsKey(uuid))
                return;

            _threads.remove(uuid).interrupt();
        }
    }

    /* public void notifyPlayerData(ProxiedPlayer player)
    {
        if (player.getServer() == null || !player.getServer().isConnected())
            return;
    
        try
        {
            System.out.println("Waiting " + player.getName() + " to be notified");
            UUID uuid = player.getUniqueId();
    
            synchronized (_threads)
            {
                _threads.put(uuid.toString(), Thread.currentThread());
            }
    
            new RedisNotifySavePlayerData(player.getServer().getInfo().getName(), uuid);
    
            Thread.sleep(10000);
    
            synchronized (_threads)
            {
                _threads.remove(uuid.toString());
            }
        }
        catch (InterruptedException ex)
        {
        }
    }*/

    public void onMessage(me.libraryaddict.network.ServerInfo serverInfo)
    {
        Iterator<Pair<String, Long>> itel = _servers.iterator();
        boolean found = false;
        boolean hub = serverInfo.getType() == ServerType.Hub;
        String name = serverInfo.getName();

        while (itel.hasNext())
        {
            Pair<String, Long> pair = itel.next();

            if (serverInfo.getStarts() >= 0 && name.equals(pair.getKey()))
            {
                itel.remove();
                found = true;
            }
            else if (serverInfo.getStarts() < 0 || UtilTime.elasped(pair.getValue(), 5000))
            {
                synchronized (_hubs)
                {
                    _hubs.remove(pair.getKey());
                }

                ProxyServer.getInstance().getServers().remove(pair.getKey().toLowerCase());
                itel.remove();
            }
        }

        if (serverInfo.getStarts() < 0)
            return;

        _servers.add(Pair.of(name, System.currentTimeMillis()));

        if (!found)
        {
            String[] split = serverInfo.getIP().split(":");

            ServerInfo info = ProxyServer.getInstance().constructServerInfo(name,
                    new InetSocketAddress(split[0], Integer.parseInt(split[1])), "None", false);

            Map<String, ServerInfo> servers = ProxyServer.getInstance().getServers();

            if (servers.containsKey(name))
            {
                if (servers.get(name).getAddress().equals(info.getAddress()))
                {
                    return;
                }
            }

            System.out.println("Registered new " + (hub ? "hub" : "server") + " " + name.toLowerCase());
            ProxyServer.getInstance().getServers().put(name.toLowerCase(), info);

            if (hub)
            {
                synchronized (_hubs)
                {
                    _hubs.add(name);
                }
            }
        }
    }

    public void setSettings(BungeeSettings newSettings)
    {
        synchronized (this)
        {
            _settings = newSettings;
            _favIcon = Favicon.create(newSettings.getFavIcon());
        }
    }

    public void switchServer(UUID uuid, String serverName)
    {
        ProxiedPlayer player = this._bungee.getProxy().getPlayer(uuid);

        if (player == null)
            return;

        ServerInfo server = _bungee.getProxy().getServerInfo(serverName.toLowerCase());

        if (server == null)
        {
            new RedisSendMessage(uuid, C.Red + "Cannot find the server '" + serverName + "'");
            return;
        }

        player.connect(server);
    }

    public void updateStatus(int players)
    {
        BungeeStatus status = new BungeeStatus(getName(), players);

        new Thread()
        {
            public void run()
            {
                // new RedisSaveBungeeStatus(status);
            }
        }.start();
    }
}
