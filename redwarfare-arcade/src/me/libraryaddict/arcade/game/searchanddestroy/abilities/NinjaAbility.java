package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.arcade.kits.Ability;

public class NinjaAbility extends Ability
{
    @Override
    public void registerAbility()
    {
        for (Player player : getPlayers(true))
        {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
        }
    }
}
