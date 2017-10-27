package me.libraryaddict.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.google.gson.annotations.Expose;

import me.libraryaddict.KeyMappings;

public class PlayerPrefs
{
    @Expose
    private HashMap<String, Object> _delayedPrefs = new HashMap<String, Object>();
    private HashMap<String, Object> _preLoadedPrefs = new HashMap<String, Object>();

    public PlayerPrefs clone()
    {
        PlayerPrefs stats = new PlayerPrefs();
        stats._preLoadedPrefs = (HashMap<String, Object>) _preLoadedPrefs.clone();
        stats._delayedPrefs = (HashMap<String, Object>) _delayedPrefs.clone();

        return stats;
    }

    public HashMap<String, Object> getDelayedPrefs()
    {
        return _delayedPrefs;
    }

    public Object getPref(Pref pref, Object def)
    {
        if (!hasPref(pref))
            return def;

        return pref.isPreload() ? _preLoadedPrefs.get(pref.getName()) : _delayedPrefs.get(pref.getName());
    }

    public HashMap<String, Object> getPreloadedPrefs()
    {
        return _preLoadedPrefs;
    }

    public boolean hasPref(Pref pref)
    {
        return _preLoadedPrefs.get(pref.getName()) != null || _delayedPrefs.get(pref.getName()) != null;
    }

    public void load(ResultSet rs) throws SQLException
    {
        rs.beforeFirst();

        while (rs.next())
        {
            // if (rs.getBoolean("preload"))
            if ("".isEmpty())
            {
                _preLoadedPrefs.put(KeyMappings.getKey(rs.getInt("type")), rs.getObject("value"));
            }
            else
            {
                _delayedPrefs.put(KeyMappings.getKey(rs.getInt("type")), rs.getObject("value"));
            }
        }
    }

    public void removePref(Pref pref)
    {
        if (pref.isPreload())
            _preLoadedPrefs.put(pref.getName(), null);
        else
            _delayedPrefs.put(pref.getName(), null);
    }

    public void setPref(Pref pref, Object value)
    {
        if (pref.isPreload())
            _preLoadedPrefs.put(pref.getName(), value);
        else
            _delayedPrefs.put(pref.getName(), value);
    }

}
