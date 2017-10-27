package me.libraryaddict.core.utils;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class UtilShapes
{
    public static ArrayList<Location> drawLineDistanced(Location loc1, Location loc2, double dist)
    {
        return drawLinePoints(loc1, loc2, (int) (UtilLoc.getDistance(loc1, loc2) / dist));
    }

    public static ArrayList<Location> drawLinePoints(Location loc1, Location loc2, int points)
    {
        Vector vec = loc2.toVector().subtract(loc1.toVector());

        vec.multiply(1D / points);

        ArrayList<Location> locs = new ArrayList<Location>();

        for (int i = 0; i < points; i++)
        {
            locs.add(loc1.clone().add(vec.clone().multiply(i)));
        }

        return locs;
    }

    public static ArrayList<Location> getDistancedCircle(Location center, double circleRadius, double pointsDistance)
    {
        return getPointsInCircle(center, circleRadius, (int) ((circleRadius * Math.PI * 2) / pointsDistance));
    }

    public static ArrayList<Location> getPointsInCircle(Location center, double radius, int points)
    {
        return getPointsInCircle(center, radius, points, 0);
    }

    public static ArrayList<Location> getPointsInCircle(Location center, double radius, int points, double yaw)
    {
        ArrayList<Location> list = new ArrayList<Location>();

        double pi = Math.PI * 2;

        yaw = (((yaw + 720) % 360) / 360) * pi;

        double slice = pi / points;

        for (int i = 0; i < points; i++)
        {
            double angle = (yaw + (slice * i)) % pi;

            double newX = (radius * Math.cos(angle));
            double newZ = (radius * Math.sin(angle));

            Location loc = center.clone().add(newX, 0, newZ);

            list.add(loc);
        }

        return list;
    }
}
