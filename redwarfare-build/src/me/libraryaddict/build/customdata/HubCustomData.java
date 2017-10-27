package me.libraryaddict.build.customdata;

import me.libraryaddict.build.customdata.borders.SquareBorder;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.fakeentity.FakeEntity;
import me.libraryaddict.core.fakeentity.FakeEntityData;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ViewDist;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class HubCustomData extends BorderCustomData {
    private ItemStack[] _hologramItems;
    private HashMap<ServerType, Pair<Hologram, Location>> _portalHolograms = new HashMap<ServerType, Pair<Hologram,
            Location>>();
    private HashMap<ServerType, ArrayList<Block>> _portalRegion = new HashMap<ServerType, ArrayList<Block>>();
    private HashMap<ServerType, ArrayList<Block>> _portalSigns = new HashMap<ServerType, ArrayList<Block>>();
    private FakeEntity _redeemNPC;
    private ItemStack[] _regionItems;
    private ServerType[] _serverTypes;
    private ItemStack _setTokenNPC = new ItemBuilder(Material.GOLD_INGOT).setTitle(C.Gold + "Set voting NPC")
            .addLore(C.Yellow + "Click to spawn an redeem NPC where you're standing").build();
    private ItemStack[] _signItems;
    private int _tick;

    public HubCustomData(WorldInfo world) {
        super(world, SquareBorder.class);

        _serverTypes = new ServerType[]{ServerType.SearchAndDestroy, ServerType.SurvivalGames, ServerType.Build,
                ServerType.Disaster};

        _regionItems = new ItemStack[_serverTypes.length];
        _signItems = new ItemStack[_serverTypes.length];
        _hologramItems = new ItemStack[_serverTypes.length];

        for (int i = 0; i < _serverTypes.length; i++) {
            ServerType type = _serverTypes[i];

            ItemStack regionItem = new ItemBuilder(Material.BLAZE_ROD)
                    .setTitle(C.Purple + "Add/Remove " + type.getName() + " portal block")
                    .addLore("Right click a block to add/remove it").build();
            ItemStack signItem = new ItemBuilder(Material.ARROW)
                    .setTitle(C.Gold + "Modify " + type.getName() + " portal signs")
                    .addLore("Right click a sign to add/remove it from that portal").build();
            ItemStack holoItem = new ItemBuilder(Material.PAPER).setTitle("Set " + type.getName() + " hologram")
                    .build();

            _regionItems[i] = regionItem;
            _signItems[i] = signItem;
            _hologramItems[i] = holoItem;
        }
    }

    @Override
    public ArrayList<ItemStack> getButtons() {
        ArrayList<ItemStack> tools = super.getButtons();

        tools.add(_setTokenNPC);

        return tools;
    }

    @Override
    public String getMissing() {
        for (ServerType serverType : _serverTypes) {
            if (!_portalRegion.containsKey(serverType)) {
                return serverType.getName() + " does not have a portal";
            }

            ArrayList<Block> pair = _portalRegion.get(serverType);

            if (pair.isEmpty()) {
                return serverType.getName() + " does not have a portal";
            }
        }

        for (ServerType serverType : _serverTypes) {
            if (!_portalSigns.containsKey(serverType)) {
                return serverType.getName() + " has no signs assigned to the portal";
            }
        }

        if (_redeemNPC == null) {
            return "No redeem NPC found!";
        }

        return super.getMissing();
    }

    @Override
    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = super.getTools();

        for (int i = 0; i < _serverTypes.length; i++) {
            tools.add(_regionItems[i]);
            tools.add(_signItems[i]);
            tools.add(_hologramItems[i]);
        }

        return tools;
    }

    @Override
    public void loadConfig(HashMap<String, ArrayList<String>> config) {
        super.loadConfig(config);

        if (config.containsKey("Redeem NPC")) {
            _redeemNPC = new FakeEntity(getLocation(config.get("Redeem NPC").get(0)),
                    new FakeEntityData(EntityType.BLAZE));
            _redeemNPC.start();
        }
    }

    @Override
    public void loadConfig(YamlConfiguration config) {
        super.loadConfig(config);

        if (!config.contains("Portals"))
            return;

        for (String key : config.getConfigurationSection("Portals").getKeys(false)) {
            ServerType type = ServerType.valueOf(key);

            if (type == null) {
                System.out.println("Unrecognized servertype " + key);
                continue;
            }

            boolean found = false;

            for (ServerType t : _serverTypes) {
                if (type != t)
                    continue;

                found = true;
            }

            if (!found) {
                System.out.println("Server " + key + " isn't a option in the hub");
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection("Portals." + key);

            if (section.contains("Region")) {
                List<String> list = section.getStringList("Region");

                ArrayList<Block> blocks = new ArrayList<Block>();

                for (String block : list) {
                    blocks.add(getBlock(block));
                }

                _portalRegion.put(type, blocks);
            }

            if (section.contains("Signs")) {
                ArrayList<Block> signs = new ArrayList<Block>();

                for (String string : section.getStringList("Signs")) {
                    signs.add(getBlock(string));
                }

                _portalSigns.put(type, signs);
            }

            if (section.contains("Holograms")) {
                Location loc = getLocation(section.getStringList("Holograms").get(0));

                Pair<Hologram, Location> pair = Pair
                        .of(new Hologram(loc, type.getName() + " Info Display").start(), loc);

                _portalHolograms.put(type, pair);
            }
        }
    }

    @Override
    public void onButtonClick(Player player, ItemStack item) {
        super.onButtonClick(player, item);

        if (!UtilInv.isSimilar(item, _setTokenNPC))
            return;

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You do not have permission to modify this map!");
            return;
        }

        Location loc = player.getLocation();

        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlock().getType() == Material.AIR ? loc.getBlockY() : loc.getY());
        loc.setZ(loc.getBlockZ() + 0.5);

        if (_redeemNPC == null) {
            _redeemNPC = new FakeEntity(loc, new FakeEntityData(EntityType.BLAZE)).start();
        } else {
            _redeemNPC.setLocation(loc);
        }
    }

    @EventHandler
    public void onHoloSet(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        ServerType server = null;

        for (int i = 0; i < _hologramItems.length; i++) {
            if (!UtilInv.isSimilar(event.getItem(), _hologramItems[i]))
                continue;

            server = _serverTypes[i];
        }

        if (server == null)
            return;

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You can't build in this world!");
            return;
        }

        Block block = event.getClickedBlock();
        BlockFace face = event.getBlockFace();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);

        if (face != BlockFace.UP) {
            loc.add(face.getModX() / 2D, face.getModY() / 2D, face.getModZ() / 2D);
        }

        if (_portalHolograms.containsKey(server)) {
            Pair<Hologram, Location> pair = _portalHolograms.get(server);

            pair.setValue(loc);

            pair.getKey().setLocation(loc);
        } else {
            Pair<Hologram, Location> pair = Pair.of(new Hologram(loc, server.getName() + " Info Display").start(), loc);

            _portalHolograms.put(server, pair);
        }
    }

    @EventHandler
    public void onParticlesTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        int i = 0;
        _tick = ++_tick % 16;

        for (ServerType serverType : _serverTypes) {
            ParticleColor color = ParticleColor.values()[i++];

            if (_portalSigns.containsKey(serverType)) {
                for (Block block : _portalSigns.get(serverType)) {
                    if (!(block.getState() instanceof Sign))
                        continue;

                    Sign sign = (Sign) block.getState();

                    for (int a = 0; a < 4; a++)
                        sign.setLine(a, "|" + UtilString.repeat(" ", (_tick + (a * 3)) % 16) + "!" + UtilString
                                .repeat(" ", 16 - (_tick + (a * 3)) % 16) + "|");

                    sign.update();

                    UtilParticle.playParticle(block.getLocation()
                                    .add(UtilMath.rr(0.1, 0.9), UtilMath.rr(0.1, 0.9), UtilMath.rr(0.1, 0.9)), color,
                            ViewDist.SHORT);
                }
            }

            if (_portalRegion.containsKey(serverType)) {
                ArrayList<Block> pair = _portalRegion.get(serverType);

                for (Block b : pair) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            if (Math.abs(x) + Math.abs(z) != 1)
                                continue;

                            Block b3 = b.getRelative(x, 0, z);

                            if (UtilBlock.solid(b3))
                                continue;

                            for (int a = 0; a < 2; a++) {
                                Location l = b.getLocation().add(0.5, 0, 0.5).add(x / 2D, UtilMath.rr(1), z / 2D);

                                l.add(x == 0 ? UtilMath.rr(-0.5, 0.5) : 0, 0, z == 0 ? UtilMath.rr(-0.5, 0.5) : 0);

                                UtilParticle.playParticle(l, color, ViewDist.SHORT);
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPortalRegionInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        ItemStack item = event.getItem();
        ServerType type = null;

        for (int i = 0; i < _serverTypes.length; i++) {
            if (!UtilInv.isSimilar(item, _regionItems[i]))
                continue;

            type = _serverTypes[i];
        }

        if (type == null)
            return;

        event.setCancelled(true);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You can't build in this world!");
            return;
        }

        ArrayList<Block> blocks = new ArrayList<Block>();

        if (_portalRegion.containsKey(type)) {
            blocks = _portalRegion.get(type);
        }

        Block block = event.getClickedBlock();

        for (ArrayList<Block> otherBlocks : _portalRegion.values()) {
            if (otherBlocks == blocks)
                continue;

            if (!otherBlocks.contains(block))
                continue;

            player.sendMessage(C.Red + "Another portal has already claimed that block!");
            return;
        }

        if (blocks.contains(block)) {
            blocks.remove(block);
        } else {
            /*if (blocks.size() > 40)
            {
                player.sendMessage(C.Red + "Don't you think that is a bit.. Obsessive?");
                return;
            }*/

            blocks.add(block);
        }

        if (blocks.isEmpty()) {
            _portalRegion.remove(type);
        } else {
            _portalRegion.put(type, blocks);
        }
    }

    @EventHandler
    public void onPortalSignInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            return;

        Player player = event.getPlayer();

        if (!isInMap(player))
            return;

        ItemStack item = event.getItem();
        ServerType type = null;

        for (int i = 0; i < _serverTypes.length; i++) {
            if (!UtilInv.isSimilar(item, _signItems[i]))
                continue;

            type = _serverTypes[i];
        }

        if (type == null)
            return;

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You can't build in this world!");
            return;
        }

        Block block = event.getClickedBlock();

        if (!(block.getState() instanceof Sign)) {
            player.sendMessage(C.Red + "That's not a sign!");
            return;
        }

        for (Entry<ServerType, ArrayList<Block>> entry : _portalSigns.entrySet()) {
            if (entry.getKey() == type)
                continue;

            if (entry.getValue().contains(block)) {
                entry.getValue().remove(block);
                return;
            }
        }

        ArrayList<Block> signs = _portalSigns.getOrDefault(type, new ArrayList<Block>());

        if (!_portalSigns.containsKey(type)) {
            _portalSigns.put(type, signs);
        }

        if (signs.contains(block)) {
            signs.remove(block);

            if (signs.isEmpty()) {
                _portalSigns.remove(type);
            }
        } else {
            signs.add(block);
        }
    }

    @Override
    public void saveConfig(HashMap<String, ArrayList<String>> config) {
        super.saveConfig(config);

        if (_redeemNPC != null)
            config.put("Redeem NPC", new ArrayList<String>(Arrays.asList(toString(_redeemNPC.getLocation()))));
    }

    @Override
    public void saveConfig(YamlConfiguration config) {
        super.saveConfig(config);

        if (_portalRegion.isEmpty() && _portalSigns.isEmpty())
            return;

        ConfigurationSection portals = config.createSection("Portals");

        for (ServerType server : _serverTypes) {
            ConfigurationSection section = portals.createSection(server.getName());

            if (_portalRegion.containsKey(server)) {
                ArrayList<String> region = new ArrayList<String>();

                ArrayList<Block> pair = _portalRegion.get(server);

                for (Block block : pair)
                    region.add(toString(block));

                section.set("Region", region);
            }

            if (_portalSigns.containsKey(server)) {
                ArrayList<String> region = new ArrayList<String>();

                for (Block block : _portalSigns.get(server)) {
                    region.add(toString(block));
                }

                section.set("Signs", region);
            }

            if (_portalHolograms.containsKey(server)) {
                section.set("Holograms",
                        new ArrayList<String>(Arrays.asList(toString(_portalHolograms.get(server).getValue()))));
            }
        }
    }

    @Override
    public void unloadData() {
        super.unloadData();

        for (Pair<Hologram, Location> holo : _portalHolograms.values()) {
            holo.getKey().stop();
        }

        if (_redeemNPC != null)
            _redeemNPC.stop();
    }
}
