package tokoibuelin.storesystem.model.response;

import java.time.OffsetDateTime;

public record StockProductDto(
        String productId,
        String productName,
        Integer stock,
        Long price,
        OffsetDateTime restockDate
) {
}
