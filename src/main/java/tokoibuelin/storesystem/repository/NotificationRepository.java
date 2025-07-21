package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.Notification;
import tokoibuelin.storesystem.model.Response;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Repository
public class NotificationRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    @Autowired
    private  JdbcTemplate jdbcTemplate;

    public List<Notification> getAll() {
        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Notification.TABLE_NAME + " WHERE deleted_at is NULL");
            return ps;
        }, (rs, rowNum) -> {
            final Integer notificationId = rs.getInt("notif_id");
            final String category = rs.getString("category");
            final String description = rs.getString("description");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null
                    : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null
                    : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);

            return new Notification(notificationId, description, category, createdAt, deletedAt);
        });
    }


    public Response countNotifications() {
        try {
            // SQL query to count notifications that are not deleted
            String sql = "SELECT COUNT(*) FROM notification WHERE deleted_at IS NULL";

            // Execute the query and get the count
            Long count = jdbcTemplate.queryForObject(sql, Long.class);

            // Return a success response with the count
            return Response.create("09", "00", "Sukses", count != null ? count : 0L);
        } catch (Exception e) {
            // Print stack trace for debugging and return an error response
            e.printStackTrace();
            return Response.create("01", "00", "Database error", null);
        }
    }


    public long deleteNotification(final Integer notifId) {
        try {
            String sql = "UPDATE " + Notification.TABLE_NAME + " SET deleted_at=CURRENT_TIMESTAMP WHERE notif_id = ?";
            return jdbcTemplate.update(sql, notifId);
        } catch (Exception e) {
            log.error("Gagal untuk menghapus notifikasi : {}", e.getMessage());
            return 0L;
        }
    }
    public boolean notificationExists(String description) {
        String sql = "SELECT COUNT(*) FROM notification WHERE description = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{description}, Integer.class);
        return count != null && count > 0;
    }


}
