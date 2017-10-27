package me.libraryaddict.build.parsers;

import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class SurvivalGamesParser extends BlockParser {
    private ArrayList<String> _chest = new ArrayList<String>();
    private ArrayList<String> _furnace = new ArrayList<String>();

    public SurvivalGamesParser(WorldInfo worldInfo, UUID publisher) {
        super(worldInfo, publisher, Material.FURNACE, Material.BURNING_FURNACE, Material.CHEST, Material.TRAPPED_CHEST);
    }

    @Override
    public void onFind(Material mat, String block) {
        if (mat == Material.FURNACE || mat == Material.BURNING_FURNACE) {
            _furnace.add(block);
        } else {
            _chest.add(block);
        }
    }

    @Override
    public void saveConfig(HashMap<String, ArrayList<String>> hashmap) {
        getInfo().Announce(C.Gold + "Found " + _chest.size() + " chests");
        getInfo().Announce(C.Gold + "Found " + _furnace.size() + " furnaces");
        hashmap.put("Furnaces", _furnace);
        hashmap.put("Chests", _chest);
    }
}
