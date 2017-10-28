package me.libraryaddict.bungee;

import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.bungee.mysql.MysqlSavePlayerHistory;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.mysql.operations.MysqlFetchBanInfo;
import me.libraryaddict.mysql.operations.MysqlLoadPlayerData;
import me.libraryaddict.network.BungeeSettings;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.redis.operations.RedisDeletePlayerData;
import me.libraryaddict.redis.operations.RedisSavePlayerData;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener
{
    private BungeeManager _bungeeManager;

    public BungeeListener(BungeeManager bungeeManager)
    {
        _bungeeManager = bungeeManager;
    }

    public BungeeManager getBungee()
    {
        return _bungeeManager;
    }

    public BungeeSettings getSettings()
    {
        return getBungee().getSettings();
    }

    // @EventHandler
    public void onConnect(ServerConnectedEvent event)
    {
        // getBungee().notifyPlayerData(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PostLoginEvent event)
    {
        event.getPlayer().setTabHeader(new ComponentBuilder(getSettings().getHeader()).create(),
                new ComponentBuilder(getSettings().getFooter()).create());
    }

    @EventHandler
    public void onKick(ServerKickEvent event)
    {
        String kickedFrom = event.getKickedFrom().getName();
        String hub = getBungee().getHub();

        if (hub == null || getBungee().isHub(kickedFrom))
        {
            return;
        }

        ServerInfo target = ProxyServer.getInstance().getServerInfo(hub.toLowerCase());

        event.setCancelled(true);
        event.setCancelServer(target);

        event.getPlayer().sendMessage(event.getKickReasonComponent());
    }

    @EventHandler
    public void onLobby(ServerConnectEvent event)
    {
        if (!event.getTarget().getName().equalsIgnoreCase("Hub"))
            return;

        String hub = getBungee().getHub();

        if (hub == null)
        {
            event.setCancelled(true);
            event.getPlayer().disconnect("No hub to send you to!");
            return;
        }

        ServerInfo target = ProxyServer.getInstance().getServerInfo(hub.toLowerCase());

        event.setTarget(target);
    }

    @EventHandler
    public void onLogin(LoginEvent event)
    {
        event.setCancelled(true);
        event.setCancelReason(UtilError.format("Error processing connection"));

        PendingConnection connection = event.getConnection();

        event.registerIntent(getBungee().getPlugin());

        new Thread()
        {
            public void run()
            {
                String message = processConnection(connection.getUniqueId(),
                        connection.getAddress().getAddress().getHostAddress(), connection.getName());

                if (message != null)
                {
                    event.setCancelled(true);
                    event.setCancelReason(message);
                }
                else
                {
                    event.setCancelled(false);
                }

                event.completeIntent(getBungee().getPlugin());
            }
        }.start();
    }

    @EventHandler
    public void onPing(ProxyPingEvent event)
    {
        ServerPing ping = new ServerPing();
        ServerPing orig = event.getResponse();

        Protocol protocol;

        BungeeSettings settings = getSettings();

        if (settings.getProtocol().isEmpty())
        {
            //protocol = new Protocol("1.10.2", 210);
             protocol = orig.getVersion();
        }
        else
        {
            protocol = new Protocol(settings.getProtocol(), 0);
        }

        PlayerInfo[] info = new ServerPing.PlayerInfo[settings.getPlayers().size()];

        for (int i = 0; i < info.length; i++)
        {
            info[i] = new ServerPing.PlayerInfo(settings.getPlayers().get(i), UUID.randomUUID());
        }

        Players players = new ServerPing.Players(settings.getMaxPlayers(), getBungee().getTotalPlayers(), info);

        ping.setPlayers(players);
        ping.setVersion(protocol);
        ping.setFavicon(getBungee().getFavicon());

        String motd = UtilMath.r(getSettings().getMotd());

        if (motd == null)
            motd = "No motd set";

        ping.setDescription(motd);

        event.setResponse(ping);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event)
    {
        ProxiedPlayer proxyPlayer = event.getPlayer();

        // getBungee().notifyPlayerData(proxyPlayer);

        String name = proxyPlayer.getName();
        UUID uuid = proxyPlayer.getUniqueId();
        String ip = proxyPlayer.getAddress().getAddress().getHostAddress();

        new Thread()
        {
            public void run()
            {
                /*RedisFetchPlayerData playerData = new RedisFetchPlayerData(uuid);

                if (playerData.isSuccess() && playerData.getPlayerData() != null)
                {
                    new MysqlSavePlayerData(playerData.getPlayerData());
                }*/

                new MysqlSavePlayerHistory(uuid, name, ip);

                new RedisDeletePlayerData(uuid);
            }
        }.start();
    }

    public String processConnection(UUID uuid, String ip, String name)
    {
        MysqlFetchBanInfo fetchBan = new MysqlFetchBanInfo(uuid.toString());

        if (!fetchBan.isSuccess())
        {
            return UtilError.format("Error connecting to database 1");
        }

        String banMessage = fetchBan.isBanned();

        if (banMessage == null)
        {
            MysqlFetchBanInfo fetchBan2 = new MysqlFetchBanInfo(ip);

            if (!fetchBan2.isSuccess())
            {
                return UtilError.format("Error connecting to database 2");
            }

            banMessage = fetchBan2.isBanned();
        }

        if (banMessage != null)
            return banMessage;

        MysqlLoadPlayerData loadData = new MysqlLoadPlayerData(uuid);

        if (!loadData.isSuccess())
        {
            return UtilError.format("Error connecting to database 3");
        }

        PlayerData data = loadData.getPlayerData();

        if (getSettings().isWhitelist())
        {
            HashMap<Integer, Long> ranks = data.getOwnedRanks();

            if (!ranks.containsKey(KeyMappings.getKey(Rank.OWNER.name()))
                    && !ranks.containsKey(KeyMappings.getKey(Rank.ADMIN.name()))
                    && !ranks.containsKey(KeyMappings.getKey(Rank.MOD.name())))
            {
                return UtilError.format("Whitelist", "You are not authorized to join the server!");
            }
        }

        Pair<Pair<String, String>, Long> mute = fetchBan.isMuted();

        if (mute != null)
        {
            data.setMute(mute.getKey().getKey(), mute.getKey().getValue(), mute.getValue());
        }

        RedisSavePlayerData savePlayer = new RedisSavePlayerData(data, false);

        if (!savePlayer.isSuccess())
        {
            return UtilError.format("Error connecting to database 4");
        }

        MysqlSavePlayerHistory saveHistory = new MysqlSavePlayerHistory(uuid, name, ip);

        if (!saveHistory.isSuccess())
        {
            new RedisDeletePlayerData(uuid);

            return UtilError.format("Error connecting to database 5");
        }

        return null;
    }
}
