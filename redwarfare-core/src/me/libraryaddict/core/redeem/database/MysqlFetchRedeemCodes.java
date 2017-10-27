package me.libraryaddict.core.redeem.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.redeem.RedeemCode;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchRedeemCodes extends DatabaseOperation
{
    private RedeemCode _code;

    private ArrayList<RedeemCode> _codes = new ArrayList<RedeemCode>();

    public MysqlFetchRedeemCodes(String codeString)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT redeem_codes.*, playerinfo.info FROM redeem_codes LEFT JOIN playerinfo ON (redeem_codes.redeemer = playerinfo.uuid AND playerinfo.type = ? AND playerinfo.last_used = (SELECT MAX(playerinfo.last_used) FROM playerinfo WHERE playerinfo.uuid = redeem_codes.redeemer)) WHERE redeem_codes.code = ? LIMIT 0,1");

            stmt.setInt(1, KeyMappings.getKey("Name"));
            stmt.setString(2, codeString);

            ResultSet rs = stmt.executeQuery();

            if (rs.first())
            {
                RedeemCode code = new RedeemCode(rs.getTimestamp("created"), rs.getString("code"),
                        KeyMappings.getKey(rs.getInt("type")));

                if (rs.getString("redeemer") != null)
                {
                    code.setRedeemed(rs.getTimestamp("redeemed"),
                            Pair.of(UUID.fromString(rs.getString("redeemer")), rs.getString("info")));
                }

                _code = code;
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public MysqlFetchRedeemCodes(UUID player)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT redeem_codes.*, playerinfo.info FROM redeem_codes LEFT JOIN playerinfo ON (redeem_codes.redeemer = playerinfo.uuid AND playerinfo.type = ? AND playerinfo.last_used = (SELECT MAX(playerinfo.last_used) FROM playerinfo WHERE playerinfo.uuid = redeem_codes.redeemer)) WHERE redeem_codes.owner = ? OR redeem_codes.owner = ?");

            stmt.setInt(1, KeyMappings.getKey("Name"));
            stmt.setString(2, player.toString());
            stmt.setString(3, player.toString().substring(0, 30));

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next())
            {
                RedeemCode code = new RedeemCode(rs.getTimestamp("created"), rs.getString("code"),
                        KeyMappings.getKey(rs.getInt("type")));

                if (rs.getString("redeemer") != null)
                {
                    code.setRedeemed(rs.getTimestamp("redeemed"),
                            Pair.of(UUID.fromString(rs.getString("redeemer")), rs.getString("info")));
                }

                _codes.add(code);
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public RedeemCode getCode()
    {
        return _code;
    }

    public ArrayList<RedeemCode> getCodes()
    {
        return _codes;
    }
}
