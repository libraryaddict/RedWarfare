package me.libraryaddict.core.redeem.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlCreateRedeemCode extends DatabaseOperation
{
    private String _code;

    public MysqlCreateRedeemCode(UUID player, String codeType)
    {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789".toCharArray();

        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM redeem_codes WHERE code = ? LIMIT 0,1");

            while (true)
            {
                String code = "";

                for (int i = 0; i < 8; i++)
                    code += chars[UtilMath.r(chars.length)];

                stmt.setString(1, code);

                ResultSet rs = stmt.executeQuery();

                if (rs.first())
                    continue;

                _code = code;

                break;
            }

            stmt = con.prepareStatement("INSERT INTO redeem_codes (owner, code, type) VALUES (?,?,?)");

            stmt.setString(1, player.toString());
            stmt.setString(2, getCode());
            stmt.setInt(3, KeyMappings.getKey(codeType));

            stmt.execute();

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public String getCode()
    {
        return _code;
    }
}
