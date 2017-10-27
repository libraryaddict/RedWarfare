package me.libraryaddict.build.customdata.borders;

import me.libraryaddict.build.customdata.CustomData;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class CircleBorder extends CustomData {
    private HashMap<Player, ArrayList<Block>> _blocks = new HashMap<Player, ArrayList<Block>>();
    private Pair<Hologram, Block> _borderCenter;
    private int _radius = 20;
    private ItemStack _setBorder = new ItemBuilder(Material.BARRIER).setTitle(C.Blue + "Set Border")
            .addLore(C.Green + "Use this tool to set the map's border",
                    C.DAqua + C.Bold + "LEFT CLICK " + C.Aqua + " to set border center",
                    C.DGreen + C.Bold + "RIGHT CLICK " + C.Aqua + " to set border radius").build();

    public CircleBorder(WorldInfo world) {
        super(world);
    }

    @Override
    public ArrayList<ItemStack> getButtons() {
        return new ArrayList<ItemStack>();
    }

    public Pair<Hologram, Block> getCenter() {
        return _borderCenter;
    }

    public ArrayList<Pair<Integer, Integer>> getChunksToKeep() {
        ArrayList<Pair<Integer, Integer>> chunks = new ArrayList<Pair<Integer, Integer>>();

        Block center = _borderCenter.getValue();
        int radius = _radius + (16 * 3);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.sqrt((x * x) + (z * z)) > radius)
                    continue;

                Pair<Integer, Integer> pair = Pair
                        .of(Math.floorDiv(center.getX() + x, 16), Math.floorDiv(center.getZ() + z, 16));

                if (chunks.contains(pair))
                    continue;

                chunks.add(pair);
            }
        }

        return chunks;
    }

    public ArrayList<Pair<Integer, Integer>> getChunksToScan() {
        ArrayList<Pair<Integer, Integer>> chunks = new ArrayList<Pair<Integer, Integer>>();

        Block center = _borderCenter.getValue();

        for (int x = -_radius; x <= _radius; x++) {
            for (int z = -_radius; z <= _radius; z++) {
                if (Math.sqrt((x * x) + (z * z)) > _radius)
                    continue;

                Pair<Integer, Integer> pair = Pair
                        .of(Math.floorDiv(center.getX() + x, 16), Math.floorDiv(center.getZ() + z, 16));

                if (chunks.contains(pair))
                    continue;

                chunks.add(pair);
            }
        }

        return chunks;
    }

    /**
     * Returns string if config is incomplete, null if config is ready
     */
    public String getMissing() {
        if (_borderCenter == null)
            return "Border has not been set";

        if (UtilLoc.getDistance2d(getWorld().getSpawnLocation(),
                _borderCenter.getKey().getLocation().add(0.5, 0, 0.5)) >= _radius) {
            return "World Spawn isn't inside the border";
        }

        if (_radius < 10) {
            return "Border is too small";
        }

        return null;
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

        tools.add(_setBorder);

        return tools;
    }

    @Override
    public boolean isBorderSet() {
        return _borderCenter != null;
    }

    public boolean isInside(Location location) {
        return _borderCenter != null && UtilLoc
                .getDistance(location, _borderCenter.getKey().getLocation().add(0.5, 0, 0.5)) < _radius;
    }

    @Override
    public void loadConfig(HashMap<String, ArrayList<String>> hashmap) {
    }

    public void loadConfig(YamlConfiguration config) {
        if (!config.contains("Border.Type") || !config.getString("Border.Type").equals("Circle"))
            return;

        _radius = config.getInt("Border.Radius");

        Block block = getBlock(config.getString("Border.Center"));

        Hologram holo = new Hologram(block.getLocation().add(0.5, 1, 0.5), "Border Center", "Size: " + _radius);
        holo.start();

        _borderCenter = Pair.of(holo, block);
    }

    @EventHandler
    public void onInteractBorder(PlayerInteractEvent event) {
        if (!UtilInv.isSimilar(event.getItem(), _setBorder))
            return;

        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (getWorld() != player.getWorld())
            return;

        if (!Recharge.canUse(player, "Tool"))
            return;

        Recharge.use(player, "Tool", 100);

        if (!isEditor(player)) {
            player.sendMessage(C.Red + "You do not have the right to use that tool");
            return;
        }

        event.setCancelled(true);

        Block block = event.getClickedBlock();

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (_borderCenter != null) {
                _borderCenter.setValue(block);
                _borderCenter.getKey().setLocation(block.getLocation().add(0.5, 1, 0.5));
            } else {
                Hologram holo = new Hologram(block.getLocation().add(0.5, 1, 0.5), "Border Center");
                holo.start();

                _borderCenter = Pair.of(holo, block);
            }

            Announce(C.Gold + player.getName() + " has set the border center");
        } else {
            if (_borderCenter == null) {
                player.sendMessage(C.Red + "Need to set border center first");
                return;
            }

            _radius = (int) Math.ceil(UtilLoc.getDistance2d(block, _borderCenter.getValue()));

            if (_radius < 10)
                _radius = 10;

            for (Entry<Player, ArrayList<Block>> entry : _blocks.entrySet()) {
                Player p = entry.getKey();

                for (Block b : entry.getValue()) {
                    p.sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
                }
            }

            _blocks.clear();

            _borderCenter.getKey().setText("Border Center", "Size: " + _radius);

            Announce(C.Gold + player.getName() + " has set the border radius to " + _radius);
        }
    }

    public void onTick() {
        if (getCenter() == null)
            return;

        Iterator<Entry<Player, ArrayList<Block>>> itel = _blocks.entrySet().iterator();

        while (itel.hasNext()) {
            Entry<Player, ArrayList<Block>> entry = itel.next();

            Player player = entry.getKey();

            if (!player.isOnline() || player.getWorld() != getWorld()) {
                itel.remove();
                continue;
            }

            Iterator<Block> bItel = entry.getValue().iterator();

            while (bItel.hasNext()) {
                Block block = bItel.next();

                double dist = UtilLoc
                        .getDistance(player.getLocation().add(0, 1, 0), block.getLocation().add(0.5, 0.5, 0.5));

                if (dist > 4 && dist < 15)
                    continue;

                bItel.remove();

                player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
            }
        }

        for (Player player : getWorld().getPlayers()) {
            if (!_blocks.containsKey(player))
                _blocks.put(player, new ArrayList<Block>());

            ArrayList<Block> blocks = _blocks.get(player);
            Location loc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);

            for (int x = -15; x <= 15; x++) {
                for (int y = -15; y <= 15; y++) {
                    for (int z = -15; z <= 15; z++) {
                        Location blockLoc = loc.clone().add(x, y + 1, z).getBlock().getLocation();

                        if (blockLoc.getY() < 0 || blockLoc.getY() > 256 || loc.distance(blockLoc) > 8 || loc
                                .distance(blockLoc) <= 4.2 || blocks.contains(blockLoc.getBlock()) || blockLoc
                                .getBlock().getType() != Material.AIR)
                            continue;

                        double dist = UtilLoc.getDistance2d(blockLoc,
                                getCenter().getValue().getLocation().add(0.5, 0, 0.5)) - _radius;

                        if (dist < 0 || dist > 1) {
                            continue;
                        }

                        blocks.add(blockLoc.getBlock());

                        player.sendBlockChange(blockLoc, Material.GLASS, (byte) 0);
                    }
                }
            }
        }
    }

    @Override
    public void saveConfig(HashMap<String, ArrayList<String>> hashmap) {
    }

    public void saveConfig(YamlConfiguration config) {
        if (_borderCenter == null)
            return;

        ConfigurationSection section = config.createSection("Border");

        section.set("Type", "Circle");
        section.set("Radius", _radius);
        section.set("Center", toString(_borderCenter.getValue()));
    }

    public void unloadData() {
        super.unloadData();

        if (_borderCenter == null)
            return;

        _borderCenter.getKey().stop();
    }
}
