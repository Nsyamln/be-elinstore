package tokoibuelin.storesystem.model.response;

public record StockDto(
        String productId,
        String productName,
        Integer stock
) {
}


