package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.RegisterReq;
import tokoibuelin.storesystem.model.response.OrderDto;
import tokoibuelin.storesystem.repository.OrderRepository;
import tokoibuelin.storesystem.repository.ProductRepository;
import tokoibuelin.storesystem.service.ProductService;
import tokoibuelin.storesystem.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class CustomerController {
    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserService userService;

    @GetMapping("/product/all-product")
    public List<Product> allProducts() {
        return productRepository.getAllProduct();
    }

     @PostMapping("/register-customer")
    public Response<Object> registerBuyer(@RequestBody RegisterReq req) {
        return userService.registerBuyer( req);
    }

    @GetMapping("/product/findbyId/{productId}")
    public Optional<Product> findProductbyId(@PathVariable String productId){
        return productRepository.findById(productId);
    }

    @GetMapping("/order/findbyId/{customerId}")
    public List<OrderDto> findTransactionbyId(@PathVariable String customerId){
        return orderRepository.findByCustomerId(customerId);
    }
}
