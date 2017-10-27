package me.libraryaddict.core.utils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import me.libraryaddict.core.Pair;

/**
 * Messing around with numbers in a way that outputs a parsed string
 */
public class UtilNumber
{
    private static String[] letters =
        {
                "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"
        };

    private static int[] numbers =
        {
                1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1
        };
    final static char symbol[] =
        {
                'M', 'D', 'C', 'L', 'X', 'V', 'I'
        };
    final static int value[] =
        {
                1000, 500, 100, 50, 10, 5, 1
        };

    public static int convertFromRoman(String roman)
    {
        roman = roman.toUpperCase();
        if (roman.length() == 0)
            return 0;
        for (int i = 0; i < symbol.length; i++)
        {
            int pos = roman.indexOf(symbol[i]);
            if (pos >= 0)
                return value[i] - convertFromRoman(roman.substring(0, pos)) + convertFromRoman(roman.substring(pos + 1));
        }
        throw new IllegalArgumentException("Invalid Roman Symbol.");
    }

    public static String convertToRoman(int number)
    {
        String roman = "";
        for (int i = 0; i < numbers.length; i++)
        {
            while (number >= numbers[i])
            {
                roman += letters[i];
                number -= numbers[i];
            }
        }
        return roman;
    }

    public static String getTime(long seconds)
    {
        return getTime(seconds, TimeUnit.SECONDS);
    }

    public static String getTime(long seconds, TimeUnit timeUnit)
    {
        return getTime(seconds, timeUnit, 10);
    }

    public static String getTime(long seconds, TimeUnit timeUnit, int maxTimeNumber)
    {
        boolean negative = seconds < 0;
        seconds = Math.abs(seconds);

        seconds = timeUnit.toSeconds(seconds);

        ArrayList<Pair<String, Long>> list = new ArrayList<Pair<String, Long>>();

        list.add(Pair.of("decade", Math.floorDiv(seconds, UtilTime.DECADE)));
        list.add(Pair.of("year", Math.floorDiv(seconds % UtilTime.DECADE, UtilTime.YEAR)));
        list.add(Pair.of("month", Math.floorDiv(seconds % UtilTime.YEAR, UtilTime.MONTH)));
        list.add(Pair.of("week", Math.floorDiv(seconds % UtilTime.MONTH, UtilTime.WEEK)));
        list.add(Pair.of("day", Math.floorDiv(seconds % UtilTime.WEEK, UtilTime.DAY)));
        list.add(Pair.of("hour", Math.floorDiv(seconds % UtilTime.DAY, UtilTime.HOUR)));
        list.add(Pair.of("minute", Math.floorDiv(seconds % UtilTime.HOUR, UtilTime.MINUTE)));
        list.add(Pair.of("second", seconds % UtilTime.MINUTE));

        String time = "";

        for (Pair<String, Long> pair : list)
        {
            if (pair.getValue() == 0)
                continue;

            if (maxTimeNumber-- <= 0)
                continue;

            if (!time.isEmpty())
                time += ", ";

            time += pair.getValue() + " " + pair.getKey() + (pair.getValue() == 1 ? "" : "s");
        }

        if (time.equals(""))
            time = "no time at all";
        else if (negative)
            time += " ago";

        return time;
    }

    public static String getTime(Timestamp timestamp)
    {
        return getTime(System.currentTimeMillis() - timestamp.getTime(), TimeUnit.MILLISECONDS);
    }

    public static String getTime(Timestamp timestamp, int figures)
    {
        return getTime(System.currentTimeMillis() - timestamp.getTime(), TimeUnit.MILLISECONDS, figures);
    }

    public static String getTimeAbbr(long seconds)
    {
        return getTimeAbbr(seconds, TimeUnit.SECONDS);
    }

    public static String getTimeAbbr(long seconds, TimeUnit timeUnit)
    {
        seconds = timeUnit.toSeconds(seconds);

        ArrayList<Pair<String, Long>> list = new ArrayList<Pair<String, Long>>();

        list.add(Pair.of("d", Math.floorDiv(seconds, UtilTime.DECADE)));
        list.add(Pair.of("y", Math.floorDiv(seconds % UtilTime.DECADE, UtilTime.YEAR)));
        list.add(Pair.of("mon", Math.floorDiv(seconds % UtilTime.YEAR, UtilTime.MONTH)));
        list.add(Pair.of("w", Math.floorDiv(seconds % UtilTime.MONTH, UtilTime.WEEK)));
        list.add(Pair.of("d", Math.floorDiv(seconds % UtilTime.WEEK, UtilTime.DAY)));
        list.add(Pair.of("h", Math.floorDiv(seconds % UtilTime.DAY, UtilTime.HOUR)));
        list.add(Pair.of("m", Math.floorDiv(seconds % UtilTime.HOUR, UtilTime.MINUTE)));
        list.add(Pair.of("s", seconds % UtilTime.MINUTE));

        String time = "";

        for (Pair<String, Long> pair : list)
        {
            if (pair.getValue() == 0 && time.isEmpty())
                continue;

            time += pair.getValue() + pair.getKey();
        }

        if (time.equals(""))
            time = "0s";

        return time;
    }

    public static boolean isParsable(String value)
    {
        try
        {
            return (Double.parseDouble(value) + "").equals(value);
        }
        catch (Exception ex)
        {
        }

        return false;
    }

    public static boolean isParsableInt(String value)
    {
        try
        {
            // Do this incase the number too long
            return (Integer.parseInt(value) + "").equals(value);
        }
        catch (Exception ex)
        {
        }

        return false;
    }

    /**
     * Parse the string "43d" to "43 days"
     */
    public static long parseNumber(String number)
    {
        String currentDigits = "";
        String currentWords = "";
        long time = 0;

        for (char c : number.toCharArray())
        {
            if (Character.isDigit(c))
            {
                if (!currentWords.isEmpty())
                {
                    long t = parseNumber(currentDigits, currentWords);

                    if (t == -1)
                        return -1;

                    time += t;
                }

                currentDigits += c;
            }
            else
            {
                currentWords += c;
            }
        }

        if (currentWords.isEmpty() || currentDigits.isEmpty())
            return -1;

        if (!currentWords.isEmpty())
        {
            long t = parseNumber(currentDigits, currentWords);

            if (t == -1)
                return -1;

            time += t;
        }

        return time;
    }

    private static long parseNumber(String number, String chars)
    {
        chars = chars.toLowerCase();
        long time = Long.parseLong(number);

        if (chars.equals("s") || chars.equals("sec") || chars.equals("secs") || chars.equals("seconds"))
        {
        }
        else if (chars.equals("min") || chars.equals("minute"))
        {
            time *= UtilTime.MINUTE;
        }
        else if (chars.equals("h") || chars.equals("hr") || chars.equals("hour"))
        {
            time *= UtilTime.HOUR;
        }
        else if (chars.equals("d") || chars.equals("day"))
        {
            time *= UtilTime.DAY;
        }
        else if (chars.equals("w") || chars.equals("wk") || chars.equals("week"))
        {
            time *= UtilTime.WEEK;
        }
        else if (chars.equals("m") || chars.equals("mon") || chars.equals("month"))
        {
            time *= UtilTime.MONTH;
        }
        else if (chars.equals("y") || chars.equals("yr") || chars.equals("year"))
        {
            time *= UtilTime.YEAR;
        }
        else
            return -1;

        return time;
    }
}
