package me.libraryaddict.core.player.types;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.mysql.operations.MysqlSaveOwned;
import me.libraryaddict.network.PlayerOwned;

public class Owned
{
    private static PlayerDataManager _dataManager;

    private static PlayerOwned get(Player player)
    {
        return _dataManager.getOwned(player);
    }

    public static boolean has(Player player, String item)
    {
        return get(player).has(item);
    }

    public static void init(PlayerDataManager dataManager)
    {
        _dataManager = dataManager;
    }

    public static void setOwned(Player player, String item)
    {
        PlayerOwned owned = get(player);

        if (owned.has(item))
            return;

        owned.add(item);

        PlayerOwned save = owned.clone();

        new BukkitRunnable()
        {
            public void run()
            {
                new MysqlSaveOwned(save);
            }
        }.runTaskAsynchronously(_dataManager.getPlugin());
    }
}
