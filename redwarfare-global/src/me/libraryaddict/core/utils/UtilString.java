package me.libraryaddict.core.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import me.libraryaddict.mysql.operations.MysqlSaveLog;

public class UtilString
{
    public static int count(String string, String lookingFor)
    {
        int number = 0;
        int index = -lookingFor.length();

        while ((index = string.indexOf(lookingFor, index + lookingFor.length())) != -1)
        {
            number++;
        }

        return number;
    }

    public static int countCapitalization(String string)
    {
        int amount = 0;

        for (char c : string.toCharArray())
        {
            if (Character.isUpperCase(c))
                amount++;
        }

        return amount;
    }

    public static String join(Collection<String> strings, String seperator)
    {
        String s = "";
        Iterator<String> itel = strings.iterator();

        while (itel.hasNext())
        {
            s += itel.next();

            if (itel.hasNext())
            {
                s += seperator;
            }
        }

        return s;
    }

    public static String join(int ignoreArgs, String[] args, String seperator)
    {
        args = Arrays.copyOfRange(args, Math.min(ignoreArgs, args.length), args.length);

        return join(Arrays.asList(args), seperator);
    }

    public static String join(String[] args, String seperator)
    {
        return join(Arrays.asList(args), seperator);
    }

    public static void log(String string)
    {
        new Thread()
        {
            public void run()
            {
                new MysqlSaveLog(UtilError.getServer(), string);
            }
        }.start();
    }

    public static String repeat(char character, int times)
    {
        return repeat("" + character, times);
    }

    public static String repeat(String string, int times)
    {
        String s = "";

        for (int i = 0; i < times; i++)
            s += string;

        return s;
    }
}
