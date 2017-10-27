package me.libraryaddict.build.customdata.borders;

import me.libraryaddict.build.customdata.CustomData;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilMath;
import org.bukkit.Effect;
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

public class SquareBorder extends CustomData {
    private HashMap<Player, ArrayList<Block>> _blocks = new HashMap<Player, ArrayList<Block>>();
    private Block[] _border = new Block[2];
    private Hologram[] _borderHolo = new Hologram[2];
    private ItemStack _setBorderSquare = new ItemBuilder(Material.BARRIER).setTitle(C.Blue + "Set Border")
            .addLore(C.Green + "Use this tool to set the map's border",
                    C.DAqua + C.Bold + "LEFT CLICK " + C.Aqua + " to set point 1",
                    C.DGreen + C.Bold + "RIGHT CLICK " + C.Aqua + " to set point 2").build();
    private String _warningCode;

    public SquareBorder(WorldInfo world) {
        super(world);

        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();

        for (int i = 0; i < 6; i++) {
            _warningCode += chars[UtilMath.r(chars.length)];
        }

        _warningCode = _warningCode.toUpperCase();
    }

    @Override
    public ArrayList<ItemStack> getButtons() {
        return new ArrayList<ItemStack>();
    }

    public ArrayList<Pair<Integer, Integer>> getChunksToKeep() {
        Block min = getMinBorder();
        Block max = getMaxBorder();

        ArrayList<Pair<Integer, Integer>> chunks = new ArrayList<Pair<Integer, Integer>>();

        for (int x = Math.floorDiv(min.getX(), 16) - 3; x <= Math.floorDiv(max.getX(), 16) + 3; x++) {
            for (int z = Math.floorDiv(min.getZ(), 16) - 3; z <= Math.floorDiv(max.getZ(), 16) + 3; z++) {
                chunks.add(Pair.of(x, z));
            }
        }

        return chunks;
    }

    public ArrayList<Pair<Integer, Integer>> getChunksToScan() {
        Block min = getMinBorder();
        Block max = getMaxBorder();

        ArrayList<Pair<Integer, Integer>> chunks = new ArrayList<Pair<Integer, Integer>>();

        for (int x = Math.floorDiv(min.getX(), 16); x <= Math.floorDiv(max.getX(), 16); x++) {
            for (int z = Math.floorDiv(min.getZ(), 16); z <= Math.floorDiv(max.getZ(), 16); z++) {
                chunks.add(Pair.of(x, z));
            }
        }

        return chunks;
    }

    public Block getMaxBorder() {
        if (_border[0] == null || _border[1] == null)
            return null;

        int maxX = Math.max(_border[0].getX(), _border[1].getX());
        int maxY = Math.max(_border[0].getY(), _border[1].getY());
        int maxZ = Math.max(_border[0].getZ(), _border[1].getZ());

        return getWorld().getBlockAt(maxX, maxY, maxZ);
    }

    public Block getMinBorder() {
        if (_border[0] == null || _border[1] == null)
            return null;

        int minX = Math.min(_border[0].getX(), _border[1].getX());
        int minY = Math.min(_border[0].getY(), _border[1].getY());
        int minZ = Math.min(_border[0].getZ(), _border[1].getZ());

        return getWorld().getBlockAt(minX, minY, minZ);
    }

    /**
     * Returns string if config is incomplete, null if config is ready
     */
    public String getMissing() {
        if (_border[0] == null || _border[1] == null)
            return "Border has not been set";

        if (!UtilLoc.isInside(getMinBorder(), getMaxBorder(), getWorld().getSpawnLocation(), true)) {
            return "World Spawn isn't inside the border";
        }

        if (Math.abs(_border[0].getX() - _border[1].getX()) < 5 || (_border[0].getY() != _border[1].getY() && Math
                .abs(_border[0].getY() - _border[1].getY()) < 5) || Math
                .abs(_border[0].getZ() - _border[1].getZ()) < 5) {
            return "Borders are too close to each other";
        }

        return null;
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

        tools.add(_setBorderSquare);

        return tools;
    }

    public String getWarningCode() {
        if (isBorderSet() && _border[0].getY() != _border[1].getY())
            return _warningCode;

        return null;
    }

    @Override
    public boolean isBorderSet() {
        return _border[0] != null && _border[1] != null;
    }

    public boolean isInside(Location location) {
        return getMinBorder() != null && getMaxBorder() != null && UtilLoc
                .isInside(getMinBorder(), getMaxBorder(), location, true);
    }

    @Override
    public void loadConfig(HashMap<String, ArrayList<String>> hashmap) {
    }

    public void loadConfig(YamlConfiguration config) {
        if (!config.contains("Border.Type") || !config.getString("Border.Type").equals("Square"))
            return;

        int i = 0;

        for (String border : config.getStringList("Border.Corners")) {
            Block block = getBlock(border);

            _border[i++] = block;
        }

        for (i = 0; i < 2; i++) {
            Block block = _border[i];

            if (block == null) {
                continue;
            }

            Hologram hologram = new Hologram(block.getLocation().add(0.5, 1, 0.5), "Border Point " + (i + 1));
            hologram.start();

            _borderHolo[i] = hologram;
        }
    }

    @EventHandler
    public void onInteractBorder(PlayerInteractEvent event) {
        if (!UtilInv.isSimilar(event.getItem(), _setBorderSquare))
            return;

        if (event.getAction() == Action.PHYSICAL)
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

        if (block == null)
            block = player.getLocation().getBlock();

        int i = event.getAction().name().contains("RIGHT") ? 1 : 0;

        Location newLoc = block.getLocation().add(0.5, 1, 0.5);

        if (_borderHolo[i] != null) {
            _borderHolo[i].setLocation(newLoc);
        } else {
            Hologram hologram = new Hologram(newLoc, "Border Point " + (i + 1));
            hologram.start();

            _borderHolo[i] = hologram;
        }

        _border[i] = block;

        Announce(C.Gold + player.getName() + " has modified border point " + (i + 1) + " to [" + block
                .getX() + "," + block.getY() + "," + block.getZ() + "]");

        if (isBorderSet() && _border[0].getY() != _border[1].getY()) {
            player.sendMessage(
                    UtilError.format("Caution!", "You defined your map as having a max height and floor level"));
            player.sendMessage(
                    C.Gray + "This means that you are specifically stating that players are not allowed to go higher," +
                            "" + "" + " that there must be a Y limit");
            player.sendMessage(
                    C.Gray + "If you did not intend this, place your borders on the same Y level to remove the " +
                            "height" + " limit");
        }
    }

    public void onTick() {
        showYLevel();

        if (_border[0] == null || _border[1] == null)
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
            Location loc = player.getLocation();

            Block min = this.getMinBorder();
            Block max = this.getMaxBorder();

            int x1 = min.getX();
            int x2 = max.getX();
            int y1 = min.getY();
            int y2 = max.getY();
            int z1 = min.getZ();
            int z2 = max.getZ();

            for (int x = -15; x <= 15; x++) {
                for (int y = -15; y <= 15; y++) {
                    for (int z = -15; z <= 15; z++) {
                        Location blockLoc = loc.clone().add(x, y + 1, z).getBlock().getLocation();

                        if (blockLoc.getY() < 0 || blockLoc.getY() > 256 || loc.distance(blockLoc) > 8 || loc
                                .distance(blockLoc) <= 4.2 || blocks.contains(blockLoc.getBlock()) || blockLoc
                                .getBlock().getType() != Material.AIR)
                            continue;

                        int bX = blockLoc.getBlockX();
                        int bY = blockLoc.getBlockY();
                        int bZ = blockLoc.getBlockZ();

                        if ((y1 == y2 || (y1 != bY && y2 != bY)) && (bX != x1 && bX != x2) && (bZ != z1 && bZ != z2))
                            continue;

                        if ((bX < x1 || bX > x2) || (bZ < z1 || bZ > z2))
                            continue;

                        if (y1 != y2 && (bY < y1 || bY > y2))
                            continue;

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
        ArrayList<String> border = new ArrayList<String>();

        if (_border[0] == null && _border[1] == null)
            return;

        for (Block b : _border) {
            if (b == null)
                continue;

            border.add(toString(b));
        }

        if (border.size() == 1)
            border.add(border.get(0));

        ConfigurationSection section = config.createSection("Border");

        section.set("Type", "Square");
        section.set("Corners", border);
    }

    private void showYLevel() {
        for (Block b : _border) {
            if (b == null)
                continue;

            for (Player player : getWorld().getPlayers()) {
                if (!UtilInv.isHolding(player, _setBorderSquare))
                    continue;

                Location loc = player.getLocation();

                loc.setY(b.getY());

                loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
            }
        }
    }

    public void unloadData() {
        super.unloadData();

        for (Hologram holo : _borderHolo) {
            if (holo == null)
                continue;

            holo.stop();
        }
    }
}
