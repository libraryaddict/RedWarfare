package me.libraryaddict.core.chat.redis;

import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;

import me.libraryaddict.core.chat.ChatManager;
import me.libraryaddict.core.chat.ChatMessage;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerChat extends JedisPubSub
{
    public ChatManager _chatManager;

    public RedisListenerChat(ChatManager chatManager)
    {
        _chatManager = chatManager;

        new BukkitRunnable()
        {
            public void run()
            {
                RedisManager.addListener(RedisListenerChat.this, RedisKey.NOTIFY_CHAT);
            }
        }.runTaskAsynchronously(_chatManager.getPlugin());
    }

    public void onMessage(String channel, String message)
    {
        ChatMessage chat = new Gson().fromJson(message, ChatMessage.class);
        
        new BukkitRunnable()
        {
            public void run()
            {
                _chatManager.onChat(chat);
            }
        }.runTask(_chatManager.getPlugin());
    }
}
