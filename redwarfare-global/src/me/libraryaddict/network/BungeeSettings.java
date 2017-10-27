package me.libraryaddict.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import me.libraryaddict.core.C;

/**
 * The global settings all bungees share
 */
public class BungeeSettings
{
    private String _favIcon = "";
    private String _footer = C.Red + "play.redwarfare.com";
    private String _header = C.Red + "Red Warfare";
    private int _maxPlayers = 250;
    private ArrayList<String> _motd = new ArrayList<String>();
    private ArrayList<String> _players = new ArrayList<String>();
    private String _protocol = "Starting up";
    private int _throttle = 0;
    private int _totalPlayers = 500;
    private boolean _whitelist;

    public BungeeSettings()
    {
    }

    public BungeeSettings(ResultSet rs) throws NumberFormatException, SQLException
    {
        rs.beforeFirst();

        while (rs.next())
        {
            String key = rs.getString("type");
            String value = rs.getString("value");

            if (key.equals("max_players"))
            {
                _maxPlayers = Integer.parseInt(value);
            }
            else if (key.equals("total_players"))
            {
                _totalPlayers = Integer.parseInt(value);
            }
            else if (key.equals("throttle"))
            {
                _throttle = Integer.parseInt(value);
            }
            else if (key.equals("protocol"))
            {
                _protocol = value;
            }
            else if (key.equals("motd"))
            {
                _motd.add(value);
            }
            else if (key.equals("favicon"))
            {
                _favIcon = value;
            }
            else if (key.equals("player"))
            {
                _players.add(value);
            }
            else if (key.equals("footer"))
            {
                _footer = value;
            }
            else if (key.equals("header"))
            {
                _header = value;
            }
            else if (key.equals("whitelist"))
            {
                _whitelist = Boolean.parseBoolean(value);
            }
        }
    }

    public String getFavIcon()
    {
        return _favIcon;
    }

    public String getFooter()
    {
        return _footer;
    }

    public String getHeader()
    {
        return _header;
    }

    public int getMaxPlayers()
    {
        return _maxPlayers;
    }

    public ArrayList<String> getMotd()
    {
        return _motd;
    }

    public ArrayList<String> getPlayers()
    {
        return _players;
    }

    public String getProtocol()
    {
        return _protocol;
    }

    public int getThrottle()
    {
        return _throttle;
    }

    public int getTotalPlayers()
    {
        return _totalPlayers;
    }

    public boolean isWhitelist()
    {
        return _whitelist;
    }

    public void setFooter(String value)
    {
        _footer = value;
    }

    public void setHeader(String value)
    {
        _header = value;
    }

    public void setMaxPlayers(int maxPlayers)
    {
        _maxPlayers = maxPlayers;
    }

    public void setMotd(String value)
    {
        _motd.clear();
        _motd.add(value);
    }

    public void setProtocol(String newProtocol)
    {
        _protocol = newProtocol;
    }

    public void setThrottle(int throttle)
    {
        _throttle = throttle;
    }

    public void setTotalPlayers(int totalPlayers)
    {
        _totalPlayers = totalPlayers;
    }

    public void setWhitelist(boolean whitelist)
    {
        _whitelist = whitelist;
    }
}
