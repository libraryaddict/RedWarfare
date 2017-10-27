package me.libraryaddict.core.player.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;

import me.libraryaddict.core.C;
import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.player.types.Mute;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class RedisListenerMutePlayer extends JedisPubSub
{
    private PlayerDataManager _playerManager;

    public RedisListenerMutePlayer(PlayerDataManager manager)
    {
        _playerManager = manager;

        RedisManager.addListener(this, RedisKey.NOTIFY_MUTE);
    }

    @Override
    public void onMessage(String channel, String message)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                Mute mute = new Gson().fromJson(message, Mute.class);

                Player player = Bukkit.getPlayer(mute.getMuted());

                if (player == null)
                    return;

                PlayerData data = _playerManager.getData(player);

                boolean previouslyMuted = data.isMuted();

                data.setMute(mute.getMuter(), mute.getReason(), mute.getExpires());
                data.save();

                if (!data.isMuted())
                {
                    if (previouslyMuted)
                    {
                        player.sendMessage(C.Blue + "You have been unmuted");

                        return;
                    }
                }

                player.sendMessage(data.getMuted());
            }
        }.runTask(_playerManager.getPlugin());
    }

}
