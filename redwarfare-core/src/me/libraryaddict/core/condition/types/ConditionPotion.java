package me.libraryaddict.core.condition.types;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.core.damage.AttackType;

public class ConditionPotion extends Condition
{

    public ConditionPotion(PotionEffectType type, Entity entity, AttackType attackType, Entity cause, int expires)
    {
        super(type.getName(), entity, attackType, cause, expires);
    }

    @Override
    public void remove()
    {
        ((LivingEntity) getVictim()).removePotionEffect(PotionEffectType.getByName(getName()));
    }

}
