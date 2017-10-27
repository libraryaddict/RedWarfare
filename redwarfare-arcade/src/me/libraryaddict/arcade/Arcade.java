package me.libraryaddict.arcade;

import java.io.File;

import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.utils.UtilFile;

public class Arcade extends JavaPlugin
{
    private ArcadeManager _arcadeManager;

    @Override
    public void onDisable()
    {
        WorldData data;

        if ((data = _arcadeManager.getWorld().getData()) != null)
            UtilFile.delete(data.getWorldFolder());
    }

    @Override
    public void onEnable()
    {
        for (File file : new File("Test").getAbsoluteFile().getParentFile().listFiles())
        {
            String name = file.getName();

            if (!name.contains("_"))
                continue;

            try
            {
                Long.parseLong(name.split("_")[1]);
            }
            catch (Exception ex)
            {
                continue;
            }

            UtilFile.delete(file);
        }

        _arcadeManager = new ArcadeManager(this);
    }
}
