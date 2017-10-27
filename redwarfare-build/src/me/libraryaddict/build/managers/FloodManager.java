package me.libraryaddict.build.managers;

import me.libraryaddict.build.events.WorldInfoUnloadEvent;
import me.libraryaddict.build.types.FloodSettings;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class FloodManager extends MiniPlugin {
    private HashMap<WorldInfo, ArrayList<FloodSettings>> _floods = new HashMap<WorldInfo, ArrayList<FloodSettings>>();

    public FloodManager(JavaPlugin plugin) {
        super(plugin, "Flood Manager");
    }

    @EventHandler
    public void onTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        Iterator<Entry<WorldInfo, ArrayList<FloodSettings>>> itel = _floods.entrySet().iterator();

        while (itel.hasNext()) {
            Entry<WorldInfo, ArrayList<FloodSettings>> entry = itel.next();

            if (!entry.getKey().isLoaded()) {
                itel.remove();
                continue;
            }

            Iterator<FloodSettings> itel2 = entry.getValue().iterator();

            while (itel2.hasNext()) {
                FloodSettings flood = itel2.next();

                flood.onTick();

                if (flood.getBlocksDone() > flood.getMaxFlood()) {
                    itel2.remove();

                    // TODO Log it

                    continue;
                }

                if (!flood.isFinished()) {// TODO Log it
                    continue;
                }

                // TODO Log it

                itel2.remove();
            }

            if (!entry.getValue().isEmpty())
                continue;

            itel.remove();
        }
    }

    @EventHandler
    public void onUnload(WorldInfoUnloadEvent event) {
        if (!_floods.containsKey(event.getWorldInfo()))
            return;

        event.setCancelled(true);
    }
}
