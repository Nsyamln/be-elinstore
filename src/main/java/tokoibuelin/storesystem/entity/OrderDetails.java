package tokoibuelin.storesystem.entity;

import java.math.BigDecimal;

public record OrderDetails(
    String detailId,
     String orderId,
     String productId,
     String productName,
     Integer quantity,
     BigDecimal price,
     Integer unit
) {
     public static final String TABLE_NAME = "order_details";

}
