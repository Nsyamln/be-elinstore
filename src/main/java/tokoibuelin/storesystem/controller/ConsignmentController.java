package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.ConsignmentDto;
import tokoibuelin.storesystem.model.response.StockProductDto;
import tokoibuelin.storesystem.repository.ConsignmentRepository;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/secured/consignment")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class ConsignmentController{

    @Autowired
    private ConsignmentRepository consignmentRepository;

    @GetMapping("/paid-payments-summary/{supplierId}")
    public ResponseEntity<Map<String, Object>> getPaidPaymentsSummaryBySupplier(@PathVariable String supplierId) {
        Map<String, Object> pendingPaymentsSummary = consignmentRepository.getPaidPaymentsSummaryBySupplier(supplierId);
        return ResponseEntity.ok(pendingPaymentsSummary);
    }

    @GetMapping("/pending-payments-summary/{supplierId}")
    public ResponseEntity<Map<String, Object>> getPendingPaymentsSummaryBySupplier(@PathVariable String supplierId) {
        Map<String, Object> pendingPaymentsSummary = consignmentRepository.getPendingPaymentsSummaryBySupplier(supplierId);
        return ResponseEntity.ok(pendingPaymentsSummary);
    }

    @GetMapping("/getAllPayment/{supplierId}")
    public Response<Object> getAllPaymentsSummaryBySupplier(
            @PathVariable String supplierId) {
        List<ConsignmentDto> profitDtos = consignmentRepository.getAllPaymentsSummaryBySupplier(supplierId);
        return Response.create("09","00","Succes",profitDtos);
    }

    @PostMapping("/update-payment-status/{supplierId}")
    public ResponseEntity<String> updatePaymentStatus(@PathVariable String supplierId) {
        consignmentRepository.updatePaymentStatus(supplierId);
        return ResponseEntity.ok("Payment status updated successfully.");
    }

    @GetMapping("/getBySupplier/{supplierId}")
    public Response<Object> getBySupplier(@PathVariable String supplierId) {
        List<ConsignmentDto> listProfit =  consignmentRepository.getAll(supplierId);
        return Response.create("09","00","Succes",listProfit);
    }

    @GetMapping("/getStockProduct/{supplierId}")
    public Response<Object> getStockProduct(@PathVariable String supplierId) {
        // return precondition(authentication, User.Role.PEMASOK).orElseGet(() -> {
            List<StockProductDto> listProduct = consignmentRepository.getStockProductBySupplierId(supplierId);
            return Response.create("09", "00", "Success", listProduct);
        // });
    }
}
