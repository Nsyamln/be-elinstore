package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public record Category(
        Integer categoryId,
        String categoryName
) {

    public static final String TABLE_NAME = "categories";

    public PreparedStatement insert(final Connection connection) {
        
        try {
            final String sql = "INSERT INTO " + "categories" + " (category_name) VALUES (?)";

            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, categoryName);
            
            return ps;
        } catch (Exception e) {
            System.out.println("Error creating PreparedStatement: {}"+ e.getMessage());
            return null;
        }
    }




}
