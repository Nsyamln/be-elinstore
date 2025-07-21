package tokoibuelin.storesystem.model.response;

import java.math.BigDecimal;

public record OrderDetailDto (
        String productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        Integer unit
){
    
}
