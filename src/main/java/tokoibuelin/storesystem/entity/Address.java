package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;


public record Address(
        String addressId,
        String userId,
        String street,
        String rt,
        String rw,
        String village,
        String district,
        String city,
        String postalCode
) {

    public static final String TABLE_NAME = "addresses";

    public PreparedStatement insert(final Connection connection) {
        try {
            final String sql = "INSERT INTO " + "addresses" + " (user_id, street, rt, rw, village, district, city, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, userId);
            ps.setString(2, street);
            ps.setString(3, rt);
            ps.setString(4, rw);
            ps.setString(5, village);
            ps.setString(6, district);
            ps.setString(7, city);
            ps.setString(8, postalCode);
            System.out.println("Creating PreparedStatement with SQL: " + sql);
            System.out.println("Setting parameters: userId=" + userId + ", street=" + street + ", rt=" + rt + ", rw=" + rw + ", village=" + village + ", district=" + district + ", city=" + city + ", postalCode=" + postalCode);
            return ps;
        } catch (Exception e) {
            System.out.println("Error creating PreparedStatement: {}"+ e.getMessage());
            return null;
        }
    }




}
