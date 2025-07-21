package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.Product;
import tokoibuelin.storesystem.model.Page;
import tokoibuelin.storesystem.model.response.StockDto;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class ProductRepository {
    private static final Logger log = LoggerFactory.getLogger(ProductRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Product> allProducts() {
        final String sql = "SELECT * FROM %s WHERE deleted_at IS NULL ORDER BY product_id".formatted(Product.TABLE_NAME);

        return jdbcTemplate.query(sql, new RowMapper<Product>() {
            @Override
            public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime restockDate = rs.getTimestamp("restock_date") == null ? null : rs.getTimestamp("restock_date").toInstant().atOffset(ZoneOffset.UTC);
                return new Product(
                        rs.getString("product_id"),
                        rs.getString("product_name"),
                        rs.getString("description"),
                        rs.getLong("unit"),
                        rs.getLong("price"),
                        rs.getLong("stock"),
                        rs.getString("supplier_id"),
                        rs.getString("product_image"),
                        rs.getInt("category_id"),
                        rs.getLong("purchase_price"),
                        rs.getString("created_by"),
                        rs.getString("updated_by"),
                        rs.getString("deleted_by"),
                        createdAt,
                        updatedAt,
                        deletedAt,
                        restockDate
                );
            }
        });
    }

    public Optional<Product> findById(String  id) {
        if (id == null ) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Product.TABLE_NAME + " WHERE product_id=?");
            ps.setString(1, id);
            return ps;
        }, rs -> {
            if (rs.next()) {
                final String productName = rs.getString("product_name");
                final String description = rs.getString("description");
                final Long unit = rs.getLong("unit");
                final Long price = rs.getLong("price");
                final Long stock = rs.getLong("stock");
                final String supplierId = rs.getString("supplier_id");
                final String productImage = rs.getString("product_image");
                final Integer categoryId = rs.getInt("category_id");
                final Long purchasePrice = rs.getLong("purchase_price");
                final String createdBy = rs.getString("created_by");
                final String updatedBy = rs.getString("updated_by");
                final String deletedBy = rs.getString("deleted_by");
                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime restockDate = rs.getTimestamp("restock_date") == null ? null : rs.getTimestamp("restock_date").toInstant().atOffset(ZoneOffset.UTC);
                return new Product(id, productName, description, unit, price, stock, supplierId, productImage, categoryId ,purchasePrice, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt,restockDate);
            }
            return null;
        }));
    }

    public Optional<Product> findProduct(String  find) {
        if (find == null ) {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Product.TABLE_NAME + " WHERE (LOWER(product_name) LIKE LOWER('%' || ? || '%') OR LOWER(product_id) LIKE LOWER('%' || ? || '%'))AND (deleted_at IS NULL OR deleted_by IS NULL)" );
            ps.setString(1, find);
            ps.setString(2, find);
            return ps;
        }, rs -> {
            if (rs.next()) {
                final String productId = rs.getString("product_id");
                final String productName = rs.getString("product_name");
                final String description = rs.getString("description");
                final Long unit = rs.getLong("unit");
                final Long price = rs.getLong("price");
                final Long stock = rs.getLong("stock");
                final String supplierId = rs.getString("supplier_id");
                final String productImage = rs.getString("product_image");
                final Integer categoryId = rs.getInt("category_id");
                final Long purchasePrice = rs.getLong("purchase_price");
                final String createdBy = rs.getString("created_by");
                final String updatedBy = rs.getString("updated_by");
                final String deletedBy = rs.getString("deleted_by");
                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
                final OffsetDateTime restockDate = rs.getTimestamp("restock_date") == null ? null : rs.getTimestamp("restock_date").toInstant().atOffset(ZoneOffset.UTC);
                return new Product(productId, productName, description, unit, price, stock, supplierId, productImage, categoryId, purchasePrice, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt, restockDate);
            }
            return null;
        }));
    }

    public List<Product> getAllProduct() {

        return jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM " + Product.TABLE_NAME + " WHERE  deleted_at IS NULL AND deleted_by IS NULL"
            );

            return ps;
        }, (rs, rowNum) -> {
            final String productId = rs.getString("product_id");
            final String productName = rs.getString("product_name");
            final String description = rs.getString("description");
            final Long unit = rs.getLong("unit");
            final Long price = rs.getLong("price");
            final Long stock = rs.getLong("stock");
            final String supplierId = rs.getString("supplier_id");
            final String productImage = rs.getString("product_image");
            final Integer categoryId = rs.getInt("category_id");
            final Long purchasePrice = rs.getLong("purchase_price");
            final String createdBy = rs.getString("created_by");
            final String updatedBy = rs.getString("updated_by");
            final String deletedBy = rs.getString("deleted_by");
            final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime updatedAt = rs.getTimestamp("updated_at") == null ? null : rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime deletedAt = rs.getTimestamp("deleted_at") == null ? null : rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC);
            final OffsetDateTime restockDate = rs.getTimestamp("restock_date") == null ? null : rs.getTimestamp("restock_date").toInstant().atOffset(ZoneOffset.UTC);
            return new Product(productId, productName, description, unit, price, stock, supplierId,productImage,categoryId, purchasePrice, createdBy, updatedBy, deletedBy, createdAt, updatedAt, deletedAt,restockDate);
        });
    }


    public String saveProduct(final Product product) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            int updateCount = jdbcTemplate.update(con -> {
                PreparedStatement ps = product.insert(con);
                return ps;
            }, keyHolder);

            if (updateCount != 1) {
                return null; // atau return 0L sesuai kebutuhan
            }

            // Ambil hasil dari KeyHolder sebagai String
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("product_id")) {
                return (String) keys.get("product_id");
            }

            return null;
        } catch (Exception e) {
            log.error("Error during saveProduct: {}", e.getMessage());
            return null; // atau return 0L sesuai kebutuhan
        }
    }

    public long deleteProduct(Product product) {
        try {
            String sql = "UPDATE " + Product.TABLE_NAME + " SET deleted_at=CURRENT_TIMESTAMP, deleted_by=? WHERE product_id=?";
            return jdbcTemplate.update(sql, product.deletedBy(), product.productId());
        } catch (Exception e) {
            log.error("Gagal untuk menghapus produk: {}", e.getMessage());
            return 0L;
        }
    }

    public boolean updateProduct(final Product product, final String userId) {
        final String sql = "UPDATE " + Product.TABLE_NAME + " SET product_name = ?,description = ?,unit=?,price=?,product_image=?,updated_by=?, updated_at = CURRENT_TIMESTAMP WHERE product_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, product.productName());
                ps.setString(2,product.description());
                ps.setLong(3,product.unit());
                ps.setLong(4,product.price());
                ps.setString(5, product.productImage());
                ps.setString(6,userId);
                ps.setString(7,product.productId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Gagal untuk update produk: {}", e.getMessage());
            return false;
        }
    }

    public boolean updateStockProduct(final Product product,Long newStock) {
        final String sql = "UPDATE " + Product.TABLE_NAME + " SET stock=?, restock_date= CURRENT_TIMESTAMP WHERE product_id = ? ";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setLong(1, newStock);
                ps.setString(2,product.productId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Gagal untuk update stock produk: {}", e.getMessage());
            return false;
        }
    }
    public List<StockDto> getStockProduct() {
        String sql = "SELECT product_id, product_name, stock FROM products WHERE (deleted_at IS NULL OR deleted_by IS NULL) ORDER BY stock ASC ";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            String productId = rs.getString("product_id");
            String productName = rs.getString("product_name");
            Integer stock = rs.getInt("stock");

            return new StockDto(productId,productName, stock);
        });
    }

    public List<Product> getLowStockProducts(int threshold) {
        String sql = "SELECT * FROM products WHERE stock <= ? AND (deleted_by IS NULL OR deleted_at IS NULL)";
        return jdbcTemplate.query(sql, new Object[]{threshold}, (rs, rowNum) -> new Product(
                rs.getString("product_id"),
                rs.getString("product_name"),
                rs.getString("description"),
                rs.getLong("unit"),
                rs.getLong("price"),
                rs.getLong("stock"),
                rs.getString("supplier_id"),
                rs.getString("product_image"),
                rs.getInt("category_id"),
                rs.getLong("purchase_price"),
                rs.getString("created_by"),
                rs.getString("updated_by"),
                rs.getString("deleted_by"),
                rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC),
                rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant().atOffset(ZoneOffset.UTC) : null,
                rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("deleted_at").toInstant().atOffset(ZoneOffset.UTC) : null,
                rs.getTimestamp("restock_date") != null ? rs.getTimestamp("restock_date").toInstant().atOffset(ZoneOffset.UTC) : null

        ));
    }

    public void insertNotification(String description, String category) {
        String sql = "INSERT INTO notification (description, category, created_at) VALUES (?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, description, category);
    }
}
