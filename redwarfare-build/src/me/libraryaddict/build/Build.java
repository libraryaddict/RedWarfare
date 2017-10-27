package me.libraryaddict.build;

import me.libraryaddict.build.managers.BuildManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Build extends JavaPlugin {
    private BuildManager _buildManager;

    @Override
    public void onDisable() {
        _buildManager.getWorld().forceSaveWorlds();
        _buildManager.getWorld().forceUnloadWorlds();
    }

    @Override
    public void onEnable() {
        _buildManager = new BuildManager(this);
    }
}