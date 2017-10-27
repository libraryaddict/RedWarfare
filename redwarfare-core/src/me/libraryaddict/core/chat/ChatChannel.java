package me.libraryaddict.core.chat;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.chat.redis.RedisPublishChat;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.network.PlayerData;

public abstract class ChatChannel
{
    private static ChatManager _chatManager;
    protected static void init(ChatManager manager)
    {
        _chatManager = manager;
    }

    private String _name;

    public ChatChannel(String name)
    {
        _name = name;
    }

    public void broadcast(String message)
    {
        handle(new ChatMessage(ServerManager.getServerName(), getName(), null, message, message));
    }

    public ChatMessage getMessage(ChatEvent event)
    {
        return new ChatMessage(ServerManager.getServerName(), getName(), event.getPlayer().getUniqueId(), event.getFinalUncensored(),
                event.getFinalCensored());
    }

    public String getName()
    {
        return _name;
    }

    public abstract ArrayList<Player> getReceivers();

    public void handle(ChatEvent event)
    {
        ChatMessage message = getMessage(event);

        handle(message);
    }

    public void handle(ChatMessage message)
    {
        onMessage(message);

        if (isCrossServer())
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    new RedisPublishChat(message);
                }
            }.runTaskAsynchronously(_chatManager.getPlugin());
        }
    }

    public abstract boolean isCrossServer();

    public abstract boolean isValid();

    public void onMessage(ChatMessage message)
    {
        System.out.println(message.getMessage());

        for (Player player : getReceivers())
        {
            if (message.hasPlayer())
            {
                PlayerData data = _chatManager.getData(player);

                if (data.getIgnoring().contains(message.getPlayer()))
                    continue;

                if (player.getUniqueId().equals(message.getPlayer()))
                {
                    player.sendMessage(message.getMessage());
                    continue;
                }
            }

            player.sendMessage(message.getCensored());
        }
    }
}
