package tokoibuelin.storesystem.model.response;

public record allProductDto (
        String productId,
        String productName,
        String description,
        Long unit,
        Long price,
        Long stock,
        String supplierId,
        String productImage,
        Long purchasePrice
) {
}
