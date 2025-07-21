package tokoibuelin.storesystem.model.request;

import tokoibuelin.storesystem.entity.SaleDetails;

import java.math.BigDecimal;
import java.util.List;

public record  OfflineSaleReq(
     BigDecimal totalPrice,
     String paymentMethod,
     BigDecimal amountPaid,
     List<SaleDetails> saleDetails) {
}
