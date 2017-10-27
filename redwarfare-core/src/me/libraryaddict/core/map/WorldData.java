package me.libraryaddict.core.map;

import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorldData {
    private String _author;
    private Location _borderCenter;
    private int _borderRadius;
    private HashMap<String, ArrayList<String>> _data = new HashMap<String, ArrayList<String>>();
    private String _description;
    private boolean _isSquare;
    private File _mapFile;
    private int _minX, _minZ, _minY, _maxX, _maxY, _maxZ;
    private String _name;
    private int _teamSize;
    private World _world;
    private File _worldFolder;

    public WorldData(File mapFile) {
        if (mapFile.isFile()) {
            _mapFile = mapFile;
        } else {
            _worldFolder = mapFile;
            loadData();

            for (World world : Bukkit.getWorlds()) {
                if (!world.getWorldFolder().equals(mapFile))
                    continue;

                loadData(world);
                break;
            }
        }
    }

    public WorldData(World world) {
        _worldFolder = world.getWorldFolder();

        loadData();
        loadData(world);
    }

    public void deleteFolder() {
        UtilFile.delete(getWorldFolder());
    }

    public String getAuthor() {
        return _author;
    }

    public ArrayList<String> getCustom(String key) {
        return _data.get(key);
    }

    public ArrayList<Block> getCustomBlocks(String key) {
        ArrayList<Block> list = new ArrayList<Block>();

        if (!_data.containsKey(key))
            return list;

        for (String s : _data.get(key)) {
            list.add(parseBlock(s));
        }

        return list;
    }

    public ArrayList<Location> getCustomLocs(String key) {
        ArrayList<Location> list = new ArrayList<Location>();

        for (String s : _data.get(key)) {
            list.add(parseLoc(s));
        }

        return list;
    }

    public ArrayList getData(String key) {
        return _data.get(key);
    }

    public String getDescription() {
        return _description;
    }

    public Location getInsideBorder(Location loc) {
        if (_isSquare)
            return getInsideSquareBorder(loc);

        return getInsideCircleBorder(loc);
    }

    private Location getInsideCircleBorder(Location loc) {
        Location toTele = loc.clone();

        Vector vec = UtilLoc.getDirection2d(loc, _borderCenter);
        vec.multiply(UtilLoc.getDistance(loc, _borderCenter) - (_borderRadius - 1));

        toTele.add(vec);

        return toTele;
    }

    private Location getInsideSquareBorder(Location loc) {
        Location toTele = loc.clone();

        if (getMinY() != getMaxY()) {
            if (getMinY() > loc.getY()) {
                // toTele.setY(getMinY());
            } else if (getMaxY() < loc.getY()) {
                toTele.setY(getMaxY() - 1);
            }
        }

        if (getMinX() > loc.getX()) {
            toTele.setX(getMinX() + 0.5);
        } else if (getMaxX() < loc.getX() + 1) {
            toTele.setX(getMaxX() + 0.5);
        }

        if (getMinZ() > loc.getZ()) {
            toTele.setZ(getMinZ() + 0.5);
        } else if (getMaxZ() < loc.getZ() + 1) {
            toTele.setZ(getMaxZ() + 0.5);
        }

        return toTele;
    }

    public Block getMaxBlock() {
        return _world.getBlockAt(getMaxX(), getMinY() == getMaxY() ? 256 : getMaxY(), getMaxZ());
    }

    public int getMaxX() {
        return _maxX;
    }

    public int getMaxY() {
        return _maxY;
    }

    public int getMaxZ() {
        return _maxZ;
    }

    public Block getMinBlock() {
        return _world.getBlockAt(getMinX(), getMinY() == getMaxY() ? 0 : getMinY(), getMinZ());
    }

    public int getMinX() {
        return _minX;
    }

    public int getMinY() {
        return _minY;
    }

    public int getMinZ() {
        return _minZ;
    }

    public String getName() {
        return _name;
    }

    public int getTeams() {
        return _teamSize;
    }

    public File getWorldFolder() {
        return _worldFolder;
    }

    public boolean isInsideBorder(Location loc) {
        if (_isSquare) {
            return UtilLoc.isInside(getMinX(), getMinY(), getMinZ(), getMaxX(), getMaxY(), getMaxZ(), loc, true);
        }

        loc = loc.getBlock().getLocation().add(0.5, 0.5, 0.5);

        return UtilLoc.getDistance2d(loc, _borderCenter) < _borderRadius + 0.5;
    }

    public boolean isYSet() {
        return getMinY() == getMaxY();
    }

    public void loadData() {
        YamlConfiguration config;

        if (_worldFolder == null)
            config = YamlConfiguration
                    .loadConfiguration(new InputStreamReader(UtilFile.readInputFromZip(_mapFile, "config.yml")));
        else
            config = YamlConfiguration.loadConfiguration(new File(_worldFolder, "config.yml"));

        _name = config.getString("Name");
        _author = config.getString("Author");
        _description = config.getString("Description");

        if (config.contains("Teams"))
            _teamSize = config.getConfigurationSection("Teams").getKeys(false).size();
    }

    public void loadData(World world) {
        _world = world;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(world.getWorldFolder(), "config.yml"));

        if (config.isList("Border")) {
            _isSquare = true;

            List<String> border = config.getStringList("Border");

            Location b1 = parseLoc(border.get(0));
            Location b2 = parseLoc(border.get(1));

            _minX = Math.min(b1.getBlockX(), b2.getBlockX());
            _minY = Math.min(b1.getBlockY(), b2.getBlockY());
            _minZ = Math.min(b1.getBlockZ(), b2.getBlockZ());
            _maxX = Math.max(b1.getBlockX(), b2.getBlockX());
            _maxY = Math.max(b1.getBlockY(), b2.getBlockY());
            _maxZ = Math.max(b1.getBlockZ(), b2.getBlockZ());
        } else {
            _isSquare = config.getString("Border.Type").equals("Square");

            if (_isSquare) {
                List<String> list = config.getStringList("Border.Corners");
                String[] c1 = list.get(0).split(",");
                String[] c2 = list.get(1).split(",");

                _minX = Math.min(Integer.parseInt(c1[0]), Integer.parseInt(c2[0]));
                _minY = Math.min(Integer.parseInt(c1[1]), Integer.parseInt(c2[1]));
                _minZ = Math.min(Integer.parseInt(c1[2]), Integer.parseInt(c2[2]));
                _maxX = Math.max(Integer.parseInt(c1[0]), Integer.parseInt(c2[0]));
                _maxY = Math.max(Integer.parseInt(c1[1]), Integer.parseInt(c2[1]));
                _maxZ = Math.max(Integer.parseInt(c1[2]), Integer.parseInt(c2[2]));

                /*    System.out.println("Border Type: Square");
                System.out.println(_minX + " X to " + _maxX + " X");
                System.out.println(_minZ + " Y to " + _maxY + " Y");
                System.out.println(_minY + " Z to " + _maxZ + " Z");*/
            } else {
                _borderRadius = config.getInt("Border.Radius");

                String[] s = config.getString("Border.Center").split(",");

                _borderCenter = new Location(world, Integer.parseInt(s[0]), Integer.parseInt(s[1]),
                        Integer.parseInt(s[2]));

                /* System.out.println("Border Type: Circle");
                System.out.println("Center: " + _borderCenter.getBlockX() + ", " + _borderCenter.getBlockY() + ", "
                        + _borderCenter.getBlockZ());
                System.out.println("Radius: " + _borderRadius);*/
            }
        }

        if (config.contains("Custom")) {
            ConfigurationSection customConfig = config.getConfigurationSection("Custom");

            for (String key : customConfig.getKeys(false)) {
                List<String> list = customConfig.getStringList(key);

                ArrayList<String> objects = new ArrayList(list);

                _data.put(key, objects);
            }
        }
    }

    public void onBorderWalk(Location loc) {
        if (_isSquare)
            return;

        for (int y = -5; y <= 5; y++) {
            if (loc.getY() + y > 256 || loc.getY() + y < 0)
                continue;

            for (int x = -5; x <= 5; x++) {
                for (int z = -5; z <= 5; z++) {
                    Location blockLoc = loc.clone().add(x, y, z).getBlock().getLocation();

                    if (UtilBlock.solid(blockLoc.getBlock()))
                        continue;

                    if (blockLoc.getY() < 0 || blockLoc.getY() > 256 || loc.distance(blockLoc) > 5)
                        continue;

                    Vector vec = blockLoc.toVector();

                    double dist = UtilLoc.getDistance2d(vec, _borderCenter.toVector()) - _borderRadius;

                    if (dist < 0 || dist > 1) {
                        continue;
                    }

                    blockLoc.getBlock().setType(Material.GLASS);

                    if (UtilMath.r(6) == 0) {
                        UtilParticle.playParticle(ParticleType.BARRIER, blockLoc.add(0.5, 0, 0.5));
                    }
                }
            }
        }
    }

    public Block parseBlock(String string) {
        String[] split = string.split(",");

        return _world.getBlockAt(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }

    public Location parseLoc(String string) {
        String[] split = string.split(",");

        Location loc = new Location(_world, Double.parseDouble(split[0]), Double.parseDouble(split[1]),
                Double.parseDouble(split[2]));

        if (split.length == 5) {
            loc.setYaw(Float.parseFloat(split[3]));
            loc.setPitch(Float.parseFloat(split[4]));
        }

        return loc;
    }

    public void setBorderRadius(int newRadius) {
        _borderRadius = newRadius;
    }

    public void setupWorld() {
        _worldFolder = new File(getName() + "_" + System.currentTimeMillis()).getAbsoluteFile();
        _worldFolder.mkdir();

        UtilFile.extractZip(_mapFile, _worldFolder);
    }
}
