package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.network.BanInfo;
import me.libraryaddict.network.BanInfo.BanState;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchBanInfo extends DatabaseOperation
{
    private ArrayList<BanInfo> _banInfo = new ArrayList<BanInfo>();

    // Can be UUID, IP
    public MysqlFetchBanInfo(String name)
    {
        try (Connection con = getMysql())
        {
            Statement stmt = con.createStatement();
            String uuid = name;

            if (name.contains("-"))
            {
                ResultSet rs = stmt.executeQuery(
                        "SELECT uuid FROM playerinfo WHERE uuid = '" + name + "' ORDER BY last_used DESC LIMIT 0,1");

                if (!rs.first())
                {
                    setSuccess();
                    return;
                }

                uuid = rs.getString("uuid");
            }
            else if (!name.contains("."))
            {
                throw new IllegalArgumentException("The provided param was not an IP or UUID!");
            }

            // Order results by when
            ResultSet rs = stmt.executeQuery("SELECT * FROM bans WHERE banned = '" + uuid + "' ORDER BY banned_when DESC");

            rs.beforeFirst();

            while (rs.next())
            {
                BanInfo banInfo = new BanInfo(rs);

                _banInfo.add(banInfo);
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public ArrayList<BanInfo> getBanInfo()
    {
        return _banInfo;
    }

    public String isBanned()
    {
        if (!isSuccess())
            return UtilError.format("Server is having database problems");

        BanInfo worst = null;

        for (BanInfo info : getBanInfo())
        {
            if (info.isRemoved())
                continue;

            if (info.isMute())
                continue;

            if (info.isExpired())
            {
                info.setBanState(BanState.EXPIRED);

                new MysqlSaveBanInfo(info);

                continue;
            }

            if (worst != null
                    && (worst.isPerm() || (!info.isPerm() || info.getBanExpires().getTime() < worst.getBanExpires().getTime())))
                continue;

            worst = info;
        }

        if (worst != null)
        {
            String s = C.Red + "You were banned by " + worst.getBanner() + "\nReason: " + worst.getReason() + "\n";

            if (worst.isPerm())
            {
                s += "This ban will not expire";
            }
            else
            {
                s += "This ban expires in " + UtilNumber.getTime(worst.getBanExpires().getTime() - System.currentTimeMillis());
            }

            s += "\nAppeal at www.redwarfare.com";

            return s;
        }

        return null;
    }

    public Pair<Pair<String, String>, Long> isMuted()
    {
        if (!isSuccess())
            return null;

        BanInfo worst = null;

        for (BanInfo info : getBanInfo())
        {
            if (info.isRemoved())
                continue;

            if (info.isBan())
                continue;

            if (info.isExpired())
            {
                info.setBanState(BanState.EXPIRED);

                new MysqlSaveBanInfo(info);

                continue;
            }

            if (worst != null
                    && (worst.isPerm() || (!info.isPerm() || info.getBanExpires().getTime() < worst.getBanExpires().getTime())))
                continue;

            worst = info;
        }

        if (worst != null)
        {
            return Pair.of(Pair.of(worst.getBanner(), worst.getReason()), worst.getBanExpires().getTime());
        }

        return null;
    }
}
