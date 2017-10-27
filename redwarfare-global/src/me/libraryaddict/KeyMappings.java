package me.libraryaddict;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.mysql.operations.MysqlFetchMappings;
import me.libraryaddict.mysql.operations.MysqlFetchSetMapping;

public class KeyMappings
{
    private static ConcurrentHashMap<Integer, String> _keys;
    private static ConcurrentHashMap<String, Integer> _keys2 = new ConcurrentHashMap<String, Integer>();

    public static String getKey(int key)
    {
        synchronized (_keys)
        {
            if (!_keys.containsKey(key))
            {
                loadKeys();
            }

            return _keys.get(key);
        }
    }

    public static int getKey(String key)
    {
        synchronized (_keys)
        {
            assert _keys != null;

            if (!_keys2.containsKey(key))
            {
                System.out.println("Mapping " + key + " not found.. Now setting fetching new mapping..");

                MysqlFetchSetMapping fetchSetMapping = new MysqlFetchSetMapping(key);

                if (!fetchSetMapping.isSuccess())
                {
                    System.out.println("Error fetching mapping!");
                    return -1;
                }

                Pair<String, Integer> mapping = fetchSetMapping.getMapping();

                if (mapping == null)
                {
                    System.out.println("Error! No mapping found!");
                    _keys2.put(key, null);
                    return -1;
                }

                System.out.println("Mapping " + key + " is now " + mapping.getValue());

                _keys.put(mapping.getValue(), key);
                _keys2.put(key, mapping.getValue());
            }

            if (_keys2.get(key) == null)
            {
                return -1;
            }

            return _keys2.get(key);
        }
    }

    public static void loadKeys()
    {
        System.out.println("Loading key mappings");
        MysqlFetchMappings fetchMappings = new MysqlFetchMappings();

        if (!fetchMappings.isSuccess())
        {
            UtilError.log("Failed to load key mappings");
            return;
        }

        KeyMappings.setMappings(fetchMappings.getKeys());
    }

    public static void setMappings(ConcurrentHashMap<Integer, String> hashMap)
    {
        _keys = hashMap;
        _keys2.clear();

        for (Entry<Integer, String> entry : _keys.entrySet())
        {
            _keys2.put(entry.getValue(), entry.getKey());
        }
    }
}
