package me.libraryaddict.core.combat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilTime;

public class CombatLog
{
    private ArrayList<CombatEvent> _combatEvent = new ArrayList<CombatEvent>();
    private long _lastValid = System.currentTimeMillis();

    public void addEvent(CombatEvent event)
    {
        _combatEvent.add(0, event);
    }

    @Override
    public CombatLog clone()
    {
        CombatLog combatLog = new CombatLog();

        for (CombatEvent event : _combatEvent)
        {
            combatLog._combatEvent.add(event);
        }

        return combatLog;
    }

    public ArrayList<CombatEvent> getEvents()
    {
        return _combatEvent;
    }

    public Entity getKiller()
    {
        if (_combatEvent.isEmpty())
            return null;

        return getLastEvent().getEvent().getFinalDamager();
    }

    public CombatEvent getLastEvent()
    {
        if (!_combatEvent.isEmpty())
            return _combatEvent.get(0);

        return null;
    }

    public HashMap<Player, Double> getResponsibility()
    {
        HashMap<Player, Double> damage = new HashMap<Player, Double>();
        double totalDamage = 0;

        System.out.println("Calculating responsibility..");

        for (CombatEvent event : _combatEvent)
        {
            Entity ent = event.getEvent().getFinalDamager();

            if (!(ent instanceof Player))
            {
                continue;
            }

            double damageCauseByEvent = Math.min(event.getHealth(), event.getDamage());

            if (damageCauseByEvent <= 0)
                continue;

            System.out.println("Rewarded " + damageCauseByEvent + " damage to " + UtilEnt.getName(ent) + " for "
                    + (event.isCosmetic() ? "cosmetic " : "") + event.getEvent().getAttackType().getName() + " "
                    + UtilNumber.getTime(System.currentTimeMillis() - event.getWhen(), TimeUnit.MILLISECONDS) + " ago");

            totalDamage += damageCauseByEvent;

            damage.put((Player) ent, damageCauseByEvent + damage.getOrDefault(ent, 0D));
        }

        System.out.println("Finished calculating it!");

        HashMap<Player, Double> respon = new HashMap<Player, Double>();

        for (Entry<Player, Double> entry : damage.entrySet())
        {
            respon.put(entry.getKey(), entry.getValue() / totalDamage);
        }

        return respon;
    }

    public boolean isValid()
    {
        return !UtilTime.elasped(_lastValid, 2000);
    }

    public void setValid()
    {
        _lastValid = System.currentTimeMillis();
    }

    public void validate(Entity entity)
    {
        // If entity isn't standing on the ground, and he isn't allowed to fly, then return cos he's falling
        if (!UtilEnt.isGrounded(entity) && !(entity instanceof Player && ((Player) entity).getAllowFlight()))
        {
            System.out.println(UtilEnt.getName(entity) + " flying");
            return;
        }

        // If he can't be damaged
        if (!(entity instanceof Damageable))
        {
            System.out.println(UtilEnt.getName(entity) + " non-damageable");
            return;
        }

        Damageable damageable = (Damageable) entity;

        double healthUnaccountedFor = damageable.getMaxHealth() - damageable.getHealth();
        Iterator<CombatEvent> itel = _combatEvent.iterator();

        while (itel.hasNext())
        {
            CombatEvent event = itel.next();

            if (healthUnaccountedFor <= 0.01 && UtilTime.elasped(event.getWhen(), 10000))
            {
                itel.remove();
            }
            else if (!event.isCosmetic())
            {
                healthUnaccountedFor -= event.getRealDamage();
            }
        }
    }
}
