package me.libraryaddict.core.explosion;

import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageManager;
import me.libraryaddict.core.plugin.MiniPlugin;

public class ExplosionManager extends MiniPlugin
{
    public static ExplosionManager explosionManager;

    private DamageManager _damageManager;

    public ExplosionManager(JavaPlugin plugin, DamageManager damageManager)
    {
        super(plugin, "Explosion Manager");

        explosionManager = this;
        _damageManager = damageManager;
    }

    public DamageManager getDamageManager()
    {
        return _damageManager;
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event)
    {
        if (event.isLivingDamagee())
            return;

        if (!(event.getDamagee() instanceof Projectile))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event)
    {
        event.setCancelled(true);

        CustomExplosion explosion = new CustomExplosion(event.getLocation(), event.getYield(), AttackType.EXPLOSION);

        explosion.explode();
    }
}
