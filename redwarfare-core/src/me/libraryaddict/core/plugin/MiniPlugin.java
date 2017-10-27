package me.libraryaddict.core.plugin;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniPlugin implements Listener
{
    private final String _name;
    private final JavaPlugin _plugin;

    public MiniPlugin(JavaPlugin plugin, String pluginName)
    {
        _plugin = plugin;
        _name = pluginName;

        Bukkit.getPluginManager().registerEvents(this, _plugin);

        System.out.print("Enabling: " + _name);
    }

    public final JavaPlugin getPlugin()
    {
        return _plugin;
    }

    protected void registerListener(Listener listener)
    {
        Bukkit.getPluginManager().registerEvents(listener, _plugin);
    }
}
