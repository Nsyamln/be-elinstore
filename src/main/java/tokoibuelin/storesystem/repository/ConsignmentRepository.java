package tokoibuelin.storesystem.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tokoibuelin.storesystem.entity.Consignment;
import tokoibuelin.storesystem.model.response.ConsignmentDto;
import tokoibuelin.storesystem.model.response.StockProductDto;

@Repository
public class ConsignmentRepository {
    private static final Logger log = LoggerFactory.getLogger(ConsignmentRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public ConsignmentRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;

    }

    public Map<String, Object> getPendingPaymentsSummaryBySupplier( final String supplierId) {
         final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");    
        String query = "SELECT c.*, p.product_name FROM consignments c JOIN products p ON c.product_id = p.product_id WHERE c.status = 'UNPAID' AND c.supplier_id = ?";
            
            List<ConsignmentDto> pendingPayments = jdbcTemplate.query(query, new Object[]{supplierId},
            (rs, rowNum) -> {
                final OffsetDateTime paymentDateTimeFromDb = rs.getTimestamp("payment_date") == null ? null
                                                           : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                String formattedPaymentDate = null;
                if (paymentDateTimeFromDb != null) {
                    formattedPaymentDate = paymentDateTimeFromDb.format(formatter);
                }

                return new ConsignmentDto(
                    rs.getString("consignment_id"),
                    rs.getString("transaction_id"),
                    rs.getString("product_id"),
                    rs.getString("supplier_id"),
                    rs.getInt("product_quantity"),
                    rs.getBigDecimal("total_purchase_price"),
                    rs.getString("status"),
                    formattedPaymentDate, 
                    rs.getString("product_name")
                );
            });

        BigDecimal totalAmountToPay = pendingPayments.stream()
            .map(ConsignmentDto::totalPurchasePrice) 
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmountToPay", totalAmountToPay);
        result.put("details", pendingPayments); 

        return result;

    }


    public Map<String, Object> getPaidPaymentsSummaryBySupplier( final String supplierId) {
         final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");    
        String query = "SELECT c.*, p.product_name FROM consignments c JOIN products p ON c.product_id = p.product_id WHERE c.status = 'PAID' AND c.supplier_id = ?";
            
            List<ConsignmentDto> pendingPayments = jdbcTemplate.query(query, new Object[]{supplierId},
            (rs, rowNum) -> {
                final OffsetDateTime paymentDateTimeFromDb = rs.getTimestamp("payment_date") == null ? null
                                                           : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                String formattedPaymentDate = null;
                if (paymentDateTimeFromDb != null) {
                    formattedPaymentDate = paymentDateTimeFromDb.format(formatter);
                }

                return new ConsignmentDto(
                    rs.getString("consignment_id"),
                    rs.getString("transaction_id"),
                    rs.getString("product_id"),
                    rs.getString("supplier_id"),
                    rs.getInt("product_quantity"),
                    rs.getBigDecimal("total_purchase_price"),
                    rs.getString("status"),
                    formattedPaymentDate, 
                    rs.getString("product_name")
                );
            });

        BigDecimal totalAmountToPay = pendingPayments.stream()
            .map(ConsignmentDto::totalPurchasePrice) 
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAmountToPay", totalAmountToPay);
        result.put("details", pendingPayments); 

        return result;

    }

    public List<ConsignmentDto> getAll(String suppId){
        Map<String, ConsignmentDto> profitMap = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
        jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT c.*, p.product_name FROM consignments c JOIN products p ON c.product_id = p.product_id WHERE c.status = 'UNPAID' AND c.supplier_id = ?"
            );
            ps.setString(1, suppId);
            return ps;
        }, (rs, rowNum) -> {
            final String sharingIdFromDb = rs.getString("consignment_id");
            ConsignmentDto profitDto = profitMap.get(sharingIdFromDb);
            if (profitDto == null) {
                final String transactionId = rs.getString("transaction_id");
                final String productId = rs.getString("product_id");
                final String supplierId = rs.getString("supplier_id");
                final int productQuantity = rs.getInt("product_quantity");
                final BigDecimal totalPurchasePrice = rs.getBigDecimal("total_purchase_price");
                final String status = rs.getString("status");
                final OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                        : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                
                String formattedPaymentDate = null;
                if (paymentDate != null) {
                    formattedPaymentDate = paymentDate.format(formatter);
                }

                final String productName = rs.getString("product_name");
                profitDto = new ConsignmentDto(sharingIdFromDb,transactionId, productId, supplierId, productQuantity, totalPurchasePrice,status, formattedPaymentDate,productName);
                profitMap.put(sharingIdFromDb, profitDto);
            }

            return null;
        });

        return new ArrayList<>(profitMap.values());
    }

    @Transactional
    public void updatePaymentStatus(String supplierId) {
        String selectQuery = "SELECT * FROM consignments WHERE status = 'UNPAID' AND supplier_id = ?";
        String updateQuery = "UPDATE consignments SET status = 'PAID', payment_date = ? WHERE consignment_id = ?";

        List<Consignment> paymentsToUpdate = jdbcTemplate.query(selectQuery, new Object[]{supplierId},
                (rs, rowNum) -> new Consignment(
                        rs.getString("consignment_id"),
                        rs.getString("transaction_id"),
                        rs.getString("product_id"),
                        rs.getString("supplier_id"),
                        rs.getInt("product_quantity"),
                        rs.getLong("total_purchase_price"),
                        rs.getString("status"),
                        rs.getObject("payment_date", OffsetDateTime.class)
                ));

        for (Consignment payment : paymentsToUpdate) {
            jdbcTemplate.update(updateQuery, LocalDateTime.now(), payment.consignmentId());
        }
    }

    public List<ConsignmentDto> getAllPaymentsSummaryBySupplier(String suppId){
        Map<String, ConsignmentDto> profitMap = new LinkedHashMap<>();

        jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "SELECT ps.*, p.product_name " +
                            "FROM consignments ps " +
                            "JOIN products p ON ps.product_id = p.product_id WHERE ps.supplier_id = ?"
            );
            ps.setString(1, suppId);
            return ps;
        }, (rs, rowNum) -> {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                                .withZone(ZoneId.systemDefault());

            final String consignmentId = rs.getString("consignment_id");
            ConsignmentDto consignmentDto = profitMap.get(consignmentId);

            if (consignmentDto == null) {
                final String transactionId = rs.getString("transaction_id");
                final String productId = rs.getString("product_id");
                final String supplierId = rs.getString("supplier_id");
                final int productQuantity = rs.getInt("product_quantity");
                final BigDecimal totalPurchasePrice = rs.getBigDecimal("total_purchase_price");
                final String status = rs.getString("status");

                // Baris ini adalah sumber masalah
                final OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                                                : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                String formattedDate;
                // Tambahkan pengecekan null di sini
                if (paymentDate != null) {
                    formattedDate = formatter.format(paymentDate);
                } else {
                    formattedDate = null; // Atau "" (string kosong), atau "N/A" (Not Available)
                                        // Sesuaikan dengan bagaimana Anda ingin menampilkan tanggal yang null di UI
                }

                final String productName = rs.getString("product_name");

                consignmentDto = new ConsignmentDto(consignmentId, transactionId, productId, supplierId, productQuantity, totalPurchasePrice, status, formattedDate, productName);
                profitMap.put(consignmentId, consignmentDto);
            }

            return null;
        });

        return new ArrayList<>(profitMap.values());
    }

    public List<StockProductDto> getStockProductBySupplierId(String supplierId) {
        String sql = "SELECT product_id, product_name, stock, price, restock_date FROM products WHERE supplier_id = ? AND (deleted_at IS NULL OR deleted_by IS NULL)";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
        return jdbcTemplate.query(sql, new Object[]{supplierId}, (rs, rowNum) -> {
            String productId = rs.getString("product_id");
            String productName = rs.getString("product_name");
            Integer stock = rs.getInt("stock");
            Long price = rs.getLong("price");
            final OffsetDateTime restockDate = rs.getTimestamp("restock_date") == null ? null
                        : rs.getTimestamp("restock_date").toInstant().atOffset(ZoneOffset.UTC);
               
            
            return new StockProductDto(productId,productName, stock, price,restockDate);
        });
    }

    public List<ConsignmentDto> getProfitSharing() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy/HH:mm:ss")
                    .withZone(ZoneId.systemDefault());
        return jdbcTemplate.query(
            "SELECT ps.*, p.product_name FROM consignments ps JOIN products p ON ps.product_id = p.product_id",
            (rs, rowNum) -> {
                OffsetDateTime paymentDate = rs.getTimestamp("payment_date") == null ? null
                    : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                String formattedPaymentDate = null;
                if (paymentDate != null) {
                    formattedPaymentDate = paymentDate.format(formatter);
                }

                return new ConsignmentDto(
                    rs.getString("consignments_id"),
                    rs.getString("transaction_id"),
                    rs.getString("product_id"),
                    rs.getString("supplier_id"),
                    rs.getInt("product_quantity"),
                    rs.getBigDecimal("total_purchase_price"),
                    rs.getString("status"),
                    formattedPaymentDate,
                    rs.getString("product_name")
                );
            }
        );
    }

    public List<StockProductDto> getStockProducts() {
        return jdbcTemplate.query(
            "SELECT product_name, stock FROM products WHERE (deleted_at IS NULL OR deleted_by IS NULL)",
            
            (rs, rowNum) -> {
                OffsetDateTime restockDate = rs.getTimestamp("payment_date") == null ? null
                    : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                return new StockProductDto(
                
                
                rs.getString("product_id"),
                rs.getString("product_name"),
                rs.getInt("stock"),
                rs.getLong("price"),
                restockDate
                );

            }
        );
    }


    public long saveConsignment(List<Consignment> consignment) {
       String sqlDetail = "INSERT INTO consignments (transaction_id, product_id, supplier_id, product_quantity, total_purchase_price, status) VALUES ( ?, ?, ?, ?, ?, ?)";
        try {
            for (Consignment consign : consignment) {
                jdbcTemplate.update(sqlDetail, consign.transactionId(), consign.productId(), consign.supplierId(), consign.productQuantity(), consign.totalPrice(), consign.status());
            }
            return 1L;
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public Long sumConsignment(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));

        try {
            String sql = "SELECT SUM(total_purchase_price) AS total FROM consignments WHERE payment_date BETWEEN ? AND ? ";
            Long total = jdbcTemplate.queryForObject(sql,new Object[]{startTimestamp, endTimestamp},  Long.class);
            System.out.println("total Sharing : " + total);
            return total != null ? total : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

}
