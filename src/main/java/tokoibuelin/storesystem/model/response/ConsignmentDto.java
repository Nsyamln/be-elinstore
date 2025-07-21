package tokoibuelin.storesystem.model.response;

import java.math.BigDecimal;

public record ConsignmentDto(
        String consignmentId,
        String transactionId,
        String productId,
        String supplierId,
        int productQuantity,
        BigDecimal totalPurchasePrice,
        String status,
        String paymentDate,
        String productName 
                        
) {

    
}
