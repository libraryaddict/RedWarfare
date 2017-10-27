package me.libraryaddict.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import me.libraryaddict.core.nbt.types.CompoundTag;
import me.libraryaddict.core.nbt.types.NbtIo;

public class UtilLoc
{
    public static Location getAverage(ArrayList<Location> locations)
    {
        if (locations.isEmpty())
            return null;

        Vector vec = new Vector(0, 0, 0);

        for (Location loc : locations)
        {
            vec.add(loc.toVector());
        }

        vec.multiply(1D / locations.size());

        return vec.toLocation(locations.get(0).getWorld());
    }

    public static <Y extends Entity> Y getClosest(Location point, ArrayList<Y> entities, int minDistance, Entity... ignore)
    {
        Y closest = null;
        double dist = 0;

        loop: for (Y entity : entities)
        {
            for (Entity e : ignore)
                if (entity == e)
                    continue loop;

            double d = getDistance(entity.getLocation(), point);

            if (closest != null && d > dist)
                continue;

            closest = entity;
            d = dist;
        }

        return closest;
    }

    public static Vector getDirection(Entity from, Entity to)
    {
        return getDirection(from.getLocation(), to.getLocation());
    }

    public static Vector getDirection(Location loc1, Location loc2)
    {
        return getDirection(loc1.toVector(), loc2.toVector());
    }

    public static Vector getDirection(Vector vec1, Vector vec2)
    {
        return vec2.clone().subtract(vec1).normalize();
    }

    public static Vector getDirection2d(Entity from, Entity to)
    {
        return getDirection2d(from.getLocation(), to.getLocation());
    }

    public static Vector getDirection2d(Location loc1, Location loc2)
    {
        return getDirection2d(loc1.toVector(), loc2.toVector());
    }

    public static Vector getDirection2d(Vector vec1, Vector vec2)
    {
        return vec2.clone().subtract(vec1).setY(0).normalize();
    }

    public static double getDistance(Entity ent1, Entity ent2)
    {
        return getDistance(ent1.getLocation(), ent2.getLocation());
    }

    public static double getDistance(Location loc1, Location loc2)
    {
        return getDistance(loc1.toVector(), loc2.toVector());
    }

    public static double getDistance(Vector vec1, Vector vec2)
    {
        return vec1.distance(vec2);
    }

    public static double getDistance2d(Block block, Block block2)
    {
        return getDistance2d(block.getLocation(), block2.getLocation());
    }

    public static double getDistance2d(Entity ent1, Entity ent2)
    {
        return getDistance2d(ent1.getLocation(), ent2.getLocation());
    }

    public static double getDistance2d(Location loc1, Location loc2)
    {
        return getDistance2d(loc1.toVector(), loc2.toVector());
    }

    public static double getDistance2d(Vector vec1, Vector vec2)
    {
        return vec1.clone().setY(vec2.getY()).distance(vec2);
    }

    public static EulerAngle getEulerAngle(Vector vector)
    {
        return new EulerAngle(Math.toRadians(getPitch(vector)), Math.toRadians(getYaw(vector)), 0);
    }

    public static Location getFurtherest(ArrayList<Location> locations)
    {
        return getFurtherest(Bukkit.getOnlinePlayers(), locations);
    }

    public static Location getFurtherest(Collection<? extends Entity> players, ArrayList<Location> locations)
    {
        return getFurtherest(players.toArray(new Entity[0]), locations);
    }

    public static Location getFurtherest(Entity[] entities, ArrayList<Location> locations)
    {
        Location loc = null;
        double worstDist = 0;

        for (Location canidate : locations)
        {
            double closest = 999999;

            for (Entity player : entities)
            {
                double newDist = getDistance(player.getLocation(), canidate);

                if (newDist < closest)
                {
                    closest = newDist;
                }
            }

            if (loc != null && closest < worstDist)
                continue;

            worstDist = closest;
            loc = canidate;
        }

        return loc;
    }

    public static <Y extends Entity> ArrayList<Y> getInRadius(Location loc, ArrayList<? extends Entity> entities, double width,
            double height, Class<Y> entityClass)
    {
        ArrayList<Y> list = new ArrayList<Y>();

        for (Entity entity : entities)
        {
            if (!entityClass.isInstance(entity))
            {
                continue;
            }

            if (getDistance2d(loc, entity.getLocation()) > width)
            {
                continue;
            }

            if (Math.abs(loc.getY() - entity.getLocation().getY()) > height)
            {
                continue;
            }

            list.add((Y) entity);
        }

        return list;
    }

    public static <Y extends Entity> ArrayList<Y> getInRadius(Location loc, ArrayList<Entity> entities, double radius,
            Class<Y> entityClass)
    {
        ArrayList<Y> list = new ArrayList<Y>();

        for (Entity entity : entities)
        {
            if (!entityClass.isInstance(entity))
                continue;

            if (getDistance(loc, entity.getLocation()) <= radius)
            {
                list.add((Y) entity);
            }
        }

        return list;
    }

    public static <Y extends Entity> ArrayList<Y> getInRadius(Location loc, double radius, Class<Y> entityClass)
    {
        ArrayList<Y> list = new ArrayList<Y>();

        for (Entity entity : loc.getWorld().getEntitiesByClass(entityClass))
        {
            if (getDistance(loc, entity.getLocation()) <= radius)
            {
                list.add((Y) entity);
            }
        }

        return list;
    }

    public static <Y extends Entity> ArrayList<Y> getInRadius(Location loc, double width, double height, Class<Y> entityClass)
    {
        ArrayList<Y> list = new ArrayList<Y>();

        for (Entity entity : loc.getWorld().getEntitiesByClass(entityClass))
        {
            if (getDistance2d(loc, entity.getLocation()) > width)
            {
                continue;
            }

            if (Math.abs(loc.getY() - entity.getLocation().getY()) > height)
                continue;

            list.add((Y) entity);
        }

        return list;
    }

    public static Vector getLeft(Vector vector)
    {
        return new Vector(vector.getZ(), vector.getY(), -vector.getX());
    }

    public static EulerAngle getLooking(Location location)
    {
        return new EulerAngle(Math.toRadians(location.getPitch()), Math.toRadians(location.getYaw()), 0);
    }

    public static float getPitch(Vector vec)
    {
        double x = vec.getX();
        double y = vec.getY();
        double z = vec.getZ();
        double xz = Math.sqrt((x * x) + (z * z));

        double pitch = Math.toDegrees(Math.atan(xz / y));
        if (y <= 0)
            pitch += 90;
        else
            pitch -= 90;

        if (pitch == 180)
            pitch = 0;

        return (float) pitch;
    }

    public static Vector getRight(Vector vector)
    {
        return new Vector(-vector.getZ(), vector.getY(), vector.getX());
    }

    /* public static double getPitch(Vector vector)
    {
        double x = vector.getX();
        double z = vector.getZ();
    
        if ((x == 0.0D) && (z == 0.0D))
        {
            return (vector.getY() > 0.0D ? -90.0F : 90.0F);
        }
    
        double xz = Math.sqrt(x * x + z * z);
    
        return ((float) Math.toDegrees(Math.atan(-vector.getY() / xz)));
    }*/

    public static Vector getVector(double yaw, double pitch)
    {
        Vector vector = new Vector();

        double rotX = yaw;
        double rotY = pitch;

        vector.setY(-Math.sin(Math.toRadians(rotY)));

        double xz = Math.cos(Math.toRadians(rotY));

        vector.setX(-xz * Math.sin(Math.toRadians(rotX)));
        vector.setZ(xz * Math.cos(Math.toRadians(rotX)));

        return vector;
    }

    public static float getYaw(Vector vec)
    {
        double x = vec.getX();
        double z = vec.getZ();

        double yaw = Math.toDegrees(Math.atan((-x) / z));
        if (z < 0)
            yaw += 180;

        return (float) yaw;
    }

    /*public static double getYaw(Vector vector)
    {
        double x = vector.getX();
        double z = vector.getZ();
    
        if ((x == 0.0D) && (z == 0.0D))
        {
            return 0;
        }
    
        double theta = Math.atan2(-x, z);
    
        return Math.toDegrees((theta + Math.PI * 2) % Math.PI * 2);
    }*/

    public static boolean hasSight(Location from, Location to)
    {
        from = from.clone();

        Vector vec = getDirection(from, to).multiply(0.1);
        double dist = getDistance(from, to);

        for (double d = 0; d < dist; d += 0.1)
        {
            if (!UtilBlock.nonSolid(from.getBlock()) && !UtilBlock.partiallySolid(from.getBlock()))
            {
                return false;
            }

            from.add(vec);
        }

        return true;
    }

    public static boolean hasSight(Location from, Block to)
    {
        from = from.clone();

        Vector vec = getDirection(from, to.getLocation().add(0.5, 0.5, 0.5)).multiply(0.1);
        double dist = getDistance(from, to.getLocation().add(0.5, 0.5, 0.5));

        for (double d = 0; d < dist; d += 0.1)
        {
            if (from.getBlock().equals(to))
                return true;

            if (!UtilBlock.nonSolid(from.getBlock()) && !UtilBlock.partiallySolid(from.getBlock()))
            {
                return false;
            }

            from.add(vec);
        }

        return true;
    }

    /* public static boolean isExposedSky(Location location)
    {
        Block b = new Location(location.getWorld(), location.getX(), 256, location.getZ()).getBlock();
    
        while (b.getY() >= location.getBlockY() && (UtilBlock.nonSolid(b) || UtilBlock.partiallySolid(b)))
        {
            b = b.getRelative(BlockFace.DOWN);
        }
    
        return location.getBlockY() >= b.getY();
    }*/

    public static boolean isExposedSky(Location location, boolean[]... toCheckAgainst)
    {
        if (toCheckAgainst.length == 0)
        {
            toCheckAgainst = new boolean[][]
                {
                        UtilBlock.nonSolid()
                };
        }

        Block b = new Location(location.getWorld(), location.getX(), 256, location.getZ()).getBlock();

        while (b.getY() >= location.getBlockY())
        {
            boolean passCheck = false;

            for (boolean[] checkAgainst : toCheckAgainst)
            {
                if (checkAgainst[b.getTypeId()])
                {
                    passCheck = true;
                    break;
                }
            }

            if (!passCheck)
                break;

            b = b.getRelative(BlockFace.DOWN);
        }

        return location.getBlockY() >= b.getY();
    }

    public static boolean isInside(Block point1, Block point2, Location location)
    {
        return isInside(point1, point2, location, false);
    }

    public static boolean isInside(Block point1, Block point2, Location location, boolean yOptional)
    {
        return isInside(point1.getX(), point1.getY(), point1.getZ(), point2.getX(), point2.getY(), point2.getZ(), location,
                yOptional);
    }

    public static boolean isInside(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Location location)
    {
        return isInside(minX, minY, minZ, maxX, maxY, maxZ, location, false);
    }

    public static boolean isInside(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, Location location,
            boolean yOptional)
    {
        if (location.getBlockX() < minX)
            return false;

        if (location.getBlockZ() < minZ)
            return false;

        if (location.getBlockX() > maxX + 1)
            return false;

        if (location.getBlockZ() > maxZ + 1)
            return false;

        if (yOptional && minY == maxY)
            return true;

        if (location.getBlockY() < minY)
            return false;

        if (location.getBlockY() > maxY)
            return false;

        return true;
    }

    public static boolean isInside(Location point1, Location point2, Location location)
    {
        return isInside(point1, point2, location, false);
    }

    public static boolean isInside(Location point1, Location point2, Location location, boolean yOptional)
    {
        if (location.getX() < point1.getX())
            return false;

        if (location.getZ() < point1.getZ())
            return false;

        if (location.getX() > point2.getX())
            return false;

        if (location.getZ() > point2.getZ())
            return false;

        if (yOptional && point1.getY() == point2.getY())
            return true;

        if (location.getY() < point1.getY())
            return false;

        if (location.getY() > point2.getY())
            return false;

        return true;
    }

    public static boolean isLookingAt(Player player, Block block)
    {
        Location from = player.getEyeLocation();
        Vector angle = from.getDirection();

        Vector vec = angle.multiply(0.1);

        double dist = from.distance(block.getLocation().add(0.5, 0.5, 0.5).add(new Vector(UtilMath.roundTo(vec.getX(), 0.4),
                UtilMath.roundTo(vec.getY(), 0.4), UtilMath.roundTo(vec.getZ(), 0.4))));

        if (from.getBlock() == block)
            return true;

        for (double d = 0; d < dist; d += 0.1)
        {
            from.add(vec);

            Block current = from.getBlock();

            if (block.equals(current))
                return true;

            if (!UtilBlock.solid(current))
                continue;

            return false;
        }

        return true;
    }

    public static Location getLookingAt(Location fromPoint, Vector vec)
    {
        Location from = fromPoint.clone();
        vec = vec.clone().normalize().multiply(0.1);

        for (int i = 0; i < 1000; i++)
        {
            from.add(vec);

            if (from.getY() <= 0 || from.getY() >= 256)
                break;

            if (UtilBlock.nonSolid(from.getBlock()))
                continue;

            break;
        }

        return from;
    }

    public static boolean isSafeTeleport(Location loc)
    {
        if (loc.getY() < 1)
            return false;

        Block b = loc.getBlock();

        for (int y = -1; y <= 1; y++)
        {
            for (BlockFace face : BlockFace.values())
            {
                Block block = b.getRelative(face.getModX(), face.getModY() + y, face.getModZ());

                switch (block.getType())
                {
                case STONE_PLATE:
                case WOOD_PLATE:
                case IRON_PLATE:
                case TRIPWIRE:
                    return false;
                default:
                    break;
                }
            }
        }

        // Check that there is space for the player to exist
        for (int y = 0; y <= 1; y++)
        {
            Block block = b.getRelative(0, y, 0);

            if (!UtilBlock.nonSolid(block))
                return false;
        }

        // Check that there is a block for him to land on
        for (int y = -1; y >= -2; y--)
        {
            if (b.getY() + y < 0)
                return false;

            Block block = b.getRelative(0, y, 0);

            // If you can't stand on this block, but its partially solid. Then no.
            if (!UtilBlock.standable(block) && UtilBlock.partiallySolid(block))
                return false;

            // If you can stand on this block.
            if (UtilBlock.standable(block))
                return true;
        }

        // The second check failed, there is nothing to stand on.
        return false;
    }

    public static boolean isSpawnableHere(Location loc)
    {
        Block b = loc.getBlock().getRelative(BlockFace.DOWN);

        if (!UtilBlock.spawnable(b))
            return false;

        for (int y = 1; y <= 2; y++)
        {
            if (!UtilBlock.nonSolid(b.getRelative(BlockFace.UP, y)))
            {
                return false;
            }
        }

        return true;
    }

    public static void writeEmptyGeneration(File levelDat)
    {
        try
        {
            CompoundTag root = NbtIo.readCompressed(new FileInputStream(levelDat));

            CompoundTag data = root.getCompound("Data");
            data.putString("generatorName", "flat");
            data.putString("generatorOptions", "3;minecraft:air;1;minecraft:air");

            NbtIo.writeCompressed(root, new FileOutputStream(levelDat));
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
