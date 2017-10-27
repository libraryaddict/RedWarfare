package me.libraryaddict.core.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

public class UtilMath
{
    private static Random _random = new Random();

    public static double clamp(double amount, double minAmount, double maxAmount)
    {
        return Math.max(minAmount, Math.min(maxAmount, amount));
    }

    public static double clamp(int amount, int minAmount, int maxAmount)
    {
        return Math.max(minAmount, Math.min(maxAmount, amount));
    }

    public static double getDouble()
    {
        return random().nextDouble();
    }

    public static double getDouble(double max)
    {
        return random().nextDouble() * max;
    }

    public static ArrayList<Integer> getList(int maxNumber)
    {
        return getList(0, maxNumber);
    }

    public static ArrayList<Integer> getList(int minNumber, int maxNumber)
    {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = minNumber; i < maxNumber; i++)
        {
            list.add(i);
        }

        Collections.shuffle(list);

        return list;
    }

    public static boolean nextBoolean()
    {
        return random().nextBoolean();
    }

    public static <Y> Y r(Collection<Y> collection)
    {
        if (collection.isEmpty())
            return null;

        return (Y) collection.toArray(new Object[0])[r(collection.size())];
    }

    public static <Y> Y rm(Collection<Y> collection)
    {
        if (collection.isEmpty())
            return null;

        int index = r(collection.size());
        Iterator<Y> itel = collection.iterator();

        for (int i = 0; i < index; i++)
            itel.next();

        Y r = itel.next();
        itel.remove();

        return r;
    }

    public static <Y> ArrayList<Y> r(Collection<Y> collection, int amount)
    {
        if (collection.isEmpty())
            return null;

        Object[] array = collection.toArray(new Object[0]);
        ArrayList<Y> list = new ArrayList<Y>();

        int i = 0;

        for (Integer integer : getList(collection.size()))
        {
            if (i++ >= amount)
                break;

            list.add((Y) array[integer]);
        }

        return list;
    }

    public static int r(int max)
    {
        if (max > 0)
            return random().nextInt(max);

        return 0;
    }

    public static int r(int min, int max)
    {
        return r(max - min) + min;
    }

    public static <Y> Y r(Y[] array)
    {
        if (array.length == 0)
            return null;

        return array[r(array.length)];
    }

    public static Random random()
    {
        return _random;
    }

    public static double roundTo(double number, double clampTo)
    {
        if (number > 0)
            return clampTo;

        if (number < 0)
            return -clampTo;

        return 0;
    }

    public static double roundTo(double number, double clampMin, double clampMax)
    {
        if (number > 0)
            return clampMax;

        if (number < 0)
            return clampMin;

        return 0;
    }

    public static double rr(double max)
    {
        return getDouble(max);
    }

    public static double rr(double min, double max)
    {
        return getDouble(max - min) + min;
    }
}
