package me.libraryaddict.hub.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Objects;

import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.hub.ServerInventory;
import me.libraryaddict.redis.operations.RedisSwitchServer;

public class Portal {
    private Vector<Block> _blocks = new Vector<Block>();
    private Server _currentServer;
    private ServerType _gameType;
    private Hologram _hologram;
    private JavaPlugin _hub;
    private ArrayList<Server> _servers;
    private Vector<Block> _signs = new Vector<Block>();

    public Portal(JavaPlugin plugin, ConfigurationSection configSection, ArrayList<Server> servers) {
        _gameType = ServerType.valueOf(configSection.getName());
        _servers = servers;
        _hub = plugin;

        World world = Bukkit.getWorld("world");

        world.setTime(18000);

        for (String block : configSection.getStringList("Region")) {
            String[] cords = block.split(",");

            Block b = world.getBlockAt(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));

            _blocks.add(b);
        }

        for (String block : configSection.getStringList("Signs")) {
            String[] cords = block.split(",");

            Block b = world.getBlockAt(Integer.parseInt(cords[0]), Integer.parseInt(cords[1]), Integer.parseInt(cords[2]));

            _signs.add(b);
        }

        if (configSection.contains("Holograms")) {
            for (String block : configSection.getStringList("Holograms")) {
                String[] cords = block.split(",");

                Location loc = new Location(world, Double.parseDouble(cords[0]), Double.parseDouble(cords[1]),
                        Double.parseDouble(cords[2]), Float.parseFloat(cords[3]), Float.parseFloat(cords[4]));

                _hologram = new Hologram(loc.add(0, -0.285D, 0)).start();
            }
        }
    }

    public void activate(Player player) {
        if (_currentServer == null || !Recharge.canUse(player, "Portal"))
            return;

        Recharge.use(player, "Portal", 4000);

        Server server = _currentServer;

        if (server.isFull() && !server.isInProgress() && !_gameType.isOverloadSupport()) {
            player.sendMessage(C.Red + "This server is full!");
            return;
        }

        UUID uuid = player.getUniqueId();
        String serverName = server.getName();

        new BukkitRunnable() {
            public void run() {
                new RedisSwitchServer(uuid, serverName);
            }
        }.runTaskAsynchronously(_hub);
    }

    public ServerType getType() {
        return _gameType;
    }

    public boolean isInside(Location loc) {
        return _blocks.contains(loc.getBlock()) || _blocks.contains(loc.getBlock().getRelative(0, 1, 0));
    }

    public boolean isSign(Block block) {
        return _signs.contains(block);
    }

    public void openMenu(Player player) {
        new ServerInventory(player, _servers).openInventory();
    }

    public void updateBlocks() {
        Iterator<Server> itel = _servers.iterator();

        while (itel.hasNext()) {
            Server server = itel.next();

            if (!server.isValid()) {
                itel.remove();
                continue;
            }
        }

        Collections.sort(_servers);

        if (!_servers.isEmpty()) {
            _currentServer = _servers.get(0);

            if (_currentServer.getPlayers() == 0 && _servers.size() > 1) {
                Server server = _servers.get(1);

                if (!server.isInProgress() && !server.isFull()) {
                    _currentServer = server;
                }
            }
        }
        else {
            _currentServer = null;
        }

        Material material = Material.BEDROCK;

        String[] text = new String[] {
                C.Blue + _gameType.getShortened(), "No Server", "No Server", "Right click me!"
        };

        int players = 0;

        for (Server server : _servers) {
            players += server.getPlayers();
        }

        String[] hologram = new String[] {
                C.Gold + _gameType.getName(), "No Server", "No Server", C.Aqua + players + C.DAqua + " players playing!"
        };

        if (_currentServer != null) {
            text[1] = C.Aqua + "Players: " + _currentServer.getPlayers() + "/" + _gameType.getMaxPlayers();
            hologram[1] = text[1];

            if (!getType().isGame()) {
                if (!_currentServer.isFull()) {
                    material = Material.WATER;
                    text[2] = C.DGreen + "Join!";
                    hologram[2] = C.DGreen + "Ready to join!";
                }
                else {
                    material = Material.LAVA;
                    text[2] = C.DRed + "Server full!";
                    hologram[2] = C.DRed + "The server is full!";
                }
            }
            else if (_currentServer.isInProgress()) {
                material = Material.LAVA;
                text[2] = C.Red + "In Progress";
                hologram[2] = text[2];
            }
            else if (_currentServer.getPlayers() >= _gameType.getMinPlayers()) {
                material = Material.WATER;
                text[2] = C.DGreen + "Starts in " + UtilNumber
                        .getTimeAbbr(-Math.floorDiv(System.currentTimeMillis() - _currentServer.getGameStarts(), 1000));
                hologram[2] = C.Green + "Starts in "
                        + UtilNumber.getTime(-Math.floorDiv(System.currentTimeMillis() - _currentServer.getGameStarts(), 1000));
            }
            else {
                material = Material.WATER;
                text[2] = C.DGreen + "Waiting..";
                hologram[2] = C.DGreen + "Waiting for players..";
            }
        }

        for (Block block : _blocks) {
            if (block.getType() == material)
                continue;

            block.setType(Material.AIR);
        }

        for (Block block : _blocks) {
            if (block.getType() == material)
                continue;

            block.setType(material);
        }

        if (_hologram != null) {
            _hologram.setText(hologram);
        }

        for (Block block : _signs) {
            if (!(block.getState() instanceof Sign))
                continue;

            Sign sign = (Sign) block.getState();

            if (Objects.equal(sign.getLines(), text))
                continue;

            for (int i = 0; i < text.length; i++) {
                sign.setLine(i, text[i]);
            }

            sign.update();
        }
    }
}
