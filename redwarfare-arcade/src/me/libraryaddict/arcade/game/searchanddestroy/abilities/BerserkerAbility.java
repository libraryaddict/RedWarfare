package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;

public class BerserkerAbility extends Ability
{
    private DamageMod _berserker = new DamageMod("Berserker");

    public int getKills(Player player)
    {
        return (int) Math.floor(getGame().getKillstreak(player));
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event)
    {
        if (!event.isPlayerDamager() || !hasAbility(event.getPlayerDamager()))
            return;

        if (!isLive())
            return;

        Player player = event.getPlayerDamager();

        double damage = getKills(player) * 0.7;

        event.addDamage(_berserker, Math.min(7, damage));
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Player player : getPlayers(true))
        {
            int kills = getKills(player);

            if (kills <= 0)
                continue;

            UtilParticle.playParticle(ParticleType.RED_DUST, player.getLocation().add(0, 1, 0), 0.4, 1, 0.4, kills * 2);
        }
    }

}
