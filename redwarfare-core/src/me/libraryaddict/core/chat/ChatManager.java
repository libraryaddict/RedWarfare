package me.libraryaddict.core.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.chat.redis.RedisListenerChat;
import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.network.PlayerData;

public class ChatManager extends MiniPlugin
{
    private ArrayList<ChatChannel> _channels = new ArrayList<ChatChannel>();
    private HashMap<UUID, Message> _messages = new HashMap<UUID, Message>();
    private PlayerDataManager _playerManager;
    private RankManager _rankManager;
    private ServerManager _serverManager;
    private Vector<String> _siteExceptions = new Vector<String>();
    private Vector<Pattern> _siteFilters = new Vector<Pattern>();

    public ChatManager(JavaPlugin plugin, RankManager rankManager, PlayerDataManager playerManager, ServerManager serverManager)
    {
        super(plugin, "Chat Manager");

        _rankManager = rankManager;
        _playerManager = playerManager;
        _rankManager = rankManager;
        _serverManager = serverManager;

        _siteExceptions.addAll(Arrays.asList(new String[]
            {
                    "www.redwarfare.com", "play.redwarfare.com", "redwarfa.re", "shop.redwarfare.com", "redwarfare.com",
                    "www.redwarfare"
            }));

        _siteFilters.add(Pattern.compile(
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])"));
        _siteFilters.add(Pattern.compile(
                "(http://)?(www)?\\S{2,}((\\.com)|(\\.net)|(\\.org)|(\\.co\\.uk)|(\\.tk)|(\\.info)|(\\.es)|(\\.de)|(\\.arpa)|(\\.edu)|(\\.firm)|(\\.int)|(\\.mil)|(\\.mobi)|(\\.nato)|(\\.to)|(\\.fr)|(\\.ms)|(\\.vu)|(\\.eu)|(\\.nl)|(\\.ly))"));
        _siteFilters.add(Pattern.compile(
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\,([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\,([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\,([01]?\\d\\d?|2[0-4]\\d|25[0-5])"));
        _siteFilters.add(Pattern.compile(
                "(http://)?(www)?\\S{2,}((\\,com)|(\\,net)|(\\,org)|(\\,co\\,uk)|(\\,tk)|(\\,info)|(\\,es)|(\\,de)|(\\,arpa)|(\\,edu)|(\\,firm)|(\\,int)|(\\,mil)|(\\,mobi)|(\\,nato)|(\\,to)|(\\,fr)|(\\,ms)|(\\,vu)|(\\,eu)|(\\,nl)|(\\,ly))"));

        ChatChannel.init(this);

        new RedisListenerChat(this);
    }

    public void addChannel(ChatChannel channel)
    {
        ChatChannel temp;

        if ((temp = getChannel(channel.getName())) != null && temp.isValid())
        {
            throw new IllegalArgumentException("The ChatChannel " + channel.getName() + " is already registered!");
        }
        else if (temp != null)
        {
            _channels.remove(temp);
        }

        _channels.add(channel);
    }

    public ChatChannel getChannel(String name)
    {
        for (ChatChannel channel : _channels)
        {
            if (!channel.getName().equals(name))
                continue;

            return channel;
        }

        return null;
    }

    public PlayerData getData(Player player)
    {
        return _playerManager.getData(player);
    }

    public ServerManager getServerManager()
    {
        return _serverManager;
    }

    public boolean isSpamming(Player player, String chat)
    {
        PlayerData data = getData(player);

        if (data.isMuted())
        {
            player.sendMessage(data.getMuted());
            return true;
        }

        if (!_messages.containsKey(player.getUniqueId()))
            _messages.put(player.getUniqueId(), new Message());

        Message message = _messages.get(player.getUniqueId());

        if (message.isSpamming(chat))
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    player.kickPlayer("Stop spamming");
                }
            }.runTaskLater(getPlugin(), 5);

            return true;
        }

        if (message.isAlmostSpamming(chat))
        {
            UtilPlayer.sendMessage(player,
                    C.DRed + C.Bold + "WARNING " + C.Gray + "You are spamming, cease and desist or you will be kicked");

            return true;
        }

        for (Pattern pattern : _siteFilters)
        {
            String msg = chat.toLowerCase();

            for (String s : _siteExceptions)
            {
                msg = msg.replace(s, "");
            }

            Matcher m = pattern.matcher(msg);

            if (!m.find())
            {
                continue;
            }

            UtilPlayer.sendMessage(player, C.DRed + C.Bold + "WARNING " + C.Gray + "Do not advertise or you will be banned");

            return true;
        }

        if (message.isFast())
        {
            UtilPlayer.sendMessage(player, C.DRed + C.Bold + "WARNING " + C.Gray + "You are talking too fast");

            return true;
        }

        return false;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        event.setCancelled(true);

        new BukkitRunnable()
        {
            public void run()
            {
                Player chatter = event.getPlayer();

                if (!_messages.containsKey(chatter.getUniqueId()))
                    _messages.put(chatter.getUniqueId(), new Message());

                if (!_rankManager.getRank(chatter).hasRank(Rank.MOD) && isSpamming(chatter, event.getMessage()))
                {
                    _messages.get(chatter.getUniqueId()).addMessage(event.getMessage());

                    return;
                }

                _messages.get(chatter.getUniqueId()).addMessage(event.getMessage());

                ChatEvent newEvent = new ChatEvent(chatter, _rankManager.getDisplayedRank(chatter), new String[]
                    {
                            event.getMessage(), event.getMessage()
                    });

                for (Player ignorer : UtilPlayer.getPlayers())
                {
                    PlayerData data = getData(ignorer);

                    if (data == null || !data.getIgnoring().contains(chatter.getUniqueId()))
                        continue;

                    newEvent.getRecipients().remove(ignorer);
                }

                Bukkit.getPluginManager().callEvent(newEvent);

                if (newEvent.isCancelled())
                    return;

                Bukkit.getLogger().log(Level.INFO, newEvent.getFinalUncensored());

                for (Player receiver : newEvent.getRecipients())
                {
                    if (receiver == newEvent.getPlayer())
                    {
                        receiver.sendMessage(newEvent.getFinalUncensored());
                    }
                    else
                    {
                        receiver.sendMessage(newEvent.getFinalCensored());
                    }
                }
            }
        }.runTask(getPlugin());
    }

    public void onChat(ChatMessage message)
    {
        ChatChannel channel = getChannel(message.getChannel());

        if (channel == null)
            return;

        if (!channel.isValid())
        {
            _channels.remove(channel);
            return;
        }

        if (!channel.isCrossServer())
            return;

        if (ServerManager.getServerName().equals(message.getServer()))
            return;

        channel.onMessage(message);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event)
    {
        _messages.remove(event.getPlayer().getUniqueId());

        Iterator<ChatChannel> itel = _channels.iterator();

        while (itel.hasNext())
        {
            ChatChannel entry = itel.next();

            if (entry.isValid())
                continue;

            itel.remove();
        }
    }

    public void removeChannel(ChatChannel channel)
    {
        _channels.remove(channel);
    }

}
