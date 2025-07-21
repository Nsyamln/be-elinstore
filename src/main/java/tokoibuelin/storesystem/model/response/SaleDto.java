package tokoibuelin.storesystem.model.response;
import tokoibuelin.storesystem.entity.SaleDetails;
import java.util.List;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SaleDto(String saleId,
                      OffsetDateTime saleDate,
                      BigDecimal totalPrice,
                      String customerId,
                      String customerName,
                      String orderId,
                      BigDecimal amountPaid,
                      String paymentMethod,
                      List<SaleDetails> saleDetails) {
    public SaleDto(String saleId, OffsetDateTime saleDate, BigDecimal totalPrice, String customerId,String customerName,
                   String orderId, BigDecimal amountPaid, String paymentMethod,
                   List<SaleDetails> saleDetails) {
        this.saleId = saleId;
        this.saleDate = saleDate;
        this.totalPrice = totalPrice;
        this.customerId = customerId;
        this.customerName = customerName;
        this.orderId = orderId;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.saleDetails = saleDetails;
    }
    public List<SaleDetails> getSaleDetails() {
        return saleDetails;
    }
}
