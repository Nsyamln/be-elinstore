package tokoibuelin.storesystem.entity;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;



public record Sale(
        String saleId,
        OffsetDateTime saleDate,
        BigDecimal totalPrice,
        BigDecimal amountPaid,
        String paymentMethod,
        String modifiedBy
) {

    public static final String TABLE_NAME = "sales";

    public PreparedStatement insert(final Connection connection) {
        try {
            final String sql = "INSERT INTO sales (sale_date, total_price,amount_paid,payment_method, modified_by) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?)";
            final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            ps.setBigDecimal(1, totalPrice);
            ps.setBigDecimal(2, amountPaid);
            ps.setString(3, paymentMethod);
            ps.setString(4, modifiedBy);

            System.out.println("Log ps2 -> "+ps);
            return ps;
        } catch (Exception e) {
            System.out.println("Error during saveOrder: {}" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}

