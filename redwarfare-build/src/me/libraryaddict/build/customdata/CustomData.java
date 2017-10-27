package me.libraryaddict.build.customdata;

import me.libraryaddict.build.parsers.BasicParser;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.WorldInfo;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public abstract class CustomData {
    private WorldInfo _worldInfo;

    public CustomData(WorldInfo world) {
        _worldInfo = world;
    }

    public void Announce(String message) {
        getInfo().Announce(message);
    }

    public BasicParser createParser(Player player) {
        return new BasicParser(getInfo(), player.getUniqueId());
    }

    public Block getBlock(String string) {
        String[] split = string.split(",");

        return getWorld()
                .getBlockAt(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public abstract ArrayList<ItemStack> getButtons();

    public MapInfo getData() {
        return getInfo().getData();
    }

    public WorldInfo getInfo() {
        return _worldInfo;
    }

    public Location getLocation(String string) {
        String[] split = string.split(",");

        if (split.length == 3) {
            split = Arrays.copyOf(split, 5);

            split[3] = "0";
            split[4] = "0";
        }

        return new Location(getWorld(), Double.parseDouble(split[0]), Double.parseDouble(split[1]),
                Double.parseDouble(split[2]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
    }

    public abstract String getMissing();

    public abstract ArrayList<ItemStack> getTools();

    public String getWarningCode() {
        return null;
    }

    public World getWorld() {
        return _worldInfo.getWorld();
    }

    public boolean isBorderSet() {
        return false;
    }

    public final boolean isBuilder(Player player) {
        return getInfo().isBuilder(player);
    }

    public final boolean isEditor(Player player) {
        return getInfo().isEditor(player);
    }

    public boolean isInMap(Player player) {
        return player.getWorld() == getWorld();
    }

    public abstract void loadConfig(HashMap<String, ArrayList<String>> hashmap);

    public abstract void loadConfig(YamlConfiguration config);

    public void onButtonClick(Player player, ItemStack item) {
    }

    public abstract void saveConfig(HashMap<String, ArrayList<String>> hashmap);

    public abstract void saveConfig(YamlConfiguration config);

    public String toString(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }

    public String toString(Location location) {
        return location.getX() + "," + location.getY() + "," + location.getZ() + "," + location
                .getYaw() + "," + location.getPitch();
    }

    public void unloadData() {
    }
}
