package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.response.SalesDto;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.OfflineSaleReq;
import tokoibuelin.storesystem.repository.SaleRepository;
import tokoibuelin.storesystem.service.SaleService;
import tokoibuelin.storesystem.util.SecurityContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:5173", "http://127.0.0.1:3000"})
@RequestMapping("/secured/sales")
public class SaleController {
    private final SaleService saleService;


    @Autowired
    private SaleRepository saleRepository;

    public SaleController(final SaleService saleService){
        this.saleService = saleService;
    }
    
    @GetMapping("/getAll")
    public List<SalesDto> getAllSale() {
        return saleRepository.getSalesWithDetails();
    }

    @GetMapping("/getById/{saleId}")
    public List<SalesDto> getAllSaleById(@PathVariable String saleId) {
        return saleRepository.getSalesById(saleId);
    }
    
    @PostMapping("/create-saleOff")
    public Response<Object> createSale(@RequestBody OfflineSaleReq req){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return saleService.createOfflineSale(authentication,req);
    }
    
    
    @GetMapping("/download-receipt/{saleId}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable("saleId") String saleId) {
        try {
            // Generate the receipt as a PDF
            ByteArrayOutputStream baos = saleService.generateReceipt(saleId);
            byte[] pdfBytes = baos.toByteArray();

            // Set up HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + saleId + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdfBytes.length));

            // Return PDF as response entity
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            // Handle the exception and return an appropriate response
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/view-receipt/{saleId}")
    public ResponseEntity<byte[]> viewReceipt(@PathVariable("saleId") String saleId) {
        try {
            ByteArrayOutputStream baos = saleService.generateReceipt(saleId);
            byte[] pdfBytes = baos.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=receipt-" + saleId + ".pdf");
            headers.setContentType(MediaType.APPLICATION_PDF);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/sales")
    public Response<Object> getSales(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        List<SalesDto> sales = saleRepository.getSalesWithDetails();
        return Response.create("05", "00", "Success", sales);
    }

    @GetMapping("/sales/detail")
    public Response<Object> getSalesDetail(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status) {
        List<SalesDto> sales = saleRepository.getSalesWithDetails();
        return Response.create("05", "00", "Success", sales);
    }
}
