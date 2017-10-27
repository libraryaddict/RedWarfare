package me.libraryaddict.core.cosmetics.types;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.cosmetics.Cosmetic;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;

public class CosmeticCloud extends Cosmetic
{
    private int _tick;

    public CosmeticCloud()
    {
        super("Cloud");
    }

    @Override
    public String[] getDescription()
    {
        return new String[]
            {
                    "Everywhere you walk a cloud follows you, ruining your day!"
            };
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemStack(Material.QUARTZ);
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        _tick++;

        for (Player player : getPlayers())
        {
            UtilParticle.playParticle(ParticleType.CLOUD, player.getLocation().add(0, 2.8, 0), 0.7, 0.2, 0.7, 10);
            UtilParticle.playParticle(ParticleType.DRIP_WATER, player.getLocation().add(0, 2.8, 0), 0.6, 0.2, 0.6, 1);

            if (_tick % 15 != 0)
                continue;

            player.getWorld().playSound(player.getLocation(), Sound.WEATHER_RAIN, 2, 1);
        }
    }

}
