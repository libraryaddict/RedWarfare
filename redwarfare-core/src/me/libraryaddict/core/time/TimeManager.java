package me.libraryaddict.core.time;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilTime;

public class TimeManager extends MiniPlugin
{

    public TimeManager(JavaPlugin plugin)
    {
        super(plugin, "Time Manager");

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (TimeType timeType : TimeType.values())
                {
                    if (!timeType.isTime())
                        continue;

                    if (timeType == TimeType.TICK)
                    {
                        UtilTime.currentTick++;
                    }

                    Bukkit.getPluginManager().callEvent(new TimeEvent(timeType));
                }
            }
        }.runTaskTimer(plugin, 0, 0);

        new Recharge(plugin);
    }
}
