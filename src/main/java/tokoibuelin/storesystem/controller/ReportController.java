package tokoibuelin.storesystem.controller;

import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.SalesDto;
import tokoibuelin.storesystem.repository.SaleRepository;
import tokoibuelin.storesystem.service.ReportService;
import tokoibuelin.storesystem.util.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/secured/report")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class ReportController  {
    @Autowired
    private ReportService reportService;

    @Autowired
    private SaleRepository saleRepository;
    
    @GetMapping("/getSaldoAwal/{startDate}")
    public Response<Object> getIncome(@PathVariable String startDate) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        if (authentication == null) {
            return Response.create("01", "00", "Unauthorized", null);
        }
        return reportService.getSaldoAwal(authentication, startDate);
    }

    @GetMapping("/download-reportsale")
    public ResponseEntity<byte[]> downloadSalesReport(
            @RequestParam String startDate,
            @RequestParam String endDate,@RequestParam String paymentMethod) {
        try {
            ByteArrayOutputStream outputStream;

            if ("SEMUA".equals(paymentMethod)) {
                outputStream = reportService.createSaleReportByMethodAll(startDate, endDate);
            } else {
                outputStream = reportService.createSaleReportByMethod(paymentMethod, startDate, endDate);
            }

            // Check if outputStream is not null
            if (outputStream == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate report".getBytes());
            }

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Laporan_Penjualan.pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to generate sales report: " + e.getMessage()).getBytes());
        }
    }
    @GetMapping("/by-payment-method/{paymentMethod}/{startDate}/{endDate}")
    public ResponseEntity<List<SalesDto>> getSalesByPaymentMethod(@PathVariable String paymentMethod, @PathVariable String startDate, @PathVariable String endDate) {
        List<SalesDto> sales;
        if ("SEMUA".equals(paymentMethod)) {
            sales = saleRepository.getSalesReportByPaymentMethodAll( startDate, endDate);
        } else {
             sales = saleRepository.getSalesReportByPaymentMethod(paymentMethod, startDate, endDate);
        }
        if (sales.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(sales);
    }
    @GetMapping("/cashflow-report/{startDate}/{endDate}")
    public ResponseEntity<?> getCashFlowReport(@PathVariable("startDate") String startDate,
                                               @PathVariable("endDate") String endDate) {
        try {
            Response response = reportService.getCashFlow(startDate, endDate);

            if (response.code().equals("0900")) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.create("01", "00", "Internal Server Error", null));
        }
    }

    @GetMapping("/cashflow/download")
    public ResponseEntity<byte[]> generateCashFlowReport(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) throws IOException {


        try {
            ByteArrayOutputStream baos = reportService.generateCashFlow(startDate, endDate);

            // Set HTTP headers and response entity
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Laporan Arus Kas.pdf");

            return new ResponseEntity<>(baos.toByteArray(), headers, HttpStatus.OK);

        } catch (IOException | DocumentException e) {
            // Handle IOException and DocumentException
            e.printStackTrace(); // Optional: Log the exception
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
