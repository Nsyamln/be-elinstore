package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;


public record User(String userId,
                   String name,
                   String email,
                   String password,
                   Role role,
                   String phone,
                   String createdBy,
                   String updatedBy,
                   String deletedBy,
                   OffsetDateTime createdAt, 
                   OffsetDateTime updatedAt, 
                   OffsetDateTime deletedAt


) {

    public static final String TABLE_NAME = "users";

    public PreparedStatement insert(final Connection connection) {
        try {
            final String sql = "INSERT INTO " + TABLE_NAME + " (name, email, password, role, phone, created_by, created_at) VALUES (?, ?, ?, ?,?, ?, CURRENT_TIMESTAMP)";
            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            System.out.println("cek ps user -> "+ps);
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role.name());
            ps.setString(5, phone);
            ps.setString(6, createdBy);
            return ps;
        } catch (Exception e) {
            return null;
        }
    }

    public enum Role {
        ADMIN, PEMASOK,PELANGGAN,PEMILIK, UNKNOWN, PEGAWAI,PRODUKSI;

        public static Role fromString(String str) {
            if (ADMIN.name().equals(str)) {
                return ADMIN;
            } else if (PELANGGAN.name().equals(str)) {
                return PELANGGAN;
            } else if (PEMASOK.name().equals(str)) {
                return PEMASOK;
            } else if (PEMILIK.name().equals(str)) {
                return PEMILIK;
            }else if (PEGAWAI.name().equals(str)) {
                    return PEGAWAI;
            }else if (PRODUKSI.name().equals(str)) {
                    return PRODUKSI;
            } else {
                return UNKNOWN;
            }
        }
    }


}
