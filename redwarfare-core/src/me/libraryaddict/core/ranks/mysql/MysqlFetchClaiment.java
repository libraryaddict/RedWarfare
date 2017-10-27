package me.libraryaddict.core.ranks.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchClaiment extends DatabaseOperation
{
    private String _claimedBy;
    private String _code;

    public MysqlFetchClaiment(UUID uuid, String code)
    {
        try (Connection con = getMysql())
        {
            ResultSet rs;

            if (uuid != null)
            {
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM old_ranks WHERE uuid = ?");
                stmt.setString(1, uuid.toString().substring(0, 30));
                rs = stmt.executeQuery();
            }
            else
            {
                PreparedStatement stmt = con.prepareStatement("SELECT * FROM old_ranks WHERE `code` = ?");
                stmt.setString(1, code);
                rs = stmt.executeQuery();
            }

            if (!rs.first())
            {
                setSuccess();
                return;
            }

            _code = rs.getString("code");
            _claimedBy = rs.getString("claimed_by");

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public String getClaimed()
    {
        return _claimedBy;
    }

    public String getCode()
    {
        return _code;
    }

}
