package me.libraryaddict.core.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import me.libraryaddict.core.condition.types.Condition;
import me.libraryaddict.core.condition.types.ConditionFall;
import me.libraryaddict.core.condition.types.ConditionFire;
import me.libraryaddict.core.condition.types.ConditionPotion;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilEnt;

public class ConditionManager extends MiniPlugin
{
    private static HashMap<Entity, ArrayList<Condition>> _conditions = new HashMap<Entity, ArrayList<Condition>>();

    public static void addCondition(Entity entity, Condition condition)
    {
        addCondition(entity, condition, true);
    }

    public static boolean addCondition(Entity entity, Condition condition, boolean extend)
    {
        ConditionEvent event = new ConditionEvent(entity, condition);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return false;

        if (!extend)
        {
            removeCondition(entity, condition.getName());
        }

        if (!_conditions.containsKey(entity))
        {
            _conditions.put(entity, new ArrayList<Condition>());
        }

        _conditions.get(entity).add(0, condition);

        return true;
    }

    public static void addFall(Entity victim, Entity cause)
    {
        if (cause == null)
            return;

        addCondition(victim, new ConditionFall(victim, cause), false);
    }

    public static void addPotion(PotionEffect effect, Entity entity, boolean extend)
    {
        addPotion(effect, entity, null, extend);
    }

    public static void addPotion(PotionEffect effect, Entity entity, Entity cause, boolean extend)
    {
        if (!(entity instanceof LivingEntity))
            return;

        ConditionPotion condition = new ConditionPotion(effect.getType(), entity, null, cause, effect.getDuration());

        if (!addCondition(entity, condition, extend))
            return;

        if (extend)
        {
            PotionEffect oldEffect = UtilEnt.getPotion(entity, effect.getType());

            if (oldEffect != null)
            {
                effect = new PotionEffect(effect.getType(), effect.getDuration() + oldEffect.getDuration(),
                        Math.max(effect.getAmplifier(), oldEffect.getAmplifier()), effect.isAmbient(), effect.hasParticles(),
                        effect.getColor());
            }
        }

        ((LivingEntity) entity).addPotionEffect(effect, true);
    }

    public static void clearConditions(Entity entity)
    {
        _conditions.remove(entity);
    }

    public static Condition getCondition(Entity entity, Class<? extends Condition> conditionClass)
    {
        if (!_conditions.containsKey(entity))
            return null;

        for (Condition condition : _conditions.get(entity))
        {
            if (condition.hasExpired())
                continue;

            if (!conditionClass.isAssignableFrom(condition.getClass()))
            {
                continue;
            }

            return condition;
        }

        return null;
    }

    public static Condition getCondition(Entity entity, String conditionName)
    {
        if (!_conditions.containsKey(entity))
            return null;

        for (Condition condition : _conditions.get(entity))
        {
            if (condition.hasExpired())
                continue;

            if (condition.getName().equals(conditionName))
            {
                return condition;
            }
        }

        return null;
    }

    public static void removeCondition(Entity entity, String conditionName)
    {
        if (!_conditions.containsKey(entity))
            return;

        Iterator<Condition> itel = _conditions.get(entity).iterator();

        while (itel.hasNext())
        {
            Condition condition = itel.next();

            if (!condition.getName().equals(conditionName))
            {
                continue;
            }

            itel.remove();
            condition.remove();
        }

        if (_conditions.get(entity).isEmpty())
        {
            _conditions.remove(entity);
        }
    }

    public static void setFire(Entity entity, AttackType attackType, Entity cause, int ticks, boolean extend)
    {
        if (entity instanceof LivingEntity)
        {
            int level = 0;

            for (ItemStack item : (entity instanceof Player ? ((Player) entity).getInventory().getArmorContents()
                    : ((LivingEntity) entity).getEquipment().getArmorContents()))
            {
                if (item == null)
                {
                    continue;
                }

                if (!item.containsEnchantment(Enchantment.PROTECTION_FIRE))
                    continue;

                level += item.getEnchantmentLevel(Enchantment.PROTECTION_FIRE);
            }

            if (level > 0)
            {
                ticks = (int) Math.ceil(ticks * ((Math.min(80, level * 15)) / 100D));
            }
        }

        Condition condition = new ConditionFire(entity, attackType, cause, ticks);

        if (!addCondition(entity, condition, extend))
            return;

        if (extend)
        {
            ticks += entity.getFireTicks();
        }

        entity.setFireTicks(ticks);
    }

    public static void setFire(Entity entity, AttackType attackType, int ticks, boolean extend)
    {
        setFire(entity, attackType, null, ticks, extend);
    }

    public ConditionManager(JavaPlugin plugin)
    {
        super(plugin, "Condition Manager");
    }

    public ArrayList<Condition> getConditions(Entity entity)
    {
        return _conditions.getOrDefault(entity, new ArrayList<Condition>());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(CustomDamageEvent event)
    {
        Entity entity = event.getDamagee();

        if (event.isIgnoreConditions() || !_conditions.containsKey(entity))
            return;

        AttackType type = event.getAttackType();

        if (type.isFall() && event.getDamager() == null)
        {
            Condition condition = getCondition(entity, "Fall");

            if (condition != null)
            {
                event.setDamager(condition.getCause());

                if (condition.getCause() instanceof Projectile)
                {
                    event.setAttackType(AttackType.FALL_SHOT);
                }
                else
                {
                    event.setAttackType(AttackType.FALL_PUSHED);
                }
            }
        }
        else if (type == AttackType.VOID && event.getDamager() == null)
        {
            Condition condition = getCondition(entity, "Fall");

            if (condition != null)
            {
                event.setDamager(condition.getCause());

                if (condition.getCause() instanceof Projectile)
                {
                    event.setAttackType(AttackType.VOID_SHOT);
                }
                else
                {
                    event.setAttackType(AttackType.VOID_PUSHED);
                }
            }
        }
        else if (type.isFire())
        {
            Condition condition = getCondition(entity, ConditionFire.class);

            if (condition != null)
            {
                if (event.getFinalDamager() == null)
                {
                    event.setRealDamager(condition.getCause());
                }

                event.setAttackType(condition.getAttackType());
            }
        }
        else if (type == AttackType.POISON || type == AttackType.WITHER_POISON)
        {
            for (Condition condition : getConditions(entity))
            {
                if (condition.hasExpired())
                    continue;

                if (!(condition instanceof ConditionPotion))
                    continue;

                if (!(condition.getName().equals("POISON") && type == AttackType.POISON)
                        && !(condition.getName().equals("WITHER") && type == AttackType.WITHER_POISON))
                    continue;

                if (event.getFinalDamager() == null)
                {
                    event.setRealDamager(condition.getCause());
                }
            }
        }
        else if (type == AttackType.SUICIDE || type == AttackType.SUICIDE_ASSISTED)
        {
            Condition condition = getCondition(entity, "Fall");

            if (condition != null && !UtilEnt.isGrounded(entity))
            {
                event.setDamager(condition.getCause());
            }
        }
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        Iterator<Entry<Entity, ArrayList<Condition>>> itel = _conditions.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<Entity, ArrayList<Condition>> entry = itel.next();

            Entity entity = entry.getKey();

            if (!entity.isValid() || entity.isDead() || (entity instanceof Player && !((Player) entity).isOnline()))
            {
                itel.remove();
                continue;
            }

            Iterator<Condition> itel2 = entry.getValue().iterator();
            ArrayList<String> looped = new ArrayList<String>();

            while (itel2.hasNext())
            {
                Condition condition = itel2.next();

                if (condition.hasExpired())
                {
                    itel2.remove();
                    continue;
                }

                if (looped.contains(condition.getName()))
                    continue;

                looped.add(condition.getName());

                condition.tickCondition();
            }

            if (entry.getValue().isEmpty())
                itel.remove();
        }
    }

}
