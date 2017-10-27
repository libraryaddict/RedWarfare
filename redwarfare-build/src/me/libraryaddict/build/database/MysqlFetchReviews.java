package me.libraryaddict.build.database;

import me.libraryaddict.network.DatabaseOperation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

public class MysqlFetchReviews extends DatabaseOperation {
    private int _rating;
    private int _reviewers;

    public MysqlFetchReviews(UUID map) {
        try (Connection con = getMysql()) {
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT IFNULL(SUM(rating), 0) rating, IFNULL(COUNT(rating), 0) reviewers FROM map_reviews WHERE " +
                            "" + "" + "map = ?");

            stmt.setString(1, map.toString());

            ResultSet rs = stmt.executeQuery();

            rs.first();

            _rating = rs.getInt("rating");
            _reviewers = rs.getInt("reviewers");

            setSuccess();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getRating() {
        return _rating;
    }

    public int getReviewers() {
        return _reviewers;
    }
}
