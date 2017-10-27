package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;
import me.libraryaddict.network.PlayerOwned;

public class MysqlSaveOwned extends DatabaseOperation
{
    public MysqlSaveOwned(PlayerOwned playerData)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("INSERT IGNORE INTO owned (uuid, type) VALUES (?,?)");

            stmt.setString(1, playerData.getUUID().toString());

            for (String stats : playerData.getOwned())
            {
                stmt.setInt(2, KeyMappings.getKey(stats));

                stmt.execute();
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

}
