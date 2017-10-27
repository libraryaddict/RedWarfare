package me.libraryaddict.build.importers;

import me.libraryaddict.build.types.MapType;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilLoc;
import net.lingala.zip4j.io.ZipInputStream;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SnDMapImport {
    private ArrayList<String> getSpawns(World world, int centerX, int centerY, int centerZ, int radius) {
        ArrayList<String> spawns = new ArrayList<String>();
        Location center = new Location(world, centerX, centerY, centerZ);

        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                Location spawn = new Location(world, x + 0.5, centerY, z + 0.5);

                for (int y : new int[]{0, 1, -1}) {
                    Location l = spawn.clone().add(0, y, 0);

                    if (UtilLoc.isSpawnableHere(l) && UtilLoc.hasSight(l, center)) {
                        spawns.add(l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
                        break;
                    }
                }
            }
        }

        return spawns;
    }

    public void parse(File mapFolder, World world) {
        File configFile = new File(mapFolder, "config.yml");

        if (!configFile.exists())
            return;

        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(new File(mapFolder, "config.yml"));

        if (!oldConfig.contains("MapSettings.SpawnRadius") && !oldConfig.contains("SpawnRadius"))
            return;

        configFile.renameTo(new File(mapFolder, "oldconfig.yml"));

        YamlConfiguration newConfig = new YamlConfiguration();

        HashMap<String, ArrayList> data = new HashMap<String, ArrayList>();

        String name = oldConfig.getString("MapInfo.MapName", oldConfig.getString("MapName"));

        if (oldConfig.getBoolean("MapSettings.RandomBase", oldConfig.getBoolean("RandomBombs", false))) {
            data.put("Random Base", new ArrayList(Arrays.asList(true)));
        }

        newConfig.set("Name", name);
        newConfig.set("Author", oldConfig.getString("Author", oldConfig.getString("MapInfo.Author")));
        newConfig.set("Creator", "Map Importer");
        newConfig.set("Description", oldConfig.getString("Description", oldConfig.getString("MapInfo.Description")));
        newConfig.set("Created", System.currentTimeMillis());
        newConfig.set("Modified", System.currentTimeMillis());
        newConfig.set("Generation", "FLAT:3;minecraft:air;1;minecraft:air");
        newConfig.set("MapType", MapType.SearchAndDestroy.name());

        UtilLoc.writeEmptyGeneration(new File(mapFolder, "level.dat"));

        int ySpawn = world.getSpawnLocation().getBlockY();

        int x1 = oldConfig.getInt("FirstX", oldConfig.getInt("Corners.X1"));
        int x2 = oldConfig.getInt("SecondX", oldConfig.getInt("Corners.X2"));
        int z1 = oldConfig.getInt("FirstZ", oldConfig.getInt("Corners.Z1"));
        int z2 = oldConfig.getInt("SecondZ", oldConfig.getInt("Corners.Z2"));

        ArrayList<String> border = new ArrayList<String>();

        border.add(Math.min(x1, x2) + "," + ySpawn + "," + Math.min(z1, z2));
        border.add(Math.max(x1, x2) + "," + ySpawn + "," + Math.max(z1, z2));

        ConfigurationSection borderConfig = newConfig.createSection("Border");
        borderConfig.set("Type", "Square");
        borderConfig.set("Corners", border);

        ConfigurationSection teamConfig = newConfig.createSection("Teams");

        if (oldConfig.contains("Spawn1")) {
            int i = 1;

            while (oldConfig.contains("Spawn" + i)) {
                ConfigurationSection section = oldConfig.getConfigurationSection("Spawn" + i);
                String team = section.getString("TeamName").split(" ")[0].toUpperCase();
                int radius = section.getInt("Radius");
                int centerX = section.getInt("X");
                int centerY = section.getInt("Y");
                int centerZ = section.getInt("Z");

                teamConfig.createSection(team).set("Spawns", getSpawns(world, centerX, centerY, centerZ, radius));

                String bomb = "Bomb" + i + ".";
                data.put(team + " Bombs", new ArrayList(Arrays.asList(
                        oldConfig.getString(bomb + "X") + "," + oldConfig.getString(bomb + "Y") + "," + oldConfig
                                .getString(bomb + "Z"))));
                i++;
            }
        } else if (oldConfig.contains("Teams")) {
            HashMap<String, String> bombs = new HashMap<String, String>();

            for (String key : oldConfig.getConfigurationSection("Bombs").getKeys(false)) {
                bombs.put(key, oldConfig.getString("Bombs." + key + ".X") + "," + oldConfig
                        .getString("Bombs." + key + ".Y") + "," + oldConfig.getString("Bombs." + key + ".Z"));
            }

            ConfigurationSection configSection = oldConfig.getConfigurationSection("Teams");

            for (String teamName : configSection.getKeys(false)) {
                ConfigurationSection configSection2 = configSection.getConfigurationSection(teamName);
                int radius = configSection2.getInt("Radius");
                int centerX = configSection2.getInt("X");
                int centerY = configSection2.getInt("Y");
                int centerZ = configSection2.getInt("Z");

                teamConfig.createSection(teamName).set("Spawns", getSpawns(world, centerX, centerY, centerZ, radius));

                data.put(teamName + " Bombs", new ArrayList(Arrays.asList(bombs.get(configSection2.get("Bomb")))));
            }
        }

        ArrayList done = new ArrayList();
        int removed = 0;

        for (int cx = Math.min(x1, x2); cx <= Math.max(x1, x2); cx++) {
            for (int cz = Math.min(z1, z2); cz <= Math.max(z1, z2); cz++) {
                Pair<Integer, Integer> pair = Pair.of(Math.floorDiv(cx, 16), Math.floorDiv(cz, 16));

                if (done.contains(pair))
                    continue;

                done.add(pair);

                Chunk snap = world.getChunkAt(pair.getKey(), pair.getValue());

                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 256; y++) {
                        for (int z = 0; z < 16; z++) {
                            Block block = snap.getBlock(x, y, z);

                            if (block.getState() instanceof Sign) {
                                Sign sign = (Sign) block.getState();

                                if (ChatColor.stripColor(sign.getLine(0)).toLowerCase().contains("[buy]")) {
                                    block.setType(Material.AIR);
                                    removed++;
                                }
                            }
                        }
                    }
                }
            }
        }

        Bukkit.broadcastMessage(C.Blue + "Removed " + removed + " buy signs");

        if (!data.isEmpty()) {
            ConfigurationSection custom = newConfig.createSection("Custom");

            for (String key : data.keySet()) {
                custom.set(key, data.get(key));
            }
        }

        try {
            newConfig.save(new File(mapFolder, "build.yml"));
        }
        catch (IOException e) {
            UtilError.handle(e);
        }
    }

    public YamlConfiguration readConfig(ZipInputStream stream) {
        if (stream == null)
            return null;

        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));

        if (!oldConfig.contains("MapSettings.SpawnRadius") && !oldConfig.contains("SpawnRadius"))
            return null;

        System.out.println("Found SnD config");

        YamlConfiguration newConfig = new YamlConfiguration();

        String name = oldConfig.getString("MapInfo.MapName", oldConfig.getString("MapName"));

        newConfig.set("Name", name);
        newConfig.set("Author", oldConfig.getString("Author", oldConfig.getString("MapInfo.Author")));
        newConfig.set("Creator", "Map Importer");
        newConfig.set("Description", oldConfig.getString("Description", oldConfig.getString("MapInfo.Description")));
        newConfig.set("Created", System.currentTimeMillis());
        newConfig.set("Modified", System.currentTimeMillis());
        newConfig.set("Generation", "FLAT:3;minecraft:air;1;minecraft:air");
        newConfig.set("MapType", MapType.SearchAndDestroy.name());

        ConfigurationSection builders = newConfig.createSection("Builders");
        builders.set("libraryaddict", true);

        return newConfig;
    }
}
