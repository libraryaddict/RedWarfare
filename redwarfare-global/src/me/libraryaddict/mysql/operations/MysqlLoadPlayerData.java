package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.network.PlayerPrefs;

public class MysqlLoadPlayerData extends DatabaseOperation
{
    private PlayerData _playerData;

    public MysqlLoadPlayerData(UUID uuid)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con
                    .prepareStatement("SELECT * FROM playerinfo WHERE type = ? AND uuid = ? ORDER BY last_used DESC");

            stmt.setInt(1, KeyMappings.getKey("Name"));
            stmt.setString(2, uuid.toString());

            ResultSet rs = stmt.executeQuery();

            if (!rs.first())
            {
                _playerData = new PlayerData(uuid);

                System.out.println("New user " + uuid + " has joined");

                setSuccess();
                return;
            }

            _playerData = new PlayerData(uuid);

            stmt = con.prepareStatement("SELECT * FROM ranks WHERE uuid = ?");
            stmt.setString(1, uuid.toString());

            rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next())
            {
                _playerData.getOwnedRanks().put(rs.getInt("rank"), rs.getTimestamp("expires").getTime());

                if (rs.getBoolean("display"))
                {
                    _playerData.getDisplayedRanks().add(rs.getInt("rank"));
                }
            }

            PlayerPrefs prefs = new PlayerPrefs();

            stmt = con.prepareStatement("SELECT `type`, `value` FROM prefs_boolean WHERE uuid = ?");
            stmt.setString(1, uuid.toString());

            rs = stmt.executeQuery();

            prefs.load(rs);

            stmt = con.prepareStatement("SELECT `type`, `value` FROM prefs_string WHERE uuid = ?");
            stmt.setString(1, uuid.toString());

            rs = stmt.executeQuery();

            prefs.load(rs);

            _playerData.setPrefs(prefs);

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public PlayerData getPlayerData()
    {
        return _playerData;
    }

}
