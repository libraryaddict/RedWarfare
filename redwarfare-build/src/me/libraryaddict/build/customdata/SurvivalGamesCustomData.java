package me.libraryaddict.build.customdata;

import me.libraryaddict.build.customdata.borders.CircleBorder;
import me.libraryaddict.build.customdata.borders.DeathmatchBorder;
import me.libraryaddict.build.parsers.BasicParser;
import me.libraryaddict.build.parsers.SurvivalGamesParser;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SurvivalGamesCustomData extends GameCustomData {
    private DeathmatchBorder _deathmatchBorder;
    private ItemStack _modifyChest = new ItemBuilder(Material.STICK).setTitle(C.Blue + "Add Special Chest")
            .addLore(C.Green + "Right click on a chest to add/remove them as special!").build();
    private ArrayList<Pair<Block, Hologram>> _specialChests = new ArrayList<Pair<Block, Hologram>>();

    public SurvivalGamesCustomData(WorldInfo world) {
        super(world, CircleBorder.class, TeamSettings.PLAYER);

        _deathmatchBorder = new DeathmatchBorder(world, this);
    }

    @Override
    public BasicParser createParser(Player player) {
        return new SurvivalGamesParser(getInfo(), player.getUniqueId());
    }

    @Override
    public int getMaxSpawns() {
        return 24;
    }

    @Override
    public int getMaxTeams() {
        return 1;
    }

    @Override
    public int getMinSpawns() {
        return 24;
    }

    @Override
    public int getMinTeams() {
        return 1;
    }

    @Override
    public String getMissing() {
        for (Pair<Block, Hologram> pair : _specialChests) {
            Material type = pair.getKey().getType();

            if (type != Material.TRAPPED_CHEST && type != Material.CHEST)
                return "No chest found at " + toString(pair.getKey());
        }

        if (_deathmatchBorder.getMissing(this) != null)
            return _deathmatchBorder.getMissing(this);

        return super.getMissing();
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = super.getTools();

        tools.add(_modifyChest);
        tools.addAll(_deathmatchBorder.getTools());

        return tools;
    }

    public void loadConfig(HashMap<String, ArrayList<String>> hashmap) {
        super.loadConfig(hashmap);

        _deathmatchBorder.loadConfig(hashmap);

        if (!hashmap.containsKey("Special Chests"))
            return;

        for (String chest : hashmap.get("Special Chests")) {
            Block block = getBlock(chest);

            Hologram hologram = new Hologram(block.getLocation().add(0.5, 1, 0.5), C.Gold + C.Bold + "Chest");
            hologram.start();

            _specialChests.add(Pair.of(block, hologram));
        }
    }

    @EventHandler
    public void onDeathMatchSec(TimeEvent event) {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        _deathmatchBorder.onTick();
    }

    @EventHandler
    public void onInteractChest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (!isInMap(player))
            return;

        if (!UtilInv.isSimilar(_modifyChest, event.getItem()))
            return;

        event.setCancelled(true);

        if (!Recharge.canUse(player, "Tool"))
            return;

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You are not a builder in this map");
            return;
        }

        Block block = event.getClickedBlock();

        if (block.getType() != Material.TRAPPED_CHEST && block.getType() != Material.CHEST) {
            player.sendMessage(C.Red + "Not a chest");
            return;
        }

        Iterator<Pair<Block, Hologram>> itel = _specialChests.iterator();

        while (itel.hasNext()) {
            Pair<Block, Hologram> pair = itel.next();

            if (!pair.getKey().equals(block))
                continue;

            itel.remove();
            pair.getValue().stop();

            Announce(C.Gold + player.getName() + " has removed a special chest");
            return;
        }

        Hologram hologram = new Hologram(block.getLocation().add(0.5, 1, 0.5), C.Gold + C.Bold + "Chest");
        hologram.start();

        _specialChests.add(Pair.of(block, hologram));
    }

    @EventHandler
    public void onInteractDeathBorder(PlayerInteractEvent event) {
        _deathmatchBorder.onInteractBorder(event);
    }

    public void saveConfig(HashMap<String, ArrayList<String>> hashmap) {
        super.saveConfig(hashmap);

        _deathmatchBorder.saveConfig(hashmap);

        ArrayList<String> chests = new ArrayList<String>();

        for (Pair<Block, Hologram> pair : _specialChests) {
            chests.add(toString(pair.getKey()));
        }

        hashmap.put("Special Chests", chests);
    }

    public void unloadData() {
        super.unloadData();

        for (Pair<Block, Hologram> pair : _specialChests) {
            pair.getValue().stop();
        }

        _deathmatchBorder.unloadData();
    }
}
