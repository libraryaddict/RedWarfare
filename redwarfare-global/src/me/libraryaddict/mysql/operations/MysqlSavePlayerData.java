package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map.Entry;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerData;

public class MysqlSavePlayerData extends DatabaseOperation
{
    public MysqlSavePlayerData(PlayerData playerData)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO ranks (uuid, rank, expires, display) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE expires = ?, display = ?;");

            String uuid = playerData.getUUID().toString();

            for (Entry<Integer, Long> rank : playerData.getOwnedRanks().entrySet())
            {
                boolean display = playerData.getDisplayedRanks().contains(rank.getKey());

                stmt.setString(1, uuid);
                stmt.setInt(2, rank.getKey());
                stmt.setTimestamp(3, new Timestamp(rank.getValue()));
                stmt.setBoolean(4, display);
                stmt.setTimestamp(5, new Timestamp(rank.getValue()));
                stmt.setBoolean(6, display);

                stmt.execute();
            }

            doPrefs(con, playerData, Boolean.class);
            doPrefs(con, playerData, String.class);

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    private void doPrefs(Connection con, PlayerData playerData, Class c) throws SQLException
    {
        PreparedStatement stmt = con.prepareStatement("INSERT INTO prefs_" + c.getSimpleName().toLowerCase()
                + " (uuid, type, value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE value = ?;");

        stmt.setString(1, playerData.getUUID().toString());

        boolean delete = false;

        for (Entry<String, Object> stats : playerData.getPrefs().getPreloadedPrefs().entrySet())
        {
            if (stats.getValue() == null)
            {
                delete = true;
                continue;
            }

            if (!stats.getValue().getClass().isAssignableFrom(c))
                continue;

            stmt.setInt(2, KeyMappings.getKey(stats.getKey()));

            if (stats.getValue() instanceof Boolean)
            {
                stmt.setBoolean(3, (Boolean) stats.getValue());
                stmt.setBoolean(4, (Boolean) stats.getValue());
            }
            else if (stats.getValue() instanceof String)
            {
                stmt.setString(3, (String) stats.getValue());
                stmt.setString(4, (String) stats.getValue());
            }

            stmt.execute();
        }

        if (delete)
        {
            stmt = con.prepareStatement(
                    "DELETE FROM prefs_" + c.getSimpleName().toLowerCase() + " WHERE `uuid` = ? AND `type` = ?;");

            stmt.setString(1, playerData.getUUID().toString());

            for (Entry<String, Object> stats : playerData.getPrefs().getPreloadedPrefs().entrySet())
            {
                if (stats.getValue() != null)
                    continue;

                stmt.setInt(2, KeyMappings.getKey(stats.getKey()));

                stmt.execute();
            }
        }

    }

}
