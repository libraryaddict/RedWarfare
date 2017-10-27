package me.libraryaddict.core.chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import me.libraryaddict.core.C;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilPlayer;

public class ChatEvent extends Event implements Cancellable
{
    private static class ChannelGlobal extends ChatChannel
    {
        private ArrayList<Player> _players = UtilPlayer.getPlayers();

        public ChannelGlobal()
        {
            super("Online Players");
        }

        @Override
        public ArrayList<Player> getReceivers()
        {
            return _players;
        }

        @Override
        public boolean isCrossServer()
        {
            return false;
        }

        @Override
        public boolean isValid()
        {
            return true;
        }
    }
    private static ChannelGlobal _defaultChannel;

    private static final HandlerList _handlers = new HandlerList();

    static
    {
        _defaultChannel = new ChannelGlobal();
    }

    public static HandlerList getHandlerList()
    {
        return _handlers;
    }

    private boolean _cancelled;
    private ChatChannel _channel;
    private String _displayName;
    private ArrayList<Player> _listeners = UtilPlayer.getPlayers();
    private String[] _message;
    private Player _player;
    private String _prefix = "";
    private Rank _rank;
    private String _rankPrefix = "";

    public ChatEvent(Player player, Rank rank, String[] message)
    {
        _player = player;
        _rank = rank;
        _message = message;

        _displayName = player.getName();

        if (getRank() != Rank.ALL)
        {
            setRankPrefix("[" + getRank().getPrefix() + getRank().getName() + C.Reset + "] ");
        }

        setChannel(_defaultChannel);
    }

    public ChatChannel getChannel()
    {
        return _channel;
    }

    public String getDisplayName()
    {
        return _displayName;
    }

    /*  public void addChannel(ChatChannel channel)
    {
        if (_channels.containsKey(channel.getName()))
        {
            throw new IllegalArgumentException("The channel '" + channel.getName() + "' has already been registered!");
        }
    
        _channels.put(channel.getName(), channel);
    }
    
    public void removeChannel(String channelName)
    {
        _channels.remove(channelName);
    }
    
    public ChatChannel getChannel(String channel)
    {
        return _channels.get(channel);
    }*/

    public String getFinalCensored()
    {
        return getPrefix() + _rankPrefix + getDisplayName() + C.White + ": " + getMessage()[0];
    }

    public String getFinalUncensored()
    {
        return getPrefix() + _rankPrefix + getDisplayName() + C.White + ": " + getMessage()[1];
    }

    @Override
    public HandlerList getHandlers()
    {
        return _handlers;
    }

    public String[] getMessage()
    {
        return _message;
    }

    public Player getPlayer()
    {
        return _player;
    }

    public String getPrefix()
    {
        return _prefix;
    }

    public Rank getRank()
    {
        return _rank;
    }

    public ArrayList<Player> getRecipients()
    {
        return _listeners;
    }

    @Override
    public boolean isCancelled()
    {
        return _cancelled;
    }

    public void removeFirstLetter()
    {
        _message = new String[]
            {
                    getMessage()[0].substring(1).trim(), getMessage()[1].substring(1).trim()
            };
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        _cancelled = cancelled;
    }

    public void setChannel(ChatChannel channel)
    {
        _channel = channel;
    }

    public void setDisplayName(String newName)
    {
        _displayName = newName;
    }

    public void setMessage(String[] censored)
    {
        _message = censored;
    }

    public void setPrefix(String newPrefix)
    {
        _prefix = newPrefix;
    }

    public void setRankPrefix(String newPrefix)
    {
        _rankPrefix = newPrefix;
    }
}
