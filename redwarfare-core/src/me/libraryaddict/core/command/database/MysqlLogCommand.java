package me.libraryaddict.core.command.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlLogCommand extends DatabaseOperation
{
    public MysqlLogCommand(UUID player, String command, String message)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("INSERT INTO command_log (player, command, args) VALUES (?,?,?)");
            
            stmt.setString(1, player.toString());
            stmt.setInt(2, KeyMappings.getKey(command));
            stmt.setString(3, message);
            
            stmt.execute();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
