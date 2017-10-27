package me.libraryaddict.build.database;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class MysqlFetchMapInfo extends DatabaseOperation {
    private ArrayList<MapInfo> _maps = new ArrayList<MapInfo>();

    public MysqlFetchMapInfo() {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT e.*, s1.info playername, IFNULL(SUM(r1.rating), 0) rating, IFNULL(COUNT(r1.rating), 0) "
                            + "reviewers FROM maps e INNER JOIN playerinfo s1 ON (e.creator = s1.uuid AND s1.type = "
                            + KeyMappings
                            .getKey("Name") + ") LEFT OUTER JOIN playerinfo s2 ON (e.creator = s2.uuid AND s1.id < "
                            + "s2.id AND s2.type = s1.type)  LEFT OUTER JOIN map_reviews r1 ON e.uuid = r1.map WHERE " +
                            "" + "" + "s2.uuid IS NULL AND e.deleted = 0 GROUP BY e.uuid");

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next()) {
                _maps.add(new MapInfo(rs));
            }

            stmt = con.prepareStatement(
                    "SELECT map_builders.player, map_builders.rank, playerinfo.info FROM map_builders INNER JOIN " +
                            "playerinfo ON (map_builders.player = playerinfo.uuid AND playerinfo.type = ? AND " +
                            "playerinfo.last_used = (SELECT MAX(playerinfo.last_used) FROM playerinfo WHERE " +
                            "playerinfo.uuid = map_builders.player)) WHERE map_builders.map = ? ORDER BY " +
                            "`playerinfo`" + ".`info` ASC");

            stmt.setInt(1, KeyMappings.getKey("Name"));

            for (MapInfo info : _maps) {
                stmt.setString(2, info.getUUID().toString());

                rs = stmt.executeQuery();

                info.setBuilders(rs);
            }

            setSuccess();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    public MysqlFetchMapInfo(boolean includeDeleted) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT e.*, s1.info playername FROM maps e INNER JOIN playerinfo s1 ON (e.creator = s1.uuid AND " +
                            "" + "" + "s1.type = " + KeyMappings
                            .getKey("Name") + ") LEFT OUTER JOIN playerinfo s2 ON (e.creator = s2.uuid AND s1.id < "
                            + "s2.id AND s2.type = s1.type) WHERE s2.uuid IS NULL;");

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next()) {
                _maps.add(new MapInfo(rs));
            }

            setSuccess();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    public ArrayList<MapInfo> getMaps() {
        return _maps;
    }
}
