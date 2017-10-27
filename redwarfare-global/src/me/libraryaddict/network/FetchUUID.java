package me.libraryaddict.network;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import me.libraryaddict.core.utils.UtilError;

public class FetchUUID
{
    private String _name;
    private UUID _uuid;

    public FetchUUID(String name)
    {
        try
        {
            if (!name.matches("([A-Za-z0-9_]){2,16}"))
                return;

            HttpURLConnection url = getUrl("https://api.mojang.com/users/profiles/minecraft/" + name);

            int status = url.getResponseCode();
            InputStream commandsStream;

            if (status >= 400)
            {
                commandsStream = url.getErrorStream();

                String str1 = IOUtils.toString(commandsStream, Charsets.UTF_8);
                commandsStream.close();

                System.err.println(str1);
                return;
            }
            else
            {
                commandsStream = url.getInputStream();
            }

            String str1 = IOUtils.toString(commandsStream, Charsets.UTF_8);

            commandsStream.close();

            HashMap<String, String> map = new Gson().fromJson(str1, HashMap.class);

            if (map == null)
                return;

            _name = map.get("name");
            String uuid = map.get("id");

            BigInteger bi1 = new BigInteger(uuid.substring(0, 16), 16);
            BigInteger bi2 = new BigInteger(uuid.substring(16, 32), 16);

            _uuid = new UUID(bi1.longValue(), bi2.longValue());
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public String getName()
    {
        return _name;
    }

    private HttpURLConnection getUrl(String url) throws MalformedURLException, IOException
    {
        HttpURLConnection localHttpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        localHttpURLConnection.setConnectTimeout(10000);
        localHttpURLConnection.setReadTimeout(10000);

        localHttpURLConnection.setDoOutput(true);

        return localHttpURLConnection;
    }

    public UUID getUUID()
    {
        return _uuid;
    }
}