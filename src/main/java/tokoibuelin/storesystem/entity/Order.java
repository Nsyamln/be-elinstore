
package tokoibuelin.storesystem.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.OffsetDateTime;

public record Order(
        String orderId,
        OffsetDateTime orderDate,
        String customerId,
        String deliveryAddress,
        String phone,
        Status status,
        String createdBy,
        OffsetDateTime createdAt,
        Integer shippingCost,
        String courier,
        String shippingMethod,
        OffsetDateTime estimatedDeliveryDate,
        String paymentMethod,
        OffsetDateTime paymentDate

) {
        public static final String TABLE_NAME = "orders";


        

        public PreparedStatement insert(final Connection connection) {
                try {
                        final String sql = "INSERT INTO orders (order_date, customer_id, delivery_address, phone,status, created_by, created_at, shipping_cost,courier,shipping_method,estimated_delivery_date) " +
                                "VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?,?,CURRENT_TIMESTAMP,?,?,?,?) RETURNING order_id ";
                        final PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                        System.out.println("cek ps order -> "+ps);
                        ps.setString(1, customerId);         
                        ps.setString(2, deliveryAddress);    
                        ps.setObject(3,phone);
                        ps.setString(4, status.toString());  
                        ps.setString(5, createdBy);          
                        ps.setInt(6,shippingCost);
                        ps.setString(7,courier);
                        ps.setString(8,shippingMethod);
                        ps.setObject(9,estimatedDeliveryDate);
                        
                        return ps;
                } catch (Exception e) {
                        System.out.println("Error preparing statement: {}"+ e.getMessage());
                        return null;
                }
        }

        public enum Status {
                PENDING, PAID, PROCESS, DELIVERY, DELIVERED;

                public static Status fromString(String str) {
                        if (PENDING.name().equals(str)) {
                                return PENDING;
                        } else if (PAID.name().equals(str)) {
                                return PAID;
                        } else if (PROCESS.name().equals(str)) {
                                return PROCESS;
                        } else if (DELIVERY.name().equals(str)) {
                                return DELIVERY;
                        } else {
                                return DELIVERED;
                        }
                }
        }
}