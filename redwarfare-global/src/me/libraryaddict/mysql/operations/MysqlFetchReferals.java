package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlFetchReferals extends DatabaseOperation
{
    public static class Referal
    {
        private Pair<UUID, String> _refered;
        private Timestamp _referedWhen;
        private Pair<UUID, String> _referer;
        private Timestamp _validatedWhen;

        public Referal(ResultSet rs) throws Exception
        {
            String refererName = null;

            try
            {
                refererName = rs.getString("name");
            }
            catch (SQLException ex)
            {
            }

            _referer = Pair.of(UUID.fromString(rs.getString("referer")), refererName);
            _refered = Pair.of(UUID.fromString(rs.getString("refered")), rs.getString("refered_name"));
            _referedWhen = rs.getTimestamp("when");
            _validatedWhen = rs.getTimestamp("completed");
        }

        public Timestamp getReferCompleted()
        {
            return _validatedWhen;
        }

        public Pair<UUID, String> getRefered()
        {
            return _refered;
        }

        public Timestamp getReferedWhen()
        {
            return _referedWhen;
        }

        public Pair<UUID, String> getReferer()
        {
            return _referer;
        }

        public boolean isCompleted()
        {
            return _validatedWhen != null;
        }
    }

    private ArrayList<Referal> _referals = new ArrayList<Referal>();

    public MysqlFetchReferals(UUID referer, UUID refered, String referedName)
    {
        this(referer, refered, referedName, null);
    }

    public MysqlFetchReferals(UUID referer, UUID refered, String referedName, boolean completed)
    {
        this(referer, refered, referedName, Boolean.valueOf(completed));
    }

    public MysqlFetchReferals(UUID referer, UUID refered, String referedName, Boolean completed)
    {
        try (Connection con = getMysql())
        {
            System.out.println(getStatement(referer, refered, referedName, completed));
            PreparedStatement stmt = con.prepareStatement(getStatement(referer, refered, referedName, completed));

            int i = 1;

            stmt.setInt(i++, KeyMappings.getKey("Name"));

            if (referer != null)
            {
                stmt.setString(i++, referer.toString());
            }

            if (referedName != null)
                stmt.setString(i++, referedName);

            if (refered != null)
                stmt.setString(i++, refered.toString());

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next())
            {
                _referals.add(new Referal(rs));
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public ArrayList<Referal> getReferals()
    {
        return _referals;
    }

    private String getStatement(UUID referer, UUID refered, String referedName, Boolean validated)
    {
        String join = "SELECT referals.*, playerinfo.info name FROM referals INNER JOIN playerinfo ON (referals.referer = playerinfo.uuid AND playerinfo.type = ? AND playerinfo.last_used = (SELECT MAX(playerinfo.last_used) FROM playerinfo WHERE playerinfo.uuid = referals.referer)) ";

        if (referer != null || referedName != null || refered != null)
            join += "WHERE ";

        if (referer != null)
        {
            join += "referer = ? ";

            if (refered != null || referedName != null)
                join += "AND ";
        }

        if (referedName != null)
        {
            join += "refered_name = ? ";

            if (refered != null)
                join += "AND ";
        }

        if (refered != null)
            join += "refered = ? ";

        if ((refered != null || referer != null) && validated != null)
            join += "AND ";

        if (validated != null)
            join += "completed is " + (validated ? "NOT " : "") + "NULL";

        return join;
    }
}
