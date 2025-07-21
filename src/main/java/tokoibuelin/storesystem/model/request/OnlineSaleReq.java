package tokoibuelin.storesystem.model.request;

import tokoibuelin.storesystem.model.response.OrderDetailDto;

import java.time.OffsetDateTime;
import java.util.List;

public record OnlineSaleReq (
    String customerId,
    String deliveryAddress,
    String phone,
    String paymentMethod,
    String courier,
    Integer shippingCost,
    String shippingMethod,
    OffsetDateTime estimatedDelivery,

    List<OrderDetailDto> orderDetail
){
}
