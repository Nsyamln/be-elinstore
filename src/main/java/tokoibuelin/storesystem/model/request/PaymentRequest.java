package tokoibuelin.storesystem.model.request;

import java.util.List;

public record PaymentRequest(
        String orderId,

        List <ProductRequest> products,
        double grossAmount,
        double shippingCost,
        String customerName,
        String customerEmail
) {
}
