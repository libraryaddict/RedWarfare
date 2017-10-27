package me.libraryaddict.core.player.database;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.network.DatabaseModifyCurrency;
import me.libraryaddict.redis.RedisKey;
import me.libraryaddict.redis.RedisManager;
import redis.clients.jedis.JedisPubSub;

public class CurrencyTrackerListener extends JedisPubSub
{
    private HashMap<UUID, HashMap<CurrencyType, Long>> _mappings = new HashMap<UUID, HashMap<CurrencyType, Long>>();
    private JavaPlugin _plugin;

    public CurrencyTrackerListener(JavaPlugin plugin)
    {
        _plugin = plugin;
    }

    public void addListener()
    {
        RedisManager.addListener(this, RedisKey.NOTIFY_CURRENCY);
    }

    public void addOwned(UUID uuid, CurrencyType item, String reason, long value, boolean notifyRedis)
    {
        if (value == 0)
            return;

        synchronized (_mappings)
        {
            if (_mappings.containsKey(uuid))
            {
                _mappings.get(uuid).put(item, _mappings.get(uuid).getOrDefault(item, 0L) + value);
            }
        }

        new DatabaseModifyCurrency(uuid, item.name(), reason, value, notifyRedis);
    }

    public long getOwned(UUID uuid, CurrencyType item)
    {
        synchronized (_mappings)
        {
            if (!_mappings.containsKey(uuid))
                throw new RuntimeException("Player " + uuid + " isn't loaded");

            HashMap<CurrencyType, Long> mappings = _mappings.get(uuid);

            if (!mappings.containsKey(item))
                return 0;

            return mappings.get(item);
        }
    }

    public void loadOwned(UUID uuid, HashMap<String, Long> hashmap)
    {
        HashMap<CurrencyType, Long> mappings = new HashMap<CurrencyType, Long>();

        for (Entry<String, Long> entry : hashmap.entrySet())
        {
            mappings.put(CurrencyType.valueOf(entry.getKey()), entry.getValue());
        }

        synchronized (_mappings)
        {
            _mappings.put(uuid, mappings);
        }
    }

    @Override
    public void onMessage(String channel, String message)
    {
        String[] split = message.split(":");
        UUID uuid = UUID.fromString(split[0]);
        CurrencyType item = CurrencyType.valueOf(split[1]);
        long amount = Long.parseLong(split[2]);

        synchronized (_mappings)
        {
            if (!_mappings.containsKey(uuid))
                return;

            HashMap<CurrencyType, Long> mappings = _mappings.get(uuid);

            mappings.put(item, mappings.getOrDefault(item, 0L) + amount);

            new BukkitRunnable()
            {
                public void run()
                {
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null)
                        return;

                    player.sendMessage(C.Gold + "Given " + amount + " " + item.getName() + (amount == 1L ? "" : "s"));
                }
            }.runTask(_plugin);
        }
    }

    public void setOwned(UUID uuid, CurrencyType item, long value)
    {
        synchronized (_mappings)
        {
            throw new UnsupportedOperationException("Not supported");
        }
    }

    public void unloadOwned(UUID uuid)
    {
        synchronized (_mappings)
        {
            _mappings.remove(uuid);
        }
    }

}
