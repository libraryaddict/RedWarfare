package me.libraryaddict.core.ranks.redis;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisRankListener extends JedisPubSub
{
    private RankManager _rankManager;

    public RedisRankListener(RankManager rankManager)
    {
        _rankManager = rankManager;

        new BukkitRunnable()
        {
            public void run()
            {
                RedisManager.addListener(RedisRankListener.this, RedisKey.NOTIFY_RANK_UPDATE);
            }
        }.runTaskAsynchronously(rankManager.getPlugin());
    }

    @Override
    public void onMessage(String channel, String message)
    {
        String[] split = message.split(":");

        new BukkitRunnable()
        {
            public void run()
            {
                Player player = Bukkit.getPlayer(UUID.fromString(split[0]));

                if (player == null)
                    return;

                _rankManager.setRank(player, Rank.valueOf(split[1]), Long.parseLong(split[2]));
            }
        }.runTask(_rankManager.getPlugin());
    }
}
