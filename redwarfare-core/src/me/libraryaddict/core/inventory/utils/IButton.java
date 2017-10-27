package me.libraryaddict.core.inventory.utils;

import org.bukkit.event.inventory.ClickType;

public interface IButton
{
    /**
     * Return true if cancel
     */
    public boolean onClick(ClickType clickType);
}
