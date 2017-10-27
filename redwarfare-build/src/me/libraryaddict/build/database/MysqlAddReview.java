package me.libraryaddict.build.database;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;

public class MysqlAddReview extends DatabaseOperation {
    public MysqlAddReview(UUID map, UUID player, int rating) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO map_reviews (map, player, rating) VALUES (?,?,?) ON DUPLICATE KEY UPDATE rating = ?," +
                            "" + "" + " `timestamp` = ?;");

            stmt.setString(1, map.toString());
            stmt.setString(2, player.toString());
            stmt.setInt(3, rating);
            stmt.setInt(4, rating);
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            stmt.execute();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
