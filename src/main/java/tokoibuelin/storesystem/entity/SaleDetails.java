package tokoibuelin.storesystem.entity;

import java.math.BigDecimal;

public record SaleDetails(
     String detailId,
     String saleId,
     String productId,
     String productName,
     Integer quantity,
     BigDecimal price


) {
    public static final String TABLE_NAME = "sale_details";


}
