package me.libraryaddict.build.database;

import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class MysqlDeleteBackups extends DatabaseOperation {
    public MysqlDeleteBackups(MapInfo info, ArrayList<String> backups) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM map_backups WHERE map = ? AND `date` = ?");
            stmt.setString(1, info.getUUID().toString());

            for (String backup : backups) {
                stmt.setString(2, backup);
                stmt.execute();
            }

            setSuccess();
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }
}
