package tokoibuelin.storesystem.controller;

import com.midtrans.httpclient.SnapApi;
import com.midtrans.httpclient.error.MidtransError;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.PaymentRequest;
import tokoibuelin.storesystem.model.request.ProductRequest;
import tokoibuelin.storesystem.service.OrderService;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/payment")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class PaymentController {

    @Autowired
    OrderService orderService;

    @PostMapping("/create-transaction")
    public Map<String, String> createTransaction(@RequestBody PaymentRequest paymentRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            // Prepare transaction parameters
            System.out.println("Received Payment Request: " + paymentRequest.toString());
            if (paymentRequest.orderId() == null || paymentRequest.orderId().isEmpty()) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (paymentRequest.grossAmount() <= 0) {
                throw new IllegalArgumentException("Gross amount must be greater than 0");
            }

            Map<String, Object> param = new HashMap<>();
            Map<String, String> transactionDetails = new HashMap<>();
            transactionDetails.put("order_id", paymentRequest.orderId());

            JSONArray itemDetails = new JSONArray();

            double calculatedGrossAmount = 0;

            for (ProductRequest product : paymentRequest.products()) {
                if (product.productQuantity() <= 0) {
                    throw new IllegalArgumentException("Product quantity must be greater than or equal to 1");
                }
                if (product.productName() == null || product.productName().isEmpty()) {
                    throw new IllegalArgumentException("Product name is required");
                }

                JSONObject item = new JSONObject();
                item.put("id", product.productId());
                item.put("name", product.productName());
                item.put("price", product.productPrice());
                item.put("quantity", product.productQuantity());
                itemDetails.put(item);

                calculatedGrossAmount += product.productPrice() * product.productQuantity();
            }

            // Tambahkan biaya pengiriman ke item_details
            if (paymentRequest.shippingCost() > 0) {
                JSONObject shippingItem = new JSONObject();
                shippingItem.put("id", "SHIPPING");
                shippingItem.put("name", "Shipping Cost");
                shippingItem.put("price", paymentRequest.shippingCost());
                shippingItem.put("quantity", 1); // Quantity untuk ongkir bisa 1
                itemDetails.put(shippingItem);

                // Tambahkan shippingCost ke grossAmount yang dihitung
                calculatedGrossAmount += paymentRequest.shippingCost();
            }

            // Bandingkan grossAmount yang dihitung dengan grossAmount dari permintaan
            if (paymentRequest.grossAmount() != calculatedGrossAmount) {
                throw new IllegalArgumentException("Gross amount must be equal to the sum of item details including shipping cost");
            }

            // Masukkan item_details ke dalam param
            param.put("item_details", itemDetails);

            // Masukkan gross_amount yang dihitung
            transactionDetails.put("gross_amount", String.valueOf(calculatedGrossAmount));
            param.put("transaction_details", transactionDetails);

            // Siapkan customer details
            Map<String, String> customerDetails = new HashMap<>();
            customerDetails.put("first_name", paymentRequest.customerName());
            customerDetails.put("email", paymentRequest.customerEmail());
            param.put("customer_details", customerDetails);

            System.out.println("Constructed Param: " + param.toString());

            // Panggil SnapApi untuk membuat transaksi
            JSONObject snapResponse = SnapApi.createTransaction(param);
            System.out.println("=== RESPONSE DARI SNAP API ===");
              System.out.println(snapResponse.toString());

            // Kembalikan token transaksi ke frontend
            response.put("token", snapResponse.getString("token"));

            if (snapResponse.has("token")) {
                response.put("token", snapResponse.getString("token"));
            } else if (snapResponse.has("error_messages")) {
                response.put("error", snapResponse.getJSONArray("error_messages").toString());
            } else {
                response.put("error", "Unknown error from Midtrans: " + snapResponse.toString());
            }

            return response;

        } catch (MidtransError e) {
            response.put("error", "Midtrans Error: " + e.getMessage());
        } catch (Exception e) {
            response.put("error", "Error: " + e.getMessage());
        }
        return response;
    }

    @PutMapping("/update-status/{orderId}/{paymentMethod}/{paymentTime}")
    public Response<Object> updateStatusOrder(
        @PathVariable String orderId, 
        @PathVariable String paymentMethod,
        @PathVariable OffsetDateTime paymentTime
        
        ) {
        return orderService.paidOrder(orderId, paymentMethod, paymentTime);
    }

}
