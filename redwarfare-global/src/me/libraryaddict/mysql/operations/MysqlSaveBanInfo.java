package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.BanInfo;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlSaveBanInfo extends DatabaseOperation
{
    public MysqlSaveBanInfo(BanInfo banInfo)
    {
        Connection con = null;

        try
        {
            con = getMysql();

            PreparedStatement stmt = con.prepareStatement(
                    "SELECT * FROM bans WHERE banned = ? AND banned_by = ? AND reason = ? AND banned_when = ? AND ban_expires = ? AND type = ?");

            stmt.setString(1, banInfo.getBanned());
            stmt.setInt(2, KeyMappings.getKey(banInfo.getBanner()));
            stmt.setString(3, banInfo.getReason());
            stmt.setTimestamp(4, banInfo.getBannedWhen());
            stmt.setTimestamp(5, banInfo.getBanExpires());
            stmt.setBoolean(6, !banInfo.isBan());

            ResultSet rs = stmt.executeQuery();

            if (rs.first())
            {
                stmt.close();

                stmt = con.prepareStatement("UPDATE bans SET ban_state = " + KeyMappings.getKey(banInfo.getBanState().name())
                        + " WHERE banned = ? AND banned_by = ? AND reason = ? AND banned_when = ? AND ban_expires = ? AND type = ?");

                stmt.setString(1, banInfo.getBanned());
                stmt.setInt(2, KeyMappings.getKey(banInfo.getBanner()));
                stmt.setString(3, banInfo.getReason());
                stmt.setTimestamp(4, banInfo.getBannedWhen());
                stmt.setTimestamp(5, banInfo.getBanExpires());
                stmt.setBoolean(6, !banInfo.isBan());
            }
            else
            {
                stmt.close();

                stmt = con.prepareStatement(
                        "INSERT INTO bans (banned, type, banned_when, banned_by, reason, ban_expires, ban_state) VALUES (?,?,?,?,?,?,?)");

                stmt.setString(1, banInfo.getBanned());
                stmt.setBoolean(2, !banInfo.isBan());
                stmt.setTimestamp(3, banInfo.getBannedWhen());
                stmt.setInt(4, KeyMappings.getKey(banInfo.getBanner()));
                stmt.setString(5, banInfo.getReason());
                stmt.setTimestamp(6, banInfo.getBanExpires());
                stmt.setInt(7, KeyMappings.getKey(banInfo.getBanState().name()));
            }

            stmt.execute();

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
        finally
        {
            if (con != null)
            {
                try
                {
                    con.close();
                }
                catch (Exception ex)
                {
                    UtilError.handle(ex);
                }
            }
        }
    }
}
