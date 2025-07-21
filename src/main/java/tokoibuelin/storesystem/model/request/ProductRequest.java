package tokoibuelin.storesystem.model.request;

public record ProductRequest(
        String productId,
        String productName,
        int productQuantity,
        double productPrice
) {
}
