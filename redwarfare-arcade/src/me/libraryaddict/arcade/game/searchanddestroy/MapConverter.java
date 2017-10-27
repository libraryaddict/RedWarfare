package me.libraryaddict.arcade.game.searchanddestroy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilLoc;

public class MapConverter
{
    public void convert()
    {
        File mapsFolder = new File("E:\\ServerFiles\\SDMaps");

        for (File mapFolder : mapsFolder.listFiles())
        {
            File ff = new File("E:\\ServerFiles\\TempFolder");

            UtilFile.delete(ff);
            UtilFile.copyFile(mapFolder, ff);

            String folderName = mapFolder.getName();
            mapFolder = ff;

            UtilFile.delete(new File("GameWorld"));

            UtilFile.copyFile(mapFolder, new File("GameWorld"));

            WorldCreator creator = new WorldCreator("GameWorld");

            World world = creator.createWorld();

            File configFile = new File(mapFolder, "config.yml");
            configFile.delete();

            try
            {
                configFile.createNewFile();
            }
            catch (IOException e1)
            {
                UtilError.handle(e1);
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("GameWorld/config.yml"));
            YamlConfiguration config2 = YamlConfiguration.loadConfiguration(configFile);

            HashMap<String, ArrayList> data = new HashMap<String, ArrayList>();

            String name = config.getString("MapInfo.MapName", config.getString("MapName"));

            if (config.getBoolean("MapSettings.RandomBase", config.getBoolean("RandomBombs", false)))
            {
                data.put("Random Base", new ArrayList(Arrays.asList(true)));
            }

            config2.set("Name", name);
            config2.set("Author", config.getString("Author", config.getString("MapInfo.Author")));
            config2.set("Description", config.getString("Description", config.getString("MapInfo.Description")));

            ArrayList<String> border = new ArrayList<String>();
            border.add(config.getString("FirstX", config.getString("Corners.X1")) + ",0,"
                    + config.getString("FirstZ", config.getString("Corners.Z1")));
            border.add(config.getString("SecondX", config.getString("Corners.X2")) + ",0,"
                    + config.getString("SecondZ", config.getString("Corners.Z2")));

            config2.set("Border", border);

            ConfigurationSection teamConfig = config2.createSection("Teams");

            if (config.contains("Spawn1"))
            {
                int i = 1;

                while (config.contains("Spawn" + i))
                {
                    ConfigurationSection section = config.getConfigurationSection("Spawn" + i);
                    String team = section.getString("TeamName").split(" ")[0].toUpperCase();
                    int radius = section.getInt("Radius");
                    int centerX = section.getInt("X");
                    int centerY = section.getInt("Y");
                    int centerZ = section.getInt("Z");

                    teamConfig.createSection(team).set("Spawns", getSpawns(folderName, world, centerX, centerY, centerZ, radius));

                    String bomb = "Bomb" + i + ".";
                    data.put(team + " Bombs", new ArrayList(Arrays.asList(config.getString(bomb + "X") + ","
                            + config.getString(bomb + "Y") + "," + config.getString(bomb + "Z"))));
                    i++;
                }
            }
            else if (config.contains("Teams"))
            {
                HashMap<String, String> bombs = new HashMap<String, String>();

                for (String key : config.getConfigurationSection("Bombs").getKeys(false))
                {
                    bombs.put(key, config.getString("Bombs." + key + ".X") + "," + config.getString("Bombs." + key + ".Y") + ","
                            + config.getString("Bombs." + key + ".Z"));
                }

                ConfigurationSection configSection = config.getConfigurationSection("Teams");

                for (String teamName : configSection.getKeys(false))
                {
                    ConfigurationSection configSection2 = configSection.getConfigurationSection(teamName);
                    int radius = configSection2.getInt("Radius");
                    int centerX = configSection2.getInt("X");
                    int centerY = configSection2.getInt("Y");
                    int centerZ = configSection2.getInt("Z");

                    teamConfig.createSection(teamName).set("Spawns",
                            getSpawns(folderName, world, centerX, centerY, centerZ, radius));

                    data.put(teamName + " Bombs", new ArrayList(Arrays.asList(bombs.get(configSection2.get("Bomb")))));
                }
            }

            if (!data.isEmpty())
            {
                ConfigurationSection custom = config2.createSection("Custom");

                for (String key : data.keySet())
                {
                    custom.set(key, data.get(key));
                }
            }

            try
            {
                config2.save(configFile);
            }
            catch (IOException e)
            {
                UtilError.handle(e);
            }

            File dest = new File(mapFolder.getParentFile().getAbsolutePath() + "/" + folderName + ".zip");
            dest.delete();

            UtilFile.createZip(mapFolder, dest);

            Bukkit.unloadWorld(world, false);
        }
    }

    private ArrayList<String> getSpawns(String mapName, World world, int centerX, int centerY, int centerZ, int radius)
    {
        ArrayList<String> spawns = new ArrayList<String>();
        Location center = new Location(world, centerX, centerY, centerZ);

        for (int x = centerX - radius; x <= centerX + radius; x++)
        {
            for (int z = centerZ - radius; z <= centerZ + radius; z++)
            {
                Location spawn = new Location(world, x + 0.5, centerY, z + 0.5);

                for (int y : new int[]
                    {
                            0, 1, -1
                    })
                {
                    Location l = spawn.clone().add(0, y, 0);

                    if (UtilLoc.isSpawnableHere(l) && UtilLoc.hasSight(l, center))
                    {
                        spawns.add(l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
                        break;
                    }
                }
            }
        }

        if (spawns.isEmpty())
            throw new RuntimeException("Error finding spawns");

        return spawns;
    }
}
