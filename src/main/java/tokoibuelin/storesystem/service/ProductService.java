package tokoibuelin.storesystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.request.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.allProductDto;
import tokoibuelin.storesystem.repository.ProductRepository;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
@Service
public class ProductService extends AbstractService{
    @Autowired
    ProductRepository productRepository;

    @Autowired
    UploadImgService uploadImgService;
    

    public Response<Object> allProducts() {
//        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK,User.Role.PELANGGAN).orElseGet(() -> {

            List<Product> products = productRepository.allProducts();
            List<allProductDto> productDtos = products.stream()
                    .map(product -> new allProductDto( product.productId(),product.productName(),product.description(), product.unit(), product.price(), product.stock(), product.supplierId(),product.productImage(),product.purchasePrice()))
                    .toList();
            return Response.create("09", "00", "Sukses", productDtos);
//        });
    }

    public Response<Object> getAllProducts() {
            List<Product> products = productRepository.allProducts();
            List<allProductDto> productDtos = products.stream()
                    .map(product -> new allProductDto( product.productId(),product.productName(),product.description(), product.unit(), product.price(), product.stock(), product.supplierId(),product.productImage(),product.purchasePrice()))
                    .toList();

            return Response.create("09", "00", "Sukses", productDtos);
    }


    public Response<Object> createProduct(final Authentication authentication, final RegistProductReq req, final MultipartFile file ) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            if (req == null) {
                return Response.badRequest();
            }
            String urlImg = null;
            try {
                urlImg = uploadImgService.uploadFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                return Response.create("05", "02", "Gagal upload gambar", null);
            }

            String supplierId = req.getSupplierId();
            String supplierIdToSave = (supplierId != null && !supplierId.isEmpty()) ? supplierId : null;

            final Product product = new Product( //
                    null, //
                    req.getProductName(),//
                    req.getDescription(), //
                    req.getUnit(),
                    req.getPrice(), //
                    req.getStock(),//
                    supplierIdToSave,
                    urlImg,
                    req.getCategoryId(),
                    req.getPurchasePrice(),
                    authentication.id(),
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null,
                    null
            );
            final String saved = productRepository.saveProduct(product);
            if (null == saved) {
                return Response.create("05", "01", "Gagal menambahkan Product", null);
            }
            return Response.create("05", "00", "Sukses", saved);
        });
    }

    public Response<Object> deleteProduct(Authentication authentication, String productId) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Product> productOpt = productRepository.findById(productId);

            if (!productOpt.isPresent()) {
                return Response.create("10", "02", "ID tidak ditemukan", null);
            }

            Product product = productOpt.get();

            if (product.deletedAt() != null) {
                return Response.create("10", "03", "Data sudah dihapus", null);
            }

            Product deletedProduct = new Product(
                    product.productId(),
                    product.productName(),
                    product.description(),
                    product.unit(),
                    product.price(),
                    product.stock(),
                    product.supplierId(),
                    product.productImage(),
                    product.categoryId(),
                    product.purchasePrice(),
                    product.createdBy(),
                    product.updatedBy(),
                    authentication.id(),
                    product.createdAt(),
                    product.updatedAt(),
                    OffsetDateTime.now(),
                    product.restockDate());

            Long updatedRows = productRepository.deleteProduct(deletedProduct);
            if (updatedRows > 0) {
                return Response.create("10", "00", "Berhasil hapus data", null);
            } else {
                return Response.create("10", "01", "Gagal hapus data", null);
            }
        });
    }

    public Response<Object> updateProduct(final Authentication authentication, final UpdateProductReq req, final MultipartFile file) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Product> productOpt = productRepository.findById(req.getProductId());
            if (productOpt.isEmpty()) {
                return Response.create("07", "01", "Produk  tidak ditemukan", null);
            }
            String urlImg = null;
            try {
                urlImg = uploadImgService.uploadFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                return Response.create("05", "02", "Gagal upload gambar", null);
            }

            Product product = productOpt.get();
            Product updatedProduct = new Product(
                    product.productId(),
                    req.getProductName(),
                    product.description(),
                    req.getUnit(),
                    req.getPrice(),
                    product.stock(),
                    product.supplierId(),
                    urlImg,
                    product.categoryId(),
                    product.purchasePrice(),
                    product.createdBy(),
                    authentication.id(),
                    product.deletedBy(),
                    product.createdAt(),
                    OffsetDateTime.now(),
                    product.deletedAt(),
                    product.restockDate());

            if (productRepository.updateProduct(updatedProduct,authentication.id())) {
                return Response.create("07", "00", "Data produk berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate data produk", null);
            }
        });
    }

    public Response<Object> addStockProduct(final Authentication authentication, final Long addStock, final String produkId) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Product> productOpt = productRepository.findById(produkId);
            if (productOpt.isEmpty()) {
                return Response.create("07", "01", "Produk  tidak ditemukan", null);
            }

            Product product = productOpt.get();
            Long newStock = product.stock() +addStock;
            Product updatedProduct = new Product(
                product.productId(),
                product.productName(),
                product.description(),
                product.unit(),
                product.price(),
                newStock,
                product.supplierId(),
                product.productImage(),
                product.categoryId(),
                product.purchasePrice(),
                product.createdBy(),
                authentication.id(),
                product.deletedBy(),
                product.createdAt(),
                OffsetDateTime.now(),
                product.deletedAt(),
                OffsetDateTime.now()

            );

            if (productRepository.updateStockProduct(updatedProduct,newStock)) {
                return Response.create("07", "00", "Data produk berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate data produk", null);
            }
        });
    }

}
