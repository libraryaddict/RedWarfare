package me.libraryaddict.core.condition.types;

import org.bukkit.entity.Entity;

import me.libraryaddict.core.damage.AttackType;

public class Condition
{
    private AttackType _attackType;
    private Entity _cause;
    private String _condition;
    private long _created = System.currentTimeMillis();
    private int _tickExpires;
    private Entity _victim;

    public Condition(String condition, Entity entity, AttackType attackType, Entity cause, int expires)
    {
        this(condition, entity, attackType, expires);

        _cause = cause;
    }

    public Condition(String condition, Entity entity, AttackType attackType, int expires)
    {
        this(condition, entity, expires);

        _attackType = attackType;
    }

    public Condition(String condition, Entity entity, int expires)
    {
        _condition = condition;
        _victim = entity;
        _tickExpires = expires;
    }

    public AttackType getAttackType()
    {
        return _attackType;
    }

    public Entity getCause()
    {
        return _cause;
    }

    public long getCreated()
    {
        return _created;
    }

    public String getName()
    {
        return _condition;
    }

    public Entity getVictim()
    {
        return _victim;
    }

    public boolean hasExpired()
    {
        return _tickExpires <= 0;
    }

    public void remove()
    {
    }

    public void tickCondition()
    {
        _tickExpires--;
    }

}
