package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.entity.Notification;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.repository.NotificationRepository;
import tokoibuelin.storesystem.repository.ProductRepository;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/secured/notification")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class NotificationController {
    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    ProductRepository productRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllNotifications() {
        try {
            List<Notification> notifications = notificationRepository.getAll();
            if (!notifications.isEmpty()) {
                return ResponseEntity.ok(notifications);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No notifications found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving notifications");
        }
    }

    // @GetMapping("/get-unread")
    // public ResponseEntity<?> getUnReadNotifications() {
    //     try {
    //         List<Notification> notifications = notificationRepository.getAll();
    //         if (!notifications.isEmpty()) {
    //             return ResponseEntity.ok(notifications);
    //         } else {
    //             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No notifications found");
    //         }
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving notifications");
    //     }
    // }
    



    @GetMapping("/count")
    public ResponseEntity<?> countNotifications() {
        try {
            Response response = notificationRepository.countNotifications();

                return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while counting notifications");
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000) // Run every 30 minutes
    public void checkStockLevels() {
        List<Product> lowStockProducts = productRepository.getLowStockProducts(5);
        // System.out.println("Low stock products: " + lowStockProducts); // Log the results

        for (Product product : lowStockProducts) {
            String description = String.format("Stok produk '%s' mencapai batas minimum: %d pcs", product.productName(), product.stock());

            // Cek apakah notifikasi sudah ada
            boolean notificationExists = notificationRepository.notificationExists(description);

            if (!notificationExists) {
                // Log data sebelum insert
                System.out.println("Inserting notification: " + description);
                productRepository.insertNotification(description, "Stok Minimum");
            } else {
                System.out.println("Notification already exists for: " + description);
            }
        }
    }



    @DeleteMapping("/delete/{id}")
    public Long deleteNotification(@PathVariable("id") Integer id) {
        return notificationRepository.deleteNotification(id);
    }
}
