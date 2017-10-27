package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchUUID extends DatabaseOperation
{
    private String _name;
    private UUID _uuid;

    // Can be UUID, IP, Name
    public MysqlFetchUUID(String playerName)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT uuid,info FROM playerinfo WHERE type = ? AND info = ? ORDER BY 'last_used' DESC LIMIT 0,1");

            stmt.setInt(1, KeyMappings.getKey("Name"));
            stmt.setString(2, playerName);

            ResultSet rs = stmt.executeQuery();

            if (!rs.first())
            {
                setSuccess();
                return;
            }

            _uuid = UUID.fromString(rs.getString("uuid"));
            _name = rs.getString("info");
            setSuccess();
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

    public UUID getUUID()
    {
        return _uuid;
    }
}
