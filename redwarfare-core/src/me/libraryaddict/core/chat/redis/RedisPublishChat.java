package me.libraryaddict.core.chat.redis;

import com.google.gson.Gson;

import me.libraryaddict.core.chat.ChatMessage;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.redis.RedisKey;
import redis.clients.jedis.Jedis;

public class RedisPublishChat extends DatabaseOperation
{
    public RedisPublishChat(ChatMessage message)
    {
        try (Jedis redis = getRedis())
        {
            redis.publish(RedisKey.NOTIFY_CHAT.getKey(), new Gson().toJson(message));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
