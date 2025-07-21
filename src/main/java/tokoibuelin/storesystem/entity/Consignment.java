package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;

public record Consignment(
    String consignmentId,
    String transactionId,
    String productId,
    String supplierId,
    Integer productQuantity,
    Long totalPrice,
    String status,
    OffsetDateTime paymentDate

) {
    public static final String TABLE_NAME = "consignments";
    
    public PreparedStatement insert(final Connection connection) {
                try {
                        final String sql = "INSERT INTO consignments (transaction_id,  product_id, supplier_id, product_quantity, total_purchase_price,status) " +
                                "VALUES (?,?,?,?,?,?)";
                        final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1, transactionId);   
                        ps.setObject(2,productId);
                        ps.setString(3, supplierId);  
                        ps.setInt(4, productQuantity);          
                        ps.setLong(5,totalPrice);
                        ps.setString(6,status);
                        
                        return ps;
                } catch (Exception e) {
                        System.out.println("Error preparing statement: {}"+ e.getMessage());
                        return null;
                }
    }
    public enum Status {
                UNPAID, PAID;

                public static Status fromString(String str) {
                        if (UNPAID.name().equals(str)) {
                                return UNPAID;
                        } else {
                                return PAID;
                        }
                }
        }
} 
