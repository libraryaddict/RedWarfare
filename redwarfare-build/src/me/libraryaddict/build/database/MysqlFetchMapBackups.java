package me.libraryaddict.build.database;

import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MysqlFetchMapBackups extends DatabaseOperation {
    private ArrayList<Pair<String, Timestamp>> _backups = new ArrayList<Pair<String, Timestamp>>();

    public MysqlFetchMapBackups(MapInfo info) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con
                    .prepareStatement("SELECT * FROM map_backups WHERE map = ? ORDER BY `timestamp` DESC");
            stmt.setString(1, info.getUUID().toString());

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next()) {
                _backups.add(Pair.of(rs.getString("date"), rs.getTimestamp("timestamp")));
            }

            setSuccess();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    public ArrayList<Pair<String, Timestamp>> getBackups() {
        return _backups;
    }
}
