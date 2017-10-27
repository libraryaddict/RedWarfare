package me.libraryaddict.build.database;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map.Entry;
import java.util.UUID;

public class MysqlSaveMapInfo extends DatabaseOperation {
    public MysqlSaveMapInfo(MapInfo mapInfo) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO maps " + "(uuid, name, creator, time_created, last_modified, map_type, `desc`, " +
                            "ip_loc, file_loc, backup_loc, backup_version, deleted, backup_freq, last_backup, " +
                            "visitors, releasable)" + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY " +
                            "UPDATE " + "name = ?, last_modified = ?, map_type = ?, `desc` = ?, ip_loc = ?, file_loc " +
                            "" + "" + "= ?, backup_loc = ?, backup_version = ?, deleted = ?, backup_freq = ?, " +
                            "last_backup " + "= ?, " + "creator = ?, visitors = ?, releasable = ?;");

            stmt.setString(1, mapInfo.getUUID().toString());
            stmt.setString(2, mapInfo.getName());
            stmt.setString(3, mapInfo.getCreatorUUID().toString());
            stmt.setTimestamp(4, mapInfo.getTimeCreated());
            stmt.setTimestamp(5, mapInfo.getTimeModified());
            stmt.setInt(6, KeyMappings.getKey(mapInfo.getMapType().name()));
            stmt.setString(7, mapInfo.getDescription());
            stmt.setString(8, mapInfo.getIPLoc());
            stmt.setString(9, mapInfo.getFileLoc());
            stmt.setString(10, mapInfo.getBackup());
            stmt.setString(11, mapInfo.getBackupVersion());
            stmt.setBoolean(12, mapInfo.isDeleted());
            stmt.setInt(13, mapInfo.getBackupFrequency());
            stmt.setTimestamp(14, mapInfo.getLastBackup());
            stmt.setBoolean(15, mapInfo.isAllowVisitors());
            stmt.setBoolean(16, mapInfo.isReleasable());

            stmt.setString(17, mapInfo.getName());
            stmt.setTimestamp(18, mapInfo.getTimeModified());
            stmt.setInt(19, KeyMappings.getKey(mapInfo.getMapType().name()));
            stmt.setString(20, mapInfo.getDescription());
            stmt.setString(21, mapInfo.getIPLoc());
            stmt.setString(22, mapInfo.getFileLoc());
            stmt.setString(23, mapInfo.getBackup());
            stmt.setString(24, mapInfo.getBackupVersion());
            stmt.setBoolean(25, mapInfo.isDeleted());
            stmt.setInt(26, mapInfo.getBackupFrequency());
            stmt.setTimestamp(27, mapInfo.getLastBackup());
            stmt.setString(28, mapInfo.getCreatorUUID().toString());
            stmt.setBoolean(29, mapInfo.isAllowVisitors());
            stmt.setBoolean(30, mapInfo.isReleasable());

            stmt.execute();

            stmt = con.prepareStatement(
                    "INSERT INTO map_builders (player, map, rank) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE rank = ?");

            for (Entry<Pair<UUID, String>, MapRank> builder : mapInfo.getBuilders().entrySet()) {
                if (builder.getValue() == null)
                    continue;

                stmt.setString(1, builder.getKey().getKey().toString());
                stmt.setString(2, mapInfo.getUUID().toString());
                stmt.setInt(3, KeyMappings.getKey(builder.getValue().name()));
                stmt.setInt(4, KeyMappings.getKey(builder.getValue().name()));

                stmt.execute();
            }

            stmt = con.prepareStatement("DELETE FROM map_builders WHERE player = ? AND map = ?");

            for (Entry<Pair<UUID, String>, MapRank> builder : mapInfo.getBuilders().entrySet()) {
                if (builder.getValue() != null)
                    continue;

                stmt.setString(1, builder.getKey().getKey().toString());
                stmt.setString(2, mapInfo.getUUID().toString());

                stmt.execute();
            }

            if (mapInfo.getBackup() != null) {
                stmt = con.prepareStatement("INSERT IGNORE INTO map_backups (map, date) VALUES (?,?)");
                stmt.setString(1, mapInfo.getUUID().toString());
                stmt.setString(2, mapInfo.getBackup());

                stmt.execute();
            }
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
