package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilParticle;

public class BleederAbility extends Ability
{
    @EventHandler
    public void onDamage(CustomDamageEvent event)
    {
        if (!hasAbility(event.getPlayerDamager()))
            return;

        if (!event.getAttackType().isMelee())
            return;

        event.setInitDamage("Cancel Damage", 0);
        event.addDamage(DamageMod.CUSTOM.getSubMod("Bleeder"), 2 + UtilMath.rr(1));

        event.addRunnable(new DamageRunnable("Bleeder")
        {
            @Override
            public boolean isPreDamage()
            {
                return false;
            }

            @Override
            public void run(CustomDamageEvent event2)
            {
                if (!isAlive(event2.getDamagee()))
                    return;

                if (!event2.isLivingDamagee())
                    return;

                LivingEntity ent = event2.getLivingDamagee();

                ent.setMaxHealth(Math.max(0.5, ent.getMaxHealth() - event2.getDamage()));

                for (int i = 0; i < 8; i++)
                {
                    UtilParticle.playParticle(ent.getLocation().add(UtilMath.rr(0.3), 1 + UtilMath.rr(0.5), UtilMath.rr(0.3)),
                            ParticleColor.DARK_RED);
                }
            }
        });
    }
}
