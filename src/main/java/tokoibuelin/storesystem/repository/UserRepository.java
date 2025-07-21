package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import tokoibuelin.storesystem.entity.Address;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.response.UsersDto;

import java.sql.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class UserRepository {
    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCustomerNameById(String customerId) {
        final String sql = "SELECT name FROM users WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{customerId}, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // Jika tidak ditemukan, kembalikan null
        }
    }

    public List<User> getSuppliersByRole(final String role) {
        if (role == null) {
            return Collections.emptyList(); // Kembalikan daftar kosong jika role adalah null
        }
        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT u.*, a.* FROM " + User.TABLE_NAME + " u LEFT JOIN addresses a ON u.user_id = a.user_id WHERE u.role = ? AND deleted_at IS NULL AND deleted_by IS NULL"
            );
            ps.setString(1, role);
            return ps;
        }, (rs, rowNum) -> {
            final String userId = rs.getString("user_id");
            final String name = rs.getString("name");
            final String email = rs.getString("email");
            final String password = rs.getString("password");
            final User.Role userRole = User.Role.valueOf(rs.getString("role"));
            final String phone = rs.getString("phone");
            final String createdBy = rs.getString("created_by");
            final String updatedBy = rs.getString("updated_by");
            final String deletedBy = rs.getString("deleted_by");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null
                    : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null
                    : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null
                    : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);

            return new User(userId, name, email, password, userRole, phone, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt);
        });
    }

    public List<UsersDto> getEmployWithAddress() {
        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                   "SELECT DISTINCT ON (u.user_id)\n" + //
                    "    u.*, a.* FROM users u LEFT JOIN addresses a ON u.user_id = a.user_id WHERE\n" + //
                    "    u.deleted_at IS NULL AND u.deleted_by IS NULL AND u.role NOT IN ('PEMASOK','PELANGGAN','PEMILIK')\n" + //
                    "ORDER BY\n" + //
                    "    u.user_id, a.address_id ASC;"
            );
            // ps.setString(1, role);
            return ps;
        }, (rs, rowNum) -> {
            final String userId = rs.getString("user_id");
            final String name = rs.getString("name");
            final String email = rs.getString("email");
            final String password = rs.getString("password");
            final String phone = rs.getString("phone");
            final String role = rs.getString("role");
            final String street = rs.getString("street");
            final String rt = rs.getString("rt");
            final String rw = rs.getString("rw");
            final String village = rs.getString("village");
            final String district = rs.getString("district");
            final String city = rs.getString("city");
            final String postalCode = rs.getString("postal_code");
            
            return new UsersDto(userId, name, email, password, phone, role, street, rt, rw, village, district, city,postalCode);
        });
    }

    public List<UsersDto> getUsersWithAddress(String role) {
        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                   "SELECT DISTINCT ON (u.user_id)\n" + //
                    "    u.*, a.* FROM users u LEFT JOIN addresses a ON u.user_id = a.user_id WHERE\n" + //
                    "    u.deleted_at IS NULL AND u.deleted_by IS NULL AND u.role =?\n" + //
                    "ORDER BY\n" + //
                    "    u.user_id, a.address_id ASC;"
            );
             ps.setString(1, role);
            return ps;
        }, (rs, rowNum) -> {
            final String userId = rs.getString("user_id");
            final String name = rs.getString("name");
            final String email = rs.getString("email");
            final String password = rs.getString("password");
            final String phone = rs.getString("phone");
            final String rolee = rs.getString("role");
            final String street = rs.getString("street");
            final String rt = rs.getString("rt");
            final String rw = rs.getString("rw");
            final String village = rs.getString("village");
            final String district = rs.getString("district");
            final String city = rs.getString("city");
            final String postalCode = rs.getString("postal_code");
            
            return new UsersDto(userId, name, email, password, phone, rolee, street, rt, rw, village, district, city,postalCode);
        });
    }

     public Optional<UsersDto> findWithAddressById(String id) {
        System.out.println("ID NYA ? "+id);
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT DISTINCT ON (u.user_id)\n" + //
                    "    u.*, a.* FROM users u LEFT JOIN addresses a ON u.user_id = a.user_id WHERE\n" + //
                    "    u.user_id = ? AND u.deleted_at IS NULL AND u.deleted_by IS NULL\n" + //
                    "ORDER BY\n" + //
                    "    u.user_id, a.address_id ASC;");
            ps.setString(1, id);
            return ps;
        }, rs -> {
            if (rs.next()) {
                final String userId = rs.getString("user_id");
                final String name = rs.getString("name");
                final String email = rs.getString("email");
                final String password = rs.getString("password");
                final String phone = rs.getString("phone");
                final String role = rs.getString("role");
                final String street = rs.getString("street");
                final String rt = rs.getString("rt");
                final String rw = rs.getString("rw");
                final String village = rs.getString("village");
                final String district = rs.getString("district");
                final String city = rs.getString("city");
                final String postalCode = rs.getString("postal_code");
        
                return new UsersDto(userId, name, email, password, phone, role, street, rt, rw, village, district, city,postalCode);

            }
            return null;
        }));
    }

    public Optional<User> findByEmail(final String email) {
        if (email == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT u.*, a.* FROM " + User.TABLE_NAME + " u LEFT JOIN addresses a ON u.user_id = a.user_id WHERE u.email = ?");
            ps.setString(1, email);
            return ps;

        }, rs -> {
            if (!rs.next() ) {
                return null;
            }
            final String userId = rs.getString("user_id");
            final String name = rs.getString("name");
//            final String email = rs.getString("email");
            final String password = rs.getString("password");
            final User.Role userRole = User.Role.valueOf(rs.getString("role"));
            final String phone = rs.getString("phone");
            final String createdBy = rs.getString("created_by");
            final String updatedBy = rs.getString("updated_by");
            final String deletedBy = rs.getString("deleted_by");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null
                    : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null
                    : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null
                    : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);

            return new User(userId, name, email, password, userRole, phone, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt);
        }));
    }

    public Optional<User> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT u.*, a.* FROM " + User.TABLE_NAME + " u LEFT JOIN addresses a ON u.user_id = a.user_id WHERE u.user_id = ?");
            ps.setString(1, id);
            return ps;
        }, rs -> {
            if (rs.next()) {
                final String userId = rs.getString("user_id");
                final String name = rs.getString("name");
                final String email = rs.getString("email");
                final String password = rs.getString("password");
                final User.Role userRole = User.Role.valueOf(rs.getString("role"));
                final String phone = rs.getString("phone");
                final String createdBy = rs.getString("created_by");
                final String updatedBy = rs.getString("updated_by");
                final String deletedBy = rs.getString("deleted_by");
                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null
                        : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null
                        : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null
                        : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
        
                return new User(userId, name, email, password, userRole, phone,  createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt);

            }
            return null;
        }));
    }

    public boolean updateUserCreatedBy(String userId, String createdById) {
        log.info("Attempting to update created_by for userId: {} with createdById: {}", userId, createdById); // Log input
        try {
            String sql = "UPDATE " + User.TABLE_NAME + " SET created_by=? WHERE user_id=?"; // Atau user_id=?

            int rowsUpdated = jdbcTemplate.update(sql, createdById, userId);
            log.info("Rows updated for user id {}: {}", userId, rowsUpdated); // Log hasil update

            if (rowsUpdated > 0) {
                return true;
            } else {
                log.warn("Failed to update created_by for user id {}. No rows were affected. This might indicate an incorrect ID or column name in the WHERE clause.", userId);
                return false;
            }
        } catch (DataAccessException e) {
            log.error("Data access error while updating created_by for user id {}: {}", userId, e.getMessage(), e); // Log error lengkap
            return false;
        }
    }

    public String saveUser(final User user) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            int updateCount = jdbcTemplate.update(con -> {
                PreparedStatement ps = user.insert(con);
                return ps;
            }, keyHolder);

            if (updateCount != 1) {
                return null;
            }

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("user_id")) {
                return (String) keys.get("user_id");
            }

            return null;
        } catch (Exception e) {
            log.error("Error during saveSupplier: {}", e.getMessage());
            return null;
        }
    }

    public String resetPassword(String userId, String newPassword) {
        try {
            int rowsUpdated = jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement("UPDATE " + User.TABLE_NAME
                        + " SET password=?, updated_by=?, updated_at=CURRENT_TIMESTAMP WHERE user_id=?");
                ps.setString(1, newPassword);
                ps.setString(2, userId);
                ps.setString(3, userId);
                return ps;
            });

            if (rowsUpdated > 0) {
                return userId;
            } else {
                return null;
            }
        } catch (DataAccessException e) {
            System.err.println("Error updating password for user id " + userId + ": " + e.getMessage());
            return null;
        }
    }

    public long deletedUser(User user) {
        try {
            String sql = "UPDATE " + User.TABLE_NAME + " SET deleted_at=CURRENT_TIMESTAMP, deleted_by=? WHERE user_id=?";
            return jdbcTemplate.update(sql, user.deletedBy(), user.userId());
        } catch (Exception e) {
            log.error("Failed to soft delete user: {}", e.getMessage());
            return 0L;
        }
    }

    public boolean updateNameUser(final User user) {
        final String sql = "UPDATE " + User.TABLE_NAME + " SET name = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        System.out.println("id name baru : "+user.userId());
        System.out.println("cek name baru : "+user.name());
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, user.name());
                ps.setString(2, user.userId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            return false;
        }
    }

    public boolean updatePassUser(final User user) {
        final String sql = "UPDATE " + User.TABLE_NAME + " SET password = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        System.out.println("id name baru : "+user.userId());
        System.out.println("cek name baru : "+user.name());
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, user.password());
                ps.setString(2, user.userId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateTelpUser(final User user) {
        final String sql = "UPDATE " + User.TABLE_NAME + " SET phone = ?, updated_at = CURRENT_TIMESTAMP WHERE user_id = ?";

        System.out.println("id name baru : "+user.userId());
        System.out.println("cek name baru : "+user.name());
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, user.phone());
                ps.setString(2, user.userId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Failed to update user: {}", e.getMessage());
            return false;
        }
    }

    public boolean saveResetToken(String email, String token) {
        try {
            // First, delete any existing tokens for this email
            jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE email = ?", email);
            
            // Insert new token with expiration time (24 hours from now)
            return jdbcTemplate.update(
                "INSERT INTO password_reset_tokens (email, token, expires_at) VALUES (?, ?, ?)",
                email,
                token,
                OffsetDateTime.now().plusHours(24)
            ) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<String> validateResetToken(String token) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                "SELECT email FROM password_reset_tokens WHERE token = ? AND expires_at > ?",
                String.class,
                token,
                OffsetDateTime.now()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void deleteResetToken(String token) {
        try {
            jdbcTemplate.update("DELETE FROM password_reset_tokens WHERE token = ?", token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean updateUsers(final User user) {
        final String sql = "UPDATE " + User.TABLE_NAME + " SET name = ?, email = ?, phone = ?, updated_by=? WHERE user_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, user.name());
                ps.setString(2,user.email());
                ps.setString(3, user.phone());
                ps.setString(4,user.updatedBy());
                ps.setString(5,user.userId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Failed to update Address: {}", e.getMessage());
            return false;
        }
    }

}
