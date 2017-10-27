package me.libraryaddict.build.importers;

import me.libraryaddict.build.types.MapType;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilString;
import net.lingala.zip4j.io.ZipInputStream;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SGMapImport {
    public void parse(File mapFolder, World world) {
        File configFile = new File(mapFolder, "config.yml");

        if (!configFile.exists())
            return;

        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(new File(mapFolder, "config.yml"));

        if (!oldConfig.contains("bordersize"))
            return;

        configFile.renameTo(new File(mapFolder, "oldconfig.yml"));

        YamlConfiguration newConfig = new YamlConfiguration();

        HashMap<String, ArrayList> data = new HashMap<String, ArrayList>();

        newConfig.set("Name", oldConfig.getString("MapName"));
        newConfig.set("Creator", "Map Importer");
        newConfig.set("Author", UtilString.join(oldConfig.getStringList("MapMakers"), ", "));
        newConfig.set("Description", "");
        newConfig.set("Created", System.currentTimeMillis());
        newConfig.set("Modified", System.currentTimeMillis());
        newConfig.set("Generation", "FLAT:3;minecraft:air;1;minecraft:air");
        newConfig.set("MapType", MapType.SurvivalGames.name());

        UtilLoc.writeEmptyGeneration(new File(mapFolder, "level.dat"));

        ConfigurationSection border = newConfig.createSection("Border");
        border.set("Type", "Circle");

        border.set("Center", (int) Math.floor(oldConfig.getDouble("bordercenter.x")) + "," + (int) Math
                .floor(oldConfig.getDouble("bordercenter.y")) + "," + (int) Math
                .floor(oldConfig.getDouble("bordercenter.z")));
        border.set("Radius", oldConfig.getInt("bordersize"));

        ConfigurationSection teamConfig = newConfig.createSection("Teams");

        ArrayList<String> spawns = new ArrayList<String>();

        for (String key : oldConfig.getConfigurationSection("platforms").getKeys(false)) {
            spawns.add((int) Math.floor(oldConfig.getDouble("platforms." + key + ".x")) + "," + (int) Math
                    .floor(oldConfig.getDouble("platforms." + key + ".y")) + "," + (int) Math
                    .floor(oldConfig.getDouble("platforms." + key + ".z")));
        }

        teamConfig.createSection(TeamSettings.PLAYER.name()).set("Spawns", spawns);

        newConfig.createSection("Custom").set("Deathmatch", Arrays.asList(
                oldConfig.getInt("deathborder") + "," + world.getSpawnLocation().getBlockX() + "," + world
                        .getSpawnLocation().getBlockY() + "," + world.getSpawnLocation().getBlockZ()));

        Block center = world.getBlockAt((int) Math.floor(oldConfig.getDouble("bordercenter.x")),
                (int) Math.floor(oldConfig.getDouble("bordercenter.y")),
                (int) Math.floor(oldConfig.getDouble("bordercenter.z")));

        int _radius = oldConfig.getInt("bordersize");

        ArrayList<String> special = new ArrayList<String>();
        ArrayList done = new ArrayList();

        for (int cx = center.getX() - _radius; cx <= center.getX() + _radius; cx++) {
            if (cx % 16 == 0)
                System.out.println(cx + "/" + (center.getX() + _radius));
            for (int cz = center.getZ() - _radius; cz <= center.getZ() + _radius; cz++) {
                if (Math.sqrt((cx * cx) + (cz * cz)) > _radius)
                    continue;

                Pair<Integer, Integer> pair = Pair.of(Math.floorDiv(cx, 16), Math.floorDiv(cz, 16));

                if (done.contains(pair))
                    continue;

                done.add(pair);

                ChunkSnapshot snap = world.getChunkAt(pair.getKey(), pair.getValue())
                        .getChunkSnapshot(false, false, false);

                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 256; y++) {
                        for (int z = 0; z < 16; z++) {
                            int blockId = snap.getBlockTypeId(x, y, z);

                            for (Material mat : new Material[]{Material.CHEST, Material.TRAPPED_CHEST}) {
                                if (mat.getId() != blockId)
                                    continue;

                                Block block = world.getBlockAt((pair.getKey() * 16) + x, y, (pair.getValue() * 16) + z);

                                if (!block.getChunk().isLoaded())
                                    block.getChunk().load();

                                block.setType(block.getType());

                                String s = block.getX() + "," + block.getY() + "," + block.getZ();

                                if (!(block.getState() instanceof Chest)) {
                                    System.out.println(s);
                                    continue;
                                }

                                Chest inv = (Chest) block.getState();

                                ItemStack item = inv.getBlockInventory().getItem(0);

                                if (item == null || item.getType() == Material.AIR)
                                    continue;

                                if (special.contains(s))
                                    continue;

                                special.add(s);

                                for (int bx = -1; bx <= 1; bx++) {
                                    for (int bz = -1; bz <= 1; bz++) {
                                        if (Math.abs(bx - bz) != 1)
                                            continue;

                                        Block b = block.getRelative(bx, 0, bz);

                                        if (b.getType() == mat) {
                                            special.add(b.getX() + "," + b.getY() + "," + b.getZ());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        data.put("Special Chests", special);

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

        if (!oldConfig.contains("bordersize"))
            return null;

        System.out.println("Found SG config");

        YamlConfiguration newConfig = new YamlConfiguration();

        newConfig.set("Name", oldConfig.getString("MapName"));
        newConfig.set("Author", UtilString.join(oldConfig.getStringList("MapMakers"), ", "));
        newConfig.set("Creator", "Map Importer");
        newConfig.set("Description", "");
        newConfig.set("Created", System.currentTimeMillis());
        newConfig.set("Modified", System.currentTimeMillis());
        newConfig.set("Generation", "FLAT:3;minecraft:air;1;minecraft:air");
        newConfig.set("MapType", MapType.SurvivalGames.name());

        ConfigurationSection builders = newConfig.createSection("Builders");

        for (String name : oldConfig.getStringList("MapMakers")) {
            builders.set(name, true);
        }

        builders.set("libraryaddict", true);

        return newConfig;
    }
}
