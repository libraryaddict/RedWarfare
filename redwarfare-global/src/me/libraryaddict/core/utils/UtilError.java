package me.libraryaddict.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import me.libraryaddict.core.C;
import me.libraryaddict.mysql.operations.MysqlSaveError;

public class UtilError
{
    private static String _server = "Unknown";

    static
    {
        try
        {
            final PrintStream old = System.err;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final PrintStream newStream = new PrintStream(baos);

            System.setErr(newStream);

            new Thread("Red Warfare - Error reporter")
            {
                public void run()
                {
                    int errors = 0;
                    long reset = System.currentTimeMillis();

                    while (true)
                    {
                        if (UtilTime.elasped(reset, 10000))
                        {
                            reset = System.currentTimeMillis();
                            errors = 0;
                        }

                        if (baos.size() == 0)
                            continue;

                        try
                        {
                            Thread.sleep(5);
                        }
                        catch (InterruptedException e1)
                        {
                            e1.printStackTrace();
                        }

                        byte[] array = baos.toByteArray();

                        baos.reset();

                        String error = new String(array, StandardCharsets.UTF_8);

                        try
                        {
                            old.write(array);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                        errors++;

                        if (errors > 100)
                            continue;

                        new MysqlSaveError(_server, error);
                    }
                }
            }.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static String format(String message)
    {
        return format("Error", message);
    }
    
    public static String format(String errorType, String message)
    {
        return C.Red + C.Bold + errorType + " " + C.Gray + message;
    }

    public static String getServer()
    {
        return _server;
    }
    
    public static void handle(Throwable throwable)
    {
        throwable.printStackTrace();
    }

    public static void init()
    {
    }

    public static void log(String string)
    {
        System.err.println(string);
    }

    public static void setServer(String server)
    {
        _server = server;
    }
}
