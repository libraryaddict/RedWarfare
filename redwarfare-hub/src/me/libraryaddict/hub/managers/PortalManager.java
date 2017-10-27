package me.libraryaddict.hub.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.hub.types.HubServerListener;
import me.libraryaddict.hub.types.Portal;
import me.libraryaddict.hub.types.Server;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.redis.operations.RedisSendMessage;
import me.libraryaddict.redis.operations.RedisSwitchServer;

public class PortalManager extends MiniPlugin
{
    private ArrayList<Portal> _portals = new ArrayList<Portal>();
    private HashMap<ServerType, ArrayList<Server>> _runningServers = new HashMap<ServerType, ArrayList<Server>>();
    private ServerManager _serverManager;

    public PortalManager(JavaPlugin plugin, ServerManager serverManager)
    {
        super(plugin, "Portal Manager");

        _serverManager = serverManager;

        new BukkitRunnable()
        {
            public void run()
            {
                new HubServerListener(PortalManager.this);
            }
        }.runTaskAsynchronously(getPlugin());

        new BukkitRunnable()
        {
            public void run()
            {
                new RedisListenerJoinServerType(PortalManager.this);
            }
        }.runTaskAsynchronously(getPlugin());

        for (ServerType type : ServerType.values())
        {
            _runningServers.put(type, new ArrayList<Server>());
        }

        new BukkitRunnable()
        {
            public void run()
            {
                loadPortals();
            }
        }.runTask(getPlugin());
    }

    public ArrayList<Portal> getPortals()
    {
        return _portals;
    }

    public void joinGame(UUID uuid, ServerType serverType)
    {
        if (_serverManager.isUpdateReady())
            return;

        ArrayList<Server> servers = _runningServers.get(serverType);

        Iterator<Server> itel = servers.iterator();

        while (itel.hasNext())
        {
            Server server = itel.next();

            if (!server.isValid())
            {
                itel.remove();
                continue;
            }
        }

        Collections.sort(servers);

        if (servers.isEmpty())
        {
            new RedisSendMessage(uuid, C.Red + "There are no " + serverType.getName() + " servers running");
            return;
        }

        Server toJoin = servers.get(0);

        if (toJoin.getPlayers() == 0 && servers.size() > 1)
        {
            Server server = servers.get(1);

            if (!server.isInProgress() && !server.isFull())
            {
                toJoin = server;
            }
        }

        String serverName = toJoin.getName();

        new BukkitRunnable()
        {
            public void run()
            {
                new RedisSwitchServer(uuid, serverName);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    private void loadPortals()
    {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("world/config.yml"));

        for (String key : config.getConfigurationSection("Portals").getKeys(false))
        {
            ConfigurationSection section = config.getConfigurationSection("Portals." + key);

            Portal portal = new Portal(getPlugin(), section, _runningServers.get(ServerType.valueOf(key)));

            _portals.add(portal);
        }
    }

    @EventHandler
    public void onHalfSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        for (Portal portal : _portals)
        {
            portal.updateBlocks();
        }
    }

    public void onReceive(ServerInfo serverInfo)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                ServerType type = serverInfo.getType();

                if (type == null)
                {
                    removeServer(serverInfo.getName());
                    return;
                }

                ArrayList<Server> servers = _runningServers.get(type);

                Server server = null;

                for (Server s : servers)
                {
                    if (!s.getName().equals(serverInfo.getName()))
                        continue;

                    server = s;
                    break;
                }

                if (server == null)
                {
                    removeServer(serverInfo.getName());

                    if (serverInfo.getStarts() == -1)
                    {
                        return;
                    }

                    server = new Server(serverInfo.getName(), type);
                    servers.add(server);
                }

                if (serverInfo.getStarts() == -1)
                {
                    servers.remove(server);
                }
                else
                {
                    server.update(serverInfo);
                }
            }
        }.runTask(getPlugin());
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Block block = event.getClickedBlock();

        for (Portal portal : _portals)
        {
            if (!portal.isSign(block))
                continue;

            portal.openMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        for (Player player : UtilPlayer.getPlayers())
        {
            Location loc = player.getLocation();

            for (Portal portal : _portals)
            {
                if (!portal.isInside(loc))
                    continue;

                portal.activate(player);
            }
        }
    }

    private void removeServer(String serverName)
    {
        for (ArrayList<Server> servers : _runningServers.values())
        {
            servers.removeIf(new Predicate<Server>()
            {
                public boolean test(Server server)
                {
                    return server.getName().equals(serverName);
                }
            });
        }
    }

}
