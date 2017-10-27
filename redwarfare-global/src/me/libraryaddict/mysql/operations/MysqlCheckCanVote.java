package me.libraryaddict.mysql.operations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.network.DatabaseOperation;

public class MysqlCheckCanVote extends DatabaseOperation
{
    private HashMap<String, ArrayList<Timestamp>> _canVote = new HashMap<String, ArrayList<Timestamp>>();

    public MysqlCheckCanVote(UUID uuid, String... sites)
    {
        try (Connection con = getMysql())
        {
            PreparedStatement stmt = con
                    .prepareStatement("SELECT `date` FROM vote_log WHERE player = ? AND site = ? ORDER BY `date` DESC");

            stmt.setString(1, uuid.toString());

            for (String site : sites)
            {
                stmt.setInt(2, KeyMappings.getKey(site));

                ResultSet rs = stmt.executeQuery();

                rs.beforeFirst();

                ArrayList<Timestamp> stamps = new ArrayList<Timestamp>();

                while (rs.next())
                {
                    stamps.add(rs.getTimestamp("date"));
                }

                if (stamps.isEmpty())
                    continue;

                _canVote.put(site, stamps);
            }

            setSuccess();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    public boolean canAcceptVote(String site)
    {
        Timestamp lastVote = getLastVote(site);

        return lastVote == null || UtilTime.elasped(lastVote, (UtilTime.DAY / 2) * 1000);
    }

    public boolean displayedVoted(String site)
    {
        Timestamp lastVote = getLastVote(site);

        return lastVote != null && !UtilTime.elasped(lastVote, (UtilTime.DAY - UtilTime.HOUR) * 1000);
    }

    public int getCurrentStreak(String site)
    {
        ArrayList<Timestamp> votes = getVotes(site);

        if (votes == null)
            return 0;

        int streak = 0;

        for (int i = 0; i < votes.size(); i++)
        {
            if (i == 0)
            {
                if (UtilTime.elasped(votes.get(i), UtilTime.DAY * 1500))
                    break;
            }
            else
            {
                if (votes.get(i - 1).getTime() - votes.get(i).getTime() > UtilTime.DAY * 1000)
                    break;
            }

            streak++;
        }

        return streak;
    }

    public Timestamp getLastVote(String site)
    {
        if (_canVote.get(site) != null)
            return _canVote.get(site).get(0);

        return null;
    }

    public ArrayList<Timestamp> getVotes(String site)
    {
        return _canVote.get(site);
    }
}
