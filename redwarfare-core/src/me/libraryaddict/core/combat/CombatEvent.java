package me.libraryaddict.core.combat;

import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilTime;

public class CombatEvent
{
    private final boolean _cosmetic;
    private final double _damage;
    private final CustomDamageEvent _event;
    private final double _health;
    private final long _time = System.currentTimeMillis();

    public CombatEvent(CustomDamageEvent event, double damage, boolean cosmetic)
    {
        _damage = damage;
        _event = event;
        _cosmetic = cosmetic;

        if (event.isLivingDamagee())
        {
            _health = event.getLivingDamagee().getHealth();
        }
        else
        {
            _health = 0;
        }

        System.out.println("Added combat event for " + UtilEnt.getName(event.getDamagee()) + " for " + getDamage() + " damage, "
                + getRealDamage() + " real damage. Cosmetic: " + cosmetic + ", ticks: " + UtilTime.currentTick + " and damager: "
                + UtilEnt.getName(event.getDamager()) + ", attacktype: " + event.getAttackType().getName());
    }

    /* private CombatEvent()
    {
    }
    
    @Override
    public CombatEvent clone()
    {
        CombatEvent event = new CombatEvent();
        event._damage = _damage;
        event._event = _event;
        event._cosmetic = _cosmetic;
        event._health = _health;
        event._time = _time;
        event._health = _health;
        return event;
    }*/

    public double getDamage()
    {
        return getEvent().getDamage();
    }

    public CustomDamageEvent getEvent()
    {
        return _event;
    }

    public double getHealth()
    {
        return _health;
    }

    public double getRealDamage()
    {
        return _damage;
    }

    public long getWhen()
    {
        return _time;
    }

    public boolean isCosmetic()
    {
        return _cosmetic;
    }
}
