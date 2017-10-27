package me.libraryaddict.build.database;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.RemoteFileManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class MysqlDeleteDeletedMaps extends DatabaseOperation {
    public MysqlDeleteDeletedMaps(String ip) {
        if ("".isEmpty())
            return;
        ArrayList<MapInfo> maps = new ArrayList<MapInfo>();

        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT e.*, s1.info playername FROM maps e INNER JOIN playerinfo s1 ON (e.creator = s1.uuid AND " +
                            "" + "s1.type = " + KeyMappings
                            .getKey("Name") + ") LEFT OUTER JOIN playerinfo s2 ON (e.creator = s2.uuid AND s1.id < "
                            + "s2.id AND s2.type = s1.type) AND e.deleted = 1 WHERE s2.uuid IS NULL;");

            ResultSet rs = stmt.executeQuery();

            rs.beforeFirst();

            while (rs.next()) {
                MapInfo mapInfo = new MapInfo(rs);

                maps.add(mapInfo);
            }

            setSuccess();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        int i = 0;

        for (MapInfo info : maps) {
            RemoteFileManager file = new RemoteFileManager(ip, info);

            try {

                file.deleteRemoteFile(null, info.getFileLoc());
            }
            catch (Exception e) {
            }
            try {
                file.deleteRemoteFile(null, info.getFileLoc());
            }
            catch (Exception e) {
            }

            if (i++ % 10 == 0)
                System.out.println("Deleted " + i + " maps...");
        }

        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM `maps` WHERE `uuid` = ?");

            for (MapInfo info : maps) {
                stmt.setString(1, info.getUUID().toString());
                stmt.execute();
            }

            setSuccess();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        System.out.println("Finished deleting " + i + " maps");
    }
}
