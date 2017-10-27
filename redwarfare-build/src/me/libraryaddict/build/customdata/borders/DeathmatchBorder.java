package me.libraryaddict.build.customdata.borders;

import me.libraryaddict.build.customdata.BorderCustomData;
import me.libraryaddict.build.customdata.CustomData;
import me.libraryaddict.build.customdata.SurvivalGamesCustomData;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.fakeentity.FakeEntity;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class DeathmatchBorder extends CustomData {
    private HashMap<Player, ArrayList<Block>> _blocks = new HashMap<Player, ArrayList<Block>>();
    private BorderCustomData _borderCustomData;
    private int _radius = 20;
    private ItemStack _setBorder = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 0)
            .setTitle(C.Blue + "Set Deathmatch Border").addLore(C.Green + "Use this tool to set the Deathmatch border",
                    C.DGreen + C.Bold + "CLICK " + C.Aqua + " to set deathmath border radius").build();

    public DeathmatchBorder(WorldInfo world, BorderCustomData customData) {
        super(world);

        _borderCustomData = customData;
    }

    @Override
    public ArrayList<ItemStack> getButtons() {
        return new ArrayList<ItemStack>();
    }

    public Location getDeathCenter() {
        if (((CircleBorder) _borderCustomData.getBorder()).getCenter() == null)
            return null;

        return ((CircleBorder) _borderCustomData.getBorder()).getCenter().getValue().getLocation().add(0.5, 0, 0.5);
    }

    public String getMissing() {
        return null;
    }

    /**
     * Returns string if config is incomplete, null if config is ready
     */
    public String getMissing(SurvivalGamesCustomData customData) {
        if (getDeathCenter() == null)
            return null;

        if (UtilLoc.getDistance2d(getWorld().getSpawnLocation(), getDeathCenter()) >= _radius) {
            return "World spawn isn't inside the Deathmatch!";
        }

        if (_radius < 6) {
            return "Deathmatch is too small";
        }

        for (ArrayList<Pair<Block, FakeEntity>> teams : customData.getTeams().values()) {
            int i = 0;

            for (Pair<Block, FakeEntity> spawns : teams) {
                if (!customData.isInside(spawns.getKey().getLocation())) {
                    return "Spawnpoint " + i + " is not inside Deathmatch";
                }

                i++;
            }
        }

        return null;
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

        tools.add(_setBorder);

        return tools;
    }

    public boolean isInside(Location location) {
        return getDeathCenter() != null && UtilLoc.getDistance(location, getDeathCenter()) < _radius;
    }

    @Override
    public void loadConfig(HashMap<String, ArrayList<String>> hashmap) {
        if (!hashmap.containsKey("Deathmatch"))
            return;

        _radius = Integer.parseInt(hashmap.get("Deathmatch").get(0));
    }

    public void loadConfig(YamlConfiguration config) {
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

        if (getDeathCenter() == null) {
            player.sendMessage(C.Red + "Need to set border center first");
            return;
        }

        if (block == null)
            return;

        _radius = (int) Math.ceil(UtilLoc.getDistance2d(block.getLocation().add(0.5, 0, 0.5), getDeathCenter()));

        if (_radius < 10)
            _radius = 10;

        for (Entry<Player, ArrayList<Block>> entry : _blocks.entrySet()) {
            Player p = entry.getKey();

            for (Block b : entry.getValue()) {
                p.sendBlockChange(b.getLocation(), Material.AIR, (byte) 0);
            }
        }

        _blocks.clear();

        Announce(C.Gold + player.getName() + " has set the deathmatch border radius to " + _radius);
    }

    public void onTick() {
        if (getDeathCenter() == null)
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

                        double dist = UtilLoc.getDistance2d(blockLoc, getDeathCenter()) - _radius;

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
        if (getDeathCenter() == null)
            return;

        hashmap.put("Deathmatch", new ArrayList<String>(Arrays.asList("" + _radius)));
    }

    public void saveConfig(YamlConfiguration config) {
    }
}
