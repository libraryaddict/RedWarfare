package me.libraryaddict.core.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

public class UtilTime
{
    public static int currentTick;
    public static long DAY;
    public static long DECADE;
    public static long HOUR;
    public static long MINUTE;
    public static long MONTH;
    public static long WEEK;
    public static long YEAR;

    static
    {
        MINUTE = 60;
        HOUR = MINUTE * MINUTE;
        DAY = HOUR * 24;
        WEEK = DAY * 7;
        MONTH = DAY * 31;
        YEAR = DAY * 365;
        DECADE = YEAR * 10;
    }

    public static boolean elasped(long time)
    {
        return System.currentTimeMillis() > time;
    }

    public static boolean elasped(long time, long timeSince)
    {
        return System.currentTimeMillis() > time + timeSince;
    }

    public static boolean elasped(long time, long timeSince, TimeUnit timeUnit)
    {
        return System.currentTimeMillis() > timeUnit.toMillis(time + timeSince);
    }

    public static boolean elasped(Timestamp time)
    {
        return elasped(time.getTime());
    }

    public static boolean elasped(Timestamp time, long timeSince)
    {
        return System.currentTimeMillis() > time.getTime() + timeSince;
    }

    public static String getProgress(String message, String color1, String color2, double percentage)
    {
        percentage = UtilMath.clamp(percentage, 0, 1);

        int split = (int) (message.length() * percentage);

        return color1 + message.substring(0, split) + color2 + message.substring(split);
    }

    public static String parse(long time)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

        return dateFormat.format(new Date(time));
    }

    public static String parse(Timestamp time)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

        return dateFormat.format(new Date(time.getTime()));
    }
}
