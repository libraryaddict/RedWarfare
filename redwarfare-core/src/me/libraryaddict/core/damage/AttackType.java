package me.libraryaddict.core.damage;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;

public class AttackType
{
    public static AttackType CACTUS = new AttackType("Cactus", "%Killed% licked a cactus").setNoKnockback();

    public static AttackType CUSTOM = new AttackType("Custom", "%Killed% fell over and died").setNoKnockback();

    public static AttackType DAMAGE_POTION = new AttackType("Damage Potion",
            "%Killed% died to %Killer%'s damage potion which isn't part of the game?").setNoKnockback();

    public static AttackType DRAGON_BREATH = new AttackType("Dragon Breath", "%Killed% sucked a lungfull of dragon's breath")
            .setNoKnockback();

    public static AttackType DROWNED = new AttackType("Drowned", "%Killed% is swimming with the fishes").setIgnoreArmor()
            .setNoKnockback();

    public static AttackType EXPLOSION = new AttackType("Explosion", "%Killed% was caught in an explosion");

    public static AttackType FALL = new AttackType("Fall", "%Killed% fell to their death").setFall().setNoKnockback();

    public static AttackType FALL_PUSHED = new AttackType("Pushed Fall", "%Killed% was pushed to their death by %Killer%")
            .setFall().setNoKnockback();

    public static AttackType FALL_SHOT = new AttackType("Shot Fall", "%Killed% was shot by %Killer% and fell to their death")
            .setFall().setNoKnockback();

    public static AttackType FALLING_BLOCK = new AttackType("Falling Block", "%Killed% was crushed beneath a falling block");

    /**
     * Direct exposure to fire
     */
    public static AttackType FIRE = new AttackType("Direct Fire", "%Killed% stood inside flames and laughed").setNoKnockback()
            .setBurn();

    public static AttackType FIRE_ASPECT = new AttackType("Fire Aspect", "%Killed% was charred to a crisp by %Killer%").setFire()
            .setNoKnockback().setIgnoreArmor();

    public static AttackType FIRE_BOW = new AttackType("Fire Bow", "%Killed% charred to a crisp by %Killer%'s bow").setFire()
            .setNoKnockback().setIgnoreArmor();

    /**
     * Fire on the entity itself
     */
    public static AttackType FIRE_TICK = new AttackType("Fire Tick", "%Killed% was burned alive").setNoKnockback().setFire()
            .setIgnoreArmor();

    public static AttackType FISHING_HOOK = new AttackType("Fishing Hook", "%Killed% was killed by %Killer%'s... Fishing rod?");

    public static AttackType FLY_INTO_WALL = new AttackType("Flew into Wall", "%Killed% still hadn't gotten the hang of flying")
            .setNoKnockback();

    public static AttackType LAVA = new AttackType("Lava", "%Killed% tried to swim in lava").setNoKnockback().setBurn();

    public static AttackType LIGHTNING = new AttackType("Lightning", "%Killed% was electrified by lightning").setNoKnockback();

    public static AttackType MAGMA = new AttackType("Magma", "%Killed% took a rest on some hot magma").setNoKnockback().setBurn();

    public static AttackType MELEE = new AttackType("Melee", "%Killed% was murdered by %Killer%").setMelee();

    public static AttackType MELTING = new AttackType("Melting", "%Killed% melted in the hot sun").setNoKnockback();

    public static AttackType POISON = new AttackType("Poison", "%Killed% drank a flask of poison").setIgnoreArmor()
            .setNoKnockback();

    public static AttackType PROJECTILE = new AttackType("Projectile", "%Killed% was shot by %Killer%").setProjectile();

    public static AttackType QUIT = new AttackType("Quit", "%Killed% has left the game").setInstantDeath().setNoKnockback();

    public static AttackType STARVATION = new AttackType("Starvation", "%Killed% died from starvation").setIgnoreArmor()
            .setNoKnockback();

    public static AttackType SUFFOCATION = new AttackType("Suffocation", "%Killed% choked on block").setNoKnockback();

    public static AttackType SUICIDE = new AttackType("Suicide", "%Killed% commited suicide").setInstantDeath().setNoKnockback();

    public static AttackType SUICIDE_ASSISTED = new AttackType("Assisted Suicide", "%Killed% was assisted on the path to suicide")
            .setInstantDeath().setNoKnockback();

    public static AttackType THORNS = new AttackType("Thorns", "%Killed% found out how the thorns enchantment works")
            .setNoKnockback();

    public static AttackType UNKNOWN = new AttackType("Unknown", "%Killed% died from unknown causes").setNoKnockback();

    public static AttackType VOID = new AttackType("Void", "%Killed% fell into the void").setIgnoreArmor().setNoKnockback()
            .setIgnoreRate();

    public static AttackType VOID_PUSHED = new AttackType("Void Pushed", "%Killed% was knocked into the void by %Killer%")
            .setIgnoreArmor().setNoKnockback().setIgnoreRate();

    public static AttackType VOID_SHOT = new AttackType("Void Shot", "%Killed% was shot into the void by %Killer%")
            .setIgnoreArmor().setNoKnockback().setIgnoreRate();

    public static AttackType WITHER_POISON = new AttackType("Wither Poison", "%Killed% drank a vial of wither poison")
            .setNoKnockback();

    public static AttackType getAttack(DamageCause cause)
    {
        switch (cause)
        {
        case CONTACT:
            return CACTUS;
        case ENTITY_ATTACK:
            return MELEE;
        case PROJECTILE:
            return PROJECTILE;
        case SUFFOCATION:
            return SUFFOCATION;
        case FALL:
            return FALL;
        case FIRE:
            return FIRE;
        case FIRE_TICK:
            return FIRE_TICK;
        case MELTING:
            return MELTING;
        case LAVA:
            return LAVA;
        case DROWNING:
            return DROWNED;
        case BLOCK_EXPLOSION:
            return EXPLOSION;
        case ENTITY_EXPLOSION:
            return EXPLOSION;
        case VOID:
            return VOID;
        case LIGHTNING:
            return LIGHTNING;
        case SUICIDE:
            return SUICIDE;
        case STARVATION:
            return STARVATION;
        case POISON:
            return POISON;
        case MAGIC:
            return DAMAGE_POTION;
        case WITHER:
            return WITHER_POISON;
        case FALLING_BLOCK:
            return FALLING_BLOCK;
        case THORNS:
            return THORNS;
        case DRAGON_BREATH:
            return DRAGON_BREATH;
        case CUSTOM:
            return CUSTOM;
        case FLY_INTO_WALL:
            return FLY_INTO_WALL;
        case HOT_FLOOR:
            return MAGMA;
        default:
            return UNKNOWN;
        }
    }

    private boolean _burn;
    private String[] _deathMessages;
    private boolean _explosion;
    private boolean _fall;
    private boolean _fire;
    private boolean _ignoreArmor;
    private boolean _ignoreRate;
    private boolean _instantDeath;
    private boolean _isntKnockback;
    private boolean _melee;
    private String _name;
    private boolean _projectile;

    public AttackType(String name, String deathMessage1, String... deathMessages)
    {
        _name = name;
        _deathMessages = Arrays.copyOf(deathMessages, deathMessages.length + 1);
        _deathMessages[_deathMessages.length - 1] = deathMessage1;
    }

    public AttackType cloneData(AttackType attackType)
    {
        try
        {
            for (Field field : AttackType.class.getDeclaredFields())
            {
                field.setAccessible(true);

                if (field.getName().equals("_name") || field.getName().equals("_deathMessages"))
                    continue;

                field.set(this, field.get(attackType));
            }
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        return this;
    }

    public String getDeathMessage()
    {
        return UtilMath.r(_deathMessages);
    }

    public String getDeathMessage(String color, Entity victim, Entity killer, Entity cause)
    {
        return getDeathMessage(color, UtilEnt.getName(victim), UtilEnt.getName(killer), UtilEnt.getName(cause));
    }

    public String getDeathMessage(String color, String victim, String killer, String cause)
    {
        String message = color + getDeathMessage();

        message = replace(message, "%Killed%'s", victim + "'s");
        message = replace(message, "%Killed%", victim);
        message = replace(message, "%Cause%'s", cause + "'s");
        message = replace(message, "%Cause%", cause);
        message = replace(message, "%Killer%'s", killer + "'s");
        message = replace(message, "%Killer%", killer);

        return message;
    }

    public String getName()
    {
        return _name;
    }

    public boolean isBurn()
    {
        return _burn;
    }

    public boolean isExplosion()
    {
        return _explosion;
    }

    public boolean isFall()
    {
        return _fall;
    }

    public boolean isFire()
    {
        return _fire;
    }

    public boolean isIgnoreArmor()
    {
        return _ignoreArmor;
    }

    public boolean isIgnoreRate()
    {
        return _ignoreRate;
    }

    public boolean isInstantDeath()
    {
        return _instantDeath;
    }

    public boolean isKnockback()
    {
        return !_isntKnockback;
    }

    public boolean isMelee()
    {
        return _melee;
    }

    public boolean isProjectile()
    {
        return _projectile;
    }

    private String replace(String message, String find, String replace)
    {
        while (message.contains(find))
        {
            message = message.replaceFirst(find, replace + ChatColor.getLastColors(message.substring(0, message.indexOf(find))));
        }

        return message;
    }

    public AttackType setBurn()
    {
        _burn = true;

        return this;
    }

    public AttackType setExplosion()
    {
        _explosion = true;

        return this;
    }

    public AttackType setFall()
    {
        _fall = true;
        setIgnoreArmor();
        setIgnoreRate();

        return this;
    }

    public AttackType setFire()
    {
        setBurn();

        _fire = true;

        return this;
    }

    public AttackType setIgnoreArmor()
    {
        _ignoreArmor = true;

        return this;
    }

    public AttackType setIgnoreRate()
    {
        _ignoreRate = true;

        return this;
    }

    public AttackType setInstantDeath()
    {
        _instantDeath = true;

        return this;
    }

    public AttackType setMelee()
    {
        _melee = true;

        return this;
    }

    public AttackType setNoKnockback()
    {
        _isntKnockback = true;

        return this;
    }

    public AttackType setProjectile()
    {
        _projectile = true;

        return this;
    }

    public String toString()
    {
        return "AttackType[" + getName() + "]";
    }
}
