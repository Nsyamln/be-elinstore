package tokoibuelin.storesystem.model.response;

import tokoibuelin.storesystem.entity.Order;
import tokoibuelin.storesystem.entity.OrderDetails;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
        String orderId,
        String orderDate,
        String customerId,
        String customerName,
        String customerEmail,
        String deliveryAddress,
        String phone,
        Order.Status status,
        String createdBy,
        OffsetDateTime createdAt,
        Integer shippingCost,
        String courier,
        String shippingMethod,
        OffsetDateTime estimatedDeliveryDate,
        String paymentMethod,
        OffsetDateTime paymentTime,
        List<OrderDetails> orderDetails

) {
}

