package me.libraryaddict.core.combat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;

public class CombatManager extends MiniPlugin
{
    private HashMap<Entity, CombatLog> _combatLog = new HashMap<Entity, CombatLog>();

    public CombatManager(JavaPlugin plugin)
    {
        super(plugin, "Combat Manager");
    }

    public CombatLog getCreateCombatLog(Entity entity)
    {
        if (!_combatLog.containsKey(entity))
            _combatLog.put(entity, new CombatLog());

        CombatLog combatLog = _combatLog.get(entity);

        combatLog.validate(entity);

        return combatLog;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        _combatLog.remove(event.getPlayer());
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        Iterator<Entry<Entity, CombatLog>> itel = _combatLog.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<Entity, CombatLog> entry = itel.next();

            Entity entity = entry.getKey();
            CombatLog log = entry.getValue();

            if (entity instanceof Player ? !((Player) entity).isOnline() : !entity.isValid() && entity.isDead())
            {
                if (log.isValid())
                {
                    continue;
                }

                itel.remove();
                continue;
            }

            log.setValid();
        }
    }

}
