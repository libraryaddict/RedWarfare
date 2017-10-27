package me.libraryaddict.build.database;

import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

public class MysqlRemoveReview extends DatabaseOperation {
    public MysqlRemoveReview(UUID map, UUID player) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM map_reviews WHERE map = ? AND player = ?");

            stmt.setString(1, map.toString());
            stmt.setString(2, player.toString());

            stmt.execute();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
