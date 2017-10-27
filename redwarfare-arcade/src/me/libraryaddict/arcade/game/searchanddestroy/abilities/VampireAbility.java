package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.game.searchanddestroy.KillstreakEvent;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.utils.UtilEnt;

public class VampireAbility extends Ability
{
    @EventHandler
    public void onKillstreak(KillstreakEvent event)
    {
        DeathEvent newEvent = event.getDeath();
        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!isAlive(player))
            return;

        if (newEvent.getLastAttacker() == event.getPlayer())
        {
            double toAdd = Math.min(40 - player.getMaxHealth(), 2);

            if (toAdd <= 0)
                return;

            player.setMaxHealth(player.getMaxHealth() + toAdd);

            player.sendMessage(C.Gray + "You gained " + C.Green + (int) (toAdd / 2) + " Hearts" + C.Gray + " for killing "
                    + C.Yellow + newEvent.getPlayer().getName());

            ConditionManager.addPotion(new PotionEffect(PotionEffectType.REGENERATION, 15 * 20, 0), player, false);
        }
        else
        {
            double toAdd = Math.min(40 - player.getMaxHealth(), 1);

            if (toAdd <= 0)
                return;

            player.setMaxHealth(player.getMaxHealth() + toAdd);

            player.sendMessage(C.Gray + "You gained " + C.Green + (int) (toAdd / 2) + " Heart" + C.Gray + " for assisting "
                    + C.Yellow + UtilEnt.getName(newEvent.getLastAttacker()) + " in killing " + C.Yellow
                    + newEvent.getPlayer().getName());

            ConditionManager.addPotion(new PotionEffect(PotionEffectType.REGENERATION, 15 * 10, 0), player, false);
        }
    }

    @Override
    public void registerAbility()
    {
        for (Player player : getPlayers(true))
        {
            player.setMaxHealth(16);
        }
    }
}
