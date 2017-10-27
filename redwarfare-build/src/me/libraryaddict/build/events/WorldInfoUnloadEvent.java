package me.libraryaddict.build.events;

import me.libraryaddict.build.types.WorldInfo;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldInfoUnloadEvent extends Event implements Cancellable {
    private static final HandlerList _handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return _handlers;
    }

    private boolean _cancelled;
    private boolean _forced;
    private WorldInfo _worldInfo;

    public WorldInfoUnloadEvent(WorldInfo info, boolean force) {
        _worldInfo = info;
        _forced = force;
    }

    @Override
    public HandlerList getHandlers() {
        return _handlers;
    }

    public WorldInfo getWorldInfo() {
        return _worldInfo;
    }

    @Override
    public boolean isCancelled() {
        if (isForced())
            return false;

        return _cancelled;
    }

    public boolean isForced() {
        return _forced;
    }

    @Override
    public void setCancelled(boolean cancel) {
        _cancelled = cancel;
    }
}
