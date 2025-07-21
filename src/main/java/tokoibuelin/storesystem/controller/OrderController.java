package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.OnlineSaleReq;
import tokoibuelin.storesystem.model.response.OrderDto;
import tokoibuelin.storesystem.repository.OrderRepository;
import tokoibuelin.storesystem.service.OrderService;
import tokoibuelin.storesystem.util.SecurityContextHolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/secured/order")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/create-order")
    public Response<Object> createOnlineSale(@RequestBody OnlineSaleReq req){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return orderService.createOnlineSale(authentication,req);
    }

    @PostMapping("/konfirm-order/{id}")
    public Response<Object> confirmSale(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return orderService.approveOrder(authentication, id);
    }

    @GetMapping("/getByStatus/{status}")
    public List<OrderDto> getByStatus(@PathVariable String status,@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int limit){
        return orderRepository.getByStatus(status,page,limit);
    }

    @GetMapping("/getByOrderId/{orderId}")
    public Optional<OrderDto> getByOrderId(@PathVariable String orderId){
        return orderRepository.findWithDetailByOrderId(orderId);
    }

    

    @GetMapping("getAll")
    public List<OrderDto> getAll(){
        return orderRepository.getAllOrders();
    }


    @GetMapping("/generate-label/{orderId}")
    public ResponseEntity<byte[]> generateLabel(@PathVariable String orderId) {
        try {
            ByteArrayOutputStream outputStream = orderService.generateShippingLabel(orderId);

            // Tentukan direktori dan nama file
            String directoryPath = "src/main/resources/static"; // Ubah sesuai kebutuhan
            File directory = new File(directoryPath);

            // Buat direktori jika belum ada
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new IOException("Failed to create directory: " + directoryPath);
                }
            }

            // Buat file dengan nama di dalam direktori yang ditentukan
            File file = new File(directory, "shipping-label-"+orderId+".pdf");

            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                outputStream.writeTo(fileOutputStream);
            }

            // Menampilkan lokasi file di log
            System.out.println("File saved at: " + file.getAbsolutePath());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shipping-label.pdf");
            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Failed to generate shipping label: " + e.getMessage()).getBytes());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage().getBytes());
        }
    }


}
