package me.libraryaddict.hub.managers;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.network.ServerInfo.ServerState;
import me.libraryaddict.redis.operations.RedisPublishServerInfo;

public class HubManager extends CentralManager {
    private EventManager _eventManager;
    private PlayerManager _playerManager;
    private PortalManager _portalManager;
    private RedeemNPCManager _redeemNPCManager;

    public HubManager(JavaPlugin plugin) {
        super(plugin);

        _portalManager = new PortalManager(plugin, getServer());
        _eventManager = new EventManager(plugin);
        _playerManager = new PlayerManager(plugin, this);
        _redeemNPCManager = new RedeemNPCManager(plugin, this);
        // new TitleManager(plugin);

        UtilPlayer.setDefaultGamemode(GameMode.ADVENTURE);

        getCosmetics().setEnabled(true);

        copyHub();

        new BukkitRunnable() {
            public void run() {
                new Hologram(new Location(Bukkit.getWorld("world"), 0, 81, 9), C.Gold + "Donators", C.Yellow + "TheAndroidMan",
                        C.Yellow + "NightWolfy").start();
            }
        }.runTaskLater(plugin, 10);
    }

    private void copyHub() {
        File hub = new File(UtilFile.getUpdateFolder(), "Hubs/Hub.zip");

        UtilFile.extractZip(hub, new File("world"));
    }

    public EventManager getEvent() {
        return _eventManager;
    }

    public PlayerManager getPlayer() {
        return _playerManager;
    }

    public PortalManager getPortal() {
        return _portalManager;
    }

    public RedeemNPCManager getRedeemNPC() {
        return _redeemNPCManager;
    }

    @EventHandler
    public void onSendInfo(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        if (!getServer().isPublic())
            return;

        ServerInfo serverInfo = new ServerInfo(ServerState.JOINABLE, ServerManager.getServerName(), ServerType.Hub,
                Bukkit.getOnlinePlayers().size(), 0, getServer().getIP() + ":" + Bukkit.getPort(), getServer().isUpdateReady());

        new BukkitRunnable() {
            public void run() {
                new RedisPublishServerInfo(serverInfo);
            }
        }.runTaskAsynchronously(getPlugin());
    }

}
