package me.libraryaddict.core.ranks.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlSetClaimed extends DatabaseOperation
{
    public MysqlSetClaimed(String code, UUID claimer)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("UPDATE old_ranks SET claimed_by = ? WHERE `code` =?");

            stmt.setString(1, claimer.toString());
            stmt.setString(2, code);
            stmt.execute();

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
