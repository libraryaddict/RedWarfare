package me.libraryaddict.arcade.database.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlRefundKit extends DatabaseOperation
{
    private ArrayList<String> _toRefund = new ArrayList<String>();

    public MysqlRefundKit(String kitName, int refundAmount)
    {
        try (Connection con = getMysql())
        {
            int keyId = KeyMappings.getKey(kitName);
            int creditId = KeyMappings.getKey("CREDIT");

            PreparedStatement stmt = con.prepareStatement("SELECT `uuid` FROM `owned` WHERE `type` = ?");

            stmt.setInt(1, keyId);

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next())
            {
                _toRefund.add(rs.getString("uuid"));
            }

            stmt.close();

            stmt = con.prepareStatement("DELETE FROM `owned` WHERE `type` = ?");

            stmt.setInt(1, keyId);

            stmt.execute();

            stmt = con.prepareStatement(
                    "INSERT INTO currency (uuid, type, amount) VALUES (?,?,?) ON DUPLICATE KEY UPDATE amount = amount + ?;");

            for (String uuid : getRefund())
            {
                stmt.setString(1, uuid);
                stmt.setInt(2, creditId);
                stmt.setLong(3, refundAmount);
                stmt.setLong(4, refundAmount);

                stmt.execute();
            }

            stmt = con.prepareStatement("INSERT INTO currency_log (uuid, type, reason, amount, date) VALUES (?,?,?,?,?)");

            for (String uuid : getRefund())
            {
                stmt.setString(1, uuid);
                stmt.setInt(2, creditId);
                stmt.setInt(3, KeyMappings.getKey("Refund Kit " + kitName));
                stmt.setLong(4, refundAmount);
                stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

                stmt.execute();
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public ArrayList<String> getRefund()
    {
        return _toRefund;
    }
}
