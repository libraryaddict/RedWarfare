package me.libraryaddict.build.customdata;

import me.libraryaddict.build.inventories.SetMapTypeInventory;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.build.types.MapType;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class WorldCustomData extends CustomData implements Listener {
    private ItemStack _setInfo = new ItemBuilder(Material.PAPER).setTitle(C.Gold + "Set map info")
            .addLore(C.Blue + "Click on this to be sent some commands to set the map's info").build();
    private ItemStack _setMapType = new ItemBuilder(Material.EMPTY_MAP).setTitle(C.DGreen + "Set Map Type")
            .addLore(C.Blue + "Click on this to set the map type").build();
    private ItemStack _setSpawn = new ItemBuilder(Material.APPLE).setTitle(C.Blue + "Set Spawn")
            .addLore(C.Green + "The spawn is where the spectators will join this world").build();
    private Hologram _spawnHolo;

    public WorldCustomData(WorldInfo world) {
        super(world);
    }

    public ArrayList<ItemStack> getButtons() {
        ArrayList<ItemStack> buttons = new ArrayList<ItemStack>();

        buttons.add(_setInfo);
        buttons.add(_setMapType);

        return buttons;
    }

    @Override
    public String getMissing() {
        return null;
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = new ArrayList<ItemStack>();

        tools.add(_setSpawn);

        return tools;
    }

    public void loadConfig(HashMap<String, ArrayList<String>> hashmap) {
    }

    public void loadConfig(YamlConfiguration config) {
        _spawnHolo = new Hologram(getWorld().getSpawnLocation().add(0.5, 1, 0.5), "World Spawn");
        _spawnHolo.start();
    }

    public void onButtonClick(Player player, ItemStack item) {
        super.onButtonClick(player, item);

        if (UtilInv.isSimilar(item, _setInfo)) {
            player.sendMessage(C.Gold + "Click on one of the following to set the information");

            FancyMessage fancyChat = new FancyMessage(C.Gold + C.Bold + "Map Description ")
                    .tooltip("Set the description of the map").suggest("/setinfo description ");
            fancyChat.then(C.Blue + C.Bold + "Map Authors ").tooltip("Set the displayed authors of the map")
                    .suggest("/setinfo authors ");
            fancyChat.then(C.DGreen + C.Bold + "Map Name ").tooltip("Set the name of the map")
                    .suggest("/setinfo name ");

            fancyChat.send(player);
        }

        if (UtilInv.isSimilar(item, _setMapType)) {
            new SetMapTypeInventory(player, getInfo()).openInventory();

            player.sendMessage(
                    C.Red + "When switching map types, you will lose any existing data on the current map type");
        }
    }

    @EventHandler
    public void onInteractWorldSpawn(PlayerInteractEvent event) {
        if (!UtilInv.isSimilar(_setSpawn, event.getItem())) {
            return;
        }

        if (!event.getAction().name().contains("RIGHT"))
            return;

        Player player = event.getPlayer();

        if (!isInMap(player))
            return;

        if (!Recharge.canUse(player, "Tool"))
            return;

        Recharge.use(player, "Tool", 100);

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You don't have permission to use this tool!");
            return;
        }

        event.setCancelled(true);

        Block block = player.getLocation().getBlock();

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            block = event.getClickedBlock().getRelative(0, 1, 0);
        }

        World world = getWorld();

        world.setSpawnLocation(block.getX(), block.getY(), block.getZ());

        _spawnHolo.setLocation(block.getLocation().add(0.5, 0, 0.5));

        Announce(C.Gold + player.getName() + " has changed the spawn location");

        spawnChanged();
    }

    @EventHandler
    public void onJoin(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (!getInfo().getData().hasRank(player, MapRank.ADMIN))
            return;

        if (player.getWorld() != getWorld())
            return;

        if (getData().getDescription() == null || getData().getDescription().length() < 3) {
            player.sendMessage(C.Gold + "This map is missing a description, use /tools to set it");
        }

        if (getData().getMapType() == MapType.Unknown) {
            player.sendMessage(C.Gold + "This map is missing a maptype, use /tools to set it");
        }
    }

    public void saveConfig(HashMap<String, ArrayList<String>> hashmap) {
    }

    public void saveConfig(YamlConfiguration config) {
        HashMap<String, ArrayList<String>> custom = new HashMap<String, ArrayList<String>>();

        saveConfig(custom);

        for (String key : custom.keySet()) {
            config.set("Custom." + key, custom.get(key));
        }
    }

    public void spawnChanged() {
    }

    public void unloadData() {
        super.unloadData();

        if (_spawnHolo == null)
            return;

        _spawnHolo.stop();
    }
}
