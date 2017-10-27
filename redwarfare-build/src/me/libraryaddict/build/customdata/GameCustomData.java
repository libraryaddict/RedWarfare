package me.libraryaddict.build.customdata;

import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.fakeentity.EntityInteract;
import me.libraryaddict.core.fakeentity.FakeEntity;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilBlock;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class GameCustomData extends BorderCustomData {
    private ItemStack[] _addSpawns;
    private ItemStack _icon;
    private ItemStack _removeSpawn = new ItemBuilder(Material.LEATHER_CHESTPLATE).setTitle(C.Blue + "Remove Spawn")
            .addLore(C.Green + "Right click on a block to remove that spawn!").build();
    private ItemStack _setIcon = new ItemBuilder(Material.EYE_OF_ENDER).setTitle(C.Gold + "Set map icon").addLore(
            C.Yellow + "Click on this while holding an item on your main hand, and that item will be set as the " +
                    "map's" + " icon")
            .build();
    private HashMap<TeamSettings, ArrayList<Pair<Block, FakeEntity>>> _teams = new HashMap<TeamSettings,
            ArrayList<Pair<Block, FakeEntity>>>();
    private TeamSettings[] _usableTeams;

    public GameCustomData(WorldInfo world, Class<? extends CustomData> defaultBorder, TeamSettings... usableTeams) {
        super(world, defaultBorder);

        _usableTeams = usableTeams;

        _addSpawns = new ItemStack[usableTeams.length];

        for (int i = 0; i < _addSpawns.length; i++) {
            TeamSettings settings = _usableTeams[i];

            _addSpawns[i] = new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(settings.getBukkitColor())
                    .setTitle(settings.getColor() + "Add " + settings.getName() + " Spawn").build();
        }
    }

    public ItemStack[] getAddSpawns() {
        return _addSpawns;
    }

    @Override
    public ArrayList<ItemStack> getButtons() {
        ArrayList<ItemStack> tools = super.getButtons();

        tools.add(_setIcon);

        return tools;
    }

    public abstract int getMaxSpawns();

    public abstract int getMaxTeams();

    public abstract int getMinSpawns();

    public abstract int getMinTeams();

    public String getMissing() {
        if (_teams.size() < getMinTeams()) {
            return "Not enough teams";
        }

        if (_teams.size() > getMaxTeams()) {
            return "Too many teams";
        }

        for (Entry<TeamSettings, ArrayList<Pair<Block, FakeEntity>>> teamEntry : _teams.entrySet()) {
            if (teamEntry.getValue().size() < getMinSpawns()) {
                return teamEntry.getKey().getName() + " does not have enough spawn points";
            }

            if (teamEntry.getValue().size() > getMaxSpawns()) {
                return teamEntry.getKey().getName() + " has too many spawn points";
            }

            int i = 1;

            for (Pair<Block, FakeEntity> pair : teamEntry.getValue()) {
                Block b = pair.getKey();

                String missing = isValidSpawn(b, " " + i + " for " + teamEntry.getKey().getName());

                if (missing != null)
                    return missing;

                i++;
            }
        }

        // if (_icon == null)
        // return "The map icon has not been set";

        return super.getMissing();
    }

    public HashMap<TeamSettings, ArrayList<Pair<Block, FakeEntity>>> getTeams() {
        return _teams;
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = super.getTools();

        tools.add(_removeSpawn);

        for (ItemStack item : _addSpawns) {
            tools.add(item);
        }

        // tools.add(_setIcon);

        return tools;
    }

    public TeamSettings[] getUsableTeams() {
        return _usableTeams;
    }

    public TeamSettings[] getUsedTeams() {
        return _teams.keySet().toArray(new TeamSettings[0]);
    }

    private String isValidSpawn(Block b, String id) {
        if (UtilBlock.nonSolid(b.getRelative(BlockFace.DOWN)))
            return "Player spawn" + id + " is standing on air?";

        if (!UtilBlock.nonSolid(b))
            return "Player spawn" + id + " is spawning inside a block?";

        if (!UtilBlock.nonSolid(b.getRelative(BlockFace.UP)))
            return "Player spawn" + id + " is spawning inside a block?";

        if (!isInside(b.getLocation()))
            return "Player spawn" + id + " is not inside the border";

        return null;
    }

    public void loadConfig(YamlConfiguration config) {
        super.loadConfig(config);

        if (config.contains("Icon")) {
            String[] s = config.getString("Icon").split(":");

            _icon = new ItemStack(Material.valueOf(s[0]), 1, (short) Integer.parseInt(s[1]));

            // _setIcon.setType(_icon.getType());
            // _setIcon.setDurability(_icon.getDurability());
        }

        ConfigurationSection teams = config.getConfigurationSection("Teams");

        if (teams == null)
            return;

        for (String teamName : teams.getKeys(false)) {
            TeamSettings team = TeamSettings.valueOf(teamName);

            ArrayList<Pair<Block, FakeEntity>> spawns = new ArrayList<Pair<Block, FakeEntity>>();

            for (String spawn : teams.getStringList(teamName + ".Spawns")) {
                Block block = getBlock(spawn);

                spawns.add(Pair.of(block, spawnStand(team, block)));
            }

            _teams.put(team, spawns);
        }

        recalculateSpawns();
    }

    @Override
    public void onButtonClick(Player player, ItemStack item) {
        super.onButtonClick(player, item);

        ItemStack setIcon = _setIcon.clone();

        setIcon.setType(item.getType());
        setIcon.setDurability(item.getDurability());

        if (!UtilInv.isSimilar(item, setIcon))
            return;

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You do not have permission to modify this map!");
            return;
        }

        ItemStack holding = player.getInventory().getItemInMainHand();

        if (holding == null || holding.getType() == Material.AIR) {
            player.sendMessage(C.Red + "You are not holding anything!");
            return;
        }

        _icon = holding.clone();
        _icon.setAmount(1);

        // _setIcon.setType(_icon.getType());
        // _setIcon.setDurability(_icon.getDurability());

        Announce(C.Gold + player.getName() + " has changed the icon for the map to " + _icon.getType().name());
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!isInMap(player))
            return;

        TeamSettings owner = null;
        Pair<Block, FakeEntity> pair = null;

        for (TeamSettings team : _usableTeams) {
            if (!_teams.containsKey(team))
                continue;

            for (Pair<Block, FakeEntity> values : _teams.get(team)) {
                if (!values.getValue().equals(event.getRightClicked())) {
                    continue;
                }

                pair = values;
                owner = team;
            }
        }

        if (owner == null) {
            return;
        }

        event.setCancelled(true);

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You do not have permission to build in this world");
            return;
        }

        if (!Recharge.canUse(player, "Tool"))
            return;

        if (!UtilInv.isHolding(player, _removeSpawn)) {
            return;
        }

        Recharge.use(player, "Tool", 100);

        pair.getValue().stop();

        _teams.get(owner).remove(pair);

        if (_teams.get(owner).isEmpty()) {
            _teams.remove(owner);
        }

        Announce(C.Gold + player.getName() + " has removed a spawn from " + owner.getName());

        recalculateSpawns();
    }

    @EventHandler
    public void onInteractGame(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (!isInMap(player))
            return;

        ItemStack item = event.getItem();
        Block block = event.getClickedBlock().getRelative(BlockFace.UP);

        if (!Recharge.canUse(player, "Tool"))
            return;

        if (UtilInv.isSimilar(item, _removeSpawn)) {
            Recharge.use(player, "Tool", 100);

            if (!isBuilder(player)) {
                player.sendMessage(C.Red + "You do not have permission to build in this world");
                return;
            }

            event.setCancelled(true);

            TeamSettings owner = null;
            Pair<Block, FakeEntity> pair = null;

            for (TeamSettings team : _usableTeams) {
                if (!_teams.containsKey(team))
                    continue;

                for (Pair<Block, FakeEntity> values : _teams.get(team)) {
                    if (!values.getKey().equals(block)) {
                        continue;
                    }

                    pair = values;
                    owner = team;
                }
            }

            if (owner == null) {
                player.sendMessage(C.Red + "That is not a spawn point!");
                return;
            }

            pair.getValue().stop();

            _teams.get(owner).remove(pair);

            if (_teams.get(owner).isEmpty()) {
                _teams.remove(owner);
            }

            recalculateSpawns();
        } else {
            TeamSettings team = null;

            for (int i = 0; i < _addSpawns.length; i++) {
                if (!UtilInv.isSimilar(item, _addSpawns[i])) {
                    continue;
                }

                team = _usableTeams[i];
            }

            if (team == null)
                return;

            Recharge.use(player, "Tool", 100);

            if (!isBuilder(player)) {
                player.sendMessage(C.Red + "You do not have permission to build in this world");
                return;
            }

            event.setCancelled(true);

            for (ArrayList<Pair<Block, FakeEntity>> spawns : _teams.values()) {
                for (Pair<Block, FakeEntity> pair : spawns) {
                    if (!pair.getKey().equals(block))
                        continue;

                    player.sendMessage(C.Red + "This block has already been set as a spawn point");
                    return;
                }
            }

            if (!isBorderSet()) {
                player.sendMessage(C.Red + "Please set the border first");
                return;
            }

            if (!_teams.containsKey(team)) {
                _teams.put(team, new ArrayList<Pair<Block, FakeEntity>>());
            }

            if (_teams.get(team).size() >= getMaxSpawns()) {
                player.sendMessage(C.Red + "Too many spawns set for this team, max size is: " + getMaxSpawns());
                return;
            }

            String invalid = isValidSpawn(block, "");

            if (invalid != null) {
                player.sendMessage(C.Red + invalid);
                return;
            }

            _teams.get(team).add(Pair.of(block, spawnStand(team, block)));

            recalculateSpawns();
        }
    }

    private void recalculateSpawns() {
        for (TeamSettings team : _teams.keySet()) {

            Collections.sort(_teams.get(team), new Comparator<Pair<Block, FakeEntity>>() {
                public int compare(Pair<Block, FakeEntity> o1, Pair<Block, FakeEntity> o2) {
                    Block b1 = o1.getKey();
                    Block b2 = o2.getKey();

                    if (b1.getX() != b2.getX())
                        return Integer.compare(b1.getX(), b2.getX());

                    if (b2.getZ() != b2.getZ())
                        return Integer.compare(b1.getZ(), b2.getZ());

                    if (b2.getY() != b2.getY())
                        return Integer.compare(b1.getY(), b2.getY());

                    return 0;
                }
            });

            int i = 1;

            for (Pair<Block, FakeEntity> pair : _teams.get(team)) {
                FakeEntity armorstand = pair.getValue();

                armorstand.setMetadata(MetaIndex.ENTITY_CUSTOM_NAME, team.getColor() + C.Bold + "Spawn " + i++);
                armorstand.setMetadata(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE, true);

                Location loc = armorstand.getLocation();
                float f = loc.getYaw();

                loc.setDirection(UtilLoc.getDirection2d(loc, loc.getWorld().getSpawnLocation()));

                if (loc.getYaw() == f)
                    continue;

                armorstand.setLocation(loc);
            }
        }
    }

    public void saveConfig(YamlConfiguration config) {
        super.saveConfig(config);

        if (_icon != null)
            config.set("Icon", _icon.getType().name() + ":" + _icon.getDurability());

        ConfigurationSection teams = config.createSection("Teams");

        for (Entry<TeamSettings, ArrayList<Pair<Block, FakeEntity>>> team : _teams.entrySet()) {
            ArrayList<String> spawns = new ArrayList<String>();

            for (Pair<Block, FakeEntity> block : team.getValue()) {
                spawns.add(toString(block.getKey()));
            }

            if (!spawns.isEmpty()) {
                teams.createSection(team.getKey().name()).set("Spawns", spawns);
            }
        }
    }

    public void spawnChanged() {
        super.spawnChanged();

        recalculateSpawns();
    }

    private FakeEntity spawnStand(TeamSettings owner, Block block) {
        Location loc = block.getLocation().add(0.5, 0, 0.5);
        loc.setDirection(UtilLoc.getDirection2d(loc, loc.getWorld().getSpawnLocation()));

        int i = 0;

        ItemStack[] armor = new ItemStack[4];

        for (Material mat : new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS,
                Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET}) {
            armor[i++] = new ItemBuilder(mat).setColor(owner.getBukkitColor()).build();
        }

        FakeEntity fakeEntity = new FakeEntity(loc, EntityType.ARMOR_STAND);
        fakeEntity.setMetadata(MetaIndex.ARMORSTAND_META, (byte) 1);

        fakeEntity.setArmor(armor);

        fakeEntity.setInteract(new EntityInteract() {

            @Override
            public void onInteract(Player player, InteractType interactType) {
                if (interactType != InteractType.ATTACK)
                    return;

                Pair<Block, FakeEntity> pair = null;

                for (TeamSettings team : _usableTeams) {
                    if (!_teams.containsKey(team))
                        continue;

                    for (Pair<Block, FakeEntity> values : _teams.get(team)) {
                        if (values.getValue() != fakeEntity) {
                            continue;
                        }

                        pair = values;
                    }
                }

                if (!isBuilder(player)) {
                    player.sendMessage(C.Red + "You do not have permission to build in this world");
                    return;
                }

                if (!Recharge.canUse(player, "Tool"))
                    return;

                Recharge.use(player, "Tool", 100);

                pair.getValue().stop();

                _teams.get(owner).remove(pair);

                if (_teams.get(owner).isEmpty()) {
                    _teams.remove(owner);
                }

                recalculateSpawns();
            }
        });

        fakeEntity.start();

        return fakeEntity;
    }

    public void unloadData() {
        super.unloadData();

        for (ArrayList<Pair<Block, FakeEntity>> value : _teams.values()) {
            for (Pair<Block, FakeEntity> pair : value) {
                pair.getValue().stop();
            }
        }
    }
}
