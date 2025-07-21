package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;

public record Notification (

        Integer notif_id ,
        String description ,
        String category ,
        OffsetDateTime created_at ,
         OffsetDateTime deleted_at

){
    public static final String TABLE_NAME = "notification";
    public PreparedStatement insert(final Connection connection) {
        try {
            final String sql = "INSERT INTO " + "notification" + " (description, category,created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";

            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, description());
            ps.setString(2, category());
            return ps;
        } catch (Exception e) {
            System.out.println("Error creating PreparedStatement: {}"+ e.getMessage());
            return null;
        }
    }
}
