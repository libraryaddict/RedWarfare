package me.libraryaddict.core.inventory;

import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.plugin.MiniPlugin;

public class InventoryManager extends MiniPlugin
{
    public static InventoryManager Manager;

    public InventoryManager(JavaPlugin plugin)
    {
        super(plugin, "Inventory Manager");

        Manager = this;
    }

    public void registerInventory(BasicInventory inv)
    {
        registerListener(inv);
    }
}
