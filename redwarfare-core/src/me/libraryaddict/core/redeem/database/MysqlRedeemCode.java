package me.libraryaddict.core.redeem.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlRedeemCode extends DatabaseOperation
{
    public MysqlRedeemCode(String code, UUID claimer)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("UPDATE redeem_codes SET redeemer = ?, redeemed = ? WHERE `code` = ?");

            stmt.setString(1, claimer.toString());
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, code);
            stmt.execute();

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }
}
