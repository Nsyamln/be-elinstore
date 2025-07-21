package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.OffsetDateTime;

public record Product (
        String productId,
        String productName,
        String description,
        Long unit,
        Long price,
        Long stock,
        String supplierId,
        String productImage,
        Integer categoryId,
        Long purchasePrice,
        String createdBy,
        String updatedBy,
        String deletedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt,
        OffsetDateTime restockDate

                      ) {

    public static final String TABLE_NAME = "products";
    public PreparedStatement insert(final Connection connection) throws SQLException { // Tambahkan throws SQLException
        final String sql = "INSERT INTO " + TABLE_NAME + " (product_name, description,unit, price, stock, supplier_id,product_image,category_id,purchase_price, created_by, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, productName);
        ps.setString(2, description);
        ps.setLong(3, unit);
        ps.setLong(4, price);
        ps.setLong(5, stock);
        ps.setString(6, supplierId);
        ps.setString(7, productImage);
        ps.setInt(8, categoryId);
        ps.setLong(9, purchasePrice);
        ps.setString(10, createdBy);
        return ps;
    }

     public boolean isConsignmentProduct() {
        return this.supplierId != null && !this.supplierId.trim().isEmpty();
    }

}