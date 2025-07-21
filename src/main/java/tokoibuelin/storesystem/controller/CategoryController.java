package tokoibuelin.storesystem.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tokoibuelin.storesystem.entity.Category;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.AddCategoryReq;
import tokoibuelin.storesystem.repository.CategoryRepository;
import tokoibuelin.storesystem.service.CategoryService;
import tokoibuelin.storesystem.util.SecurityContextHolder;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/secured/category")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class CategoryController {
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    CategoryService categoryService;

    @GetMapping("/all-category")
    public ResponseEntity<Response<Object>> getAllCategories() {
        Response<Object> response = categoryService.allCategories();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add-category")
    public Response<Object> addCategory(@RequestBody AddCategoryReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return categoryService.addCategory(authentication, req);
    }

    @PutMapping("/edit-category/{id}")
    public  Response<Object> editCategory(@PathVariable Integer id, @RequestParam String categoryUpdate) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        System.out.println("cek req : "+categoryUpdate);
        return categoryService.editCategory(authentication,categoryUpdate, id);
    }

    @DeleteMapping("/delete-category/{id}")
    public Response<Object> deleteCategory(@PathVariable Integer id){
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return categoryService.deleteCategory(authentication, id);
    }
    @GetMapping("/find-category/{find}")
    public Optional<Category> findProduct(@PathVariable Integer find){
        return categoryRepository.findById(find);
    }

}
