package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.*;
import tokoibuelin.storesystem.model.response.OrderDto;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderRepository {
    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public String getLatestOrderId() {
        String sql = "SELECT MAX(order_id) FROM orders";
        return jdbcTemplate.queryForObject(sql, String.class);
    }
    public String generateNewOrderId() {
        String latestOrderId = getLatestOrderId();
        if (latestOrderId == null) {
            return "OR001";
        }

        int latestNumber = Integer.parseInt(latestOrderId.substring(2));
        int newNumber = latestNumber + 1;
        return String.format("OR%03d", newNumber);
    }
    public String saveOrder(final Order order) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            int updateCount = jdbcTemplate.update(con -> {
                PreparedStatement ps = order.insert(con);
                System.out.println("Cek PS Order : "+ps.toString());
                return ps;
            }, keyHolder);

            if (updateCount != 1) {
                return null;
            }

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("order_id")) {
                return (String) keys.get("order_id");
            }

            return null;
        } catch (Exception e) {
            log.error("Error during saveOrder: {}", e.getMessage());
            return null; 
        }
    }
        
    public long saveOrderDetails(List<OrderDetails> orderDetails) {
        String sqlDetail = "INSERT INTO order_details (order_id, product_id, product_name, quantity, price, unit) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            for (OrderDetails detail : orderDetails) {
                jdbcTemplate.update(sqlDetail, detail.orderId(), detail.productId(), detail.productName(), detail.quantity(), detail.price(), detail.unit());
            }
            return 1L;
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    public Optional<Order> findById(String  id) {
        if (id == null || id == "") {
            return Optional.empty();
        }
        return Optional.ofNullable(jdbcTemplate.query(con -> {
            final PreparedStatement ps = con.prepareStatement("SELECT * FROM " + Order.TABLE_NAME + " WHERE order_id=?");
            ps.setString(1, id);
            return ps;
        }, rs -> {
            if(rs.next()) {
                final OffsetDateTime orderDate = rs.getTimestamp("order_date") == null ? null : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                final String customerId = rs.getString("customer_id");
                final String deliveryAddress = rs.getString("delivery_address");
                final String phone = rs.getString("phone");
                final Order.Status status = Order.Status.valueOf(rs.getString("status"));
                final String createdBy = rs.getString("created_by");
                final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                final Integer shippingChost = rs.getInt("shipping_cost");
                final String courier = rs.getString("courier");
                final String shippingMethod  =rs.getString("shipping_method");
                final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                final String paymentMethod = rs.getString("payment_method");
                final OffsetDateTime paymentTime = rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);
                return new Order(id, orderDate, customerId, deliveryAddress,phone, status, createdBy, createdAt,shippingChost,courier,shippingMethod,estimatedDeliveryDate,paymentMethod,paymentTime);
            }
            return null;
        }));
    }

    public List<OrderDto> getByStatus(String status,int page,int limit) {
        if (status == null || status.isEmpty()) {
            return Collections.emptyList(); 
        }

        int offset = (page - 1) * limit;

        String sql = "select o.*, od.*, u.name from orders o join users u on o.customer_id = u.user_id left join order_details od on o.order_id = od.order_id where o.status = ? LIMIT ? OFFSET ?";

        return jdbcTemplate.query(sql, new Object[]{status,limit,offset}, (ResultSet rs) -> {
            Map<String, OrderDto> orderMap = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


            while (rs.next()) {
                final String orderId = rs.getString("order_id");
                OrderDto orderDto = orderMap.get(orderId);

                // Jika OrderDto belum ada di map, buat yang baru
                if (orderDto == null) {
                    final OffsetDateTime orderDateTime = rs.getTimestamp("order_date") == null? null : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                    String orderDate = (orderDateTime != null) ? formatter.format(orderDateTime) : null;
                    final String customerId = rs.getString("customer_id");
                    final String customerName = rs.getString("name");
                    final String customerEmail = rs.getString("email");
                    final String deliveryAddress = rs.getString("delivery_address");
                    final String phone = rs.getString("phone");
                    final Order.Status statusEnum = Order.Status.valueOf(rs.getString("status"));
                    final String createdBy = rs.getString("created_by");
                    final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                    final Integer shippingCost = rs.getInt("shipping_cost");
                    final String courier = rs.getString("courier");
                    final String shippingMethod = rs.getString("shipping_method");
                    final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                    final String paymentMethod = rs.getString("payment_method");
                    final OffsetDateTime paymentTime = rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                    orderDto = new OrderDto(orderId, orderDate, customerId, customerName, customerEmail, deliveryAddress, phone, statusEnum,
                            createdBy, createdAt, shippingCost,courier, shippingMethod, estimatedDeliveryDate,paymentMethod,paymentTime,new ArrayList<>()); 
                    orderMap.put(orderId, orderDto);
                }

                // Tambahkan detail pesanan jika ada (yaitu jika detailId tidak null)
                final String detailId = rs.getString("detail_order_id");
                if (detailId != null) { // Pastikan ada detail sebelum membuat objek OrderDetails
                    final String productId = rs.getString("product_id");
                    final String productName = rs.getString("product_name");
                    final Integer quantity = rs.getInt("quantity");
                    final BigDecimal price = rs.getBigDecimal("price");
                    final Integer unit = rs.getInt("unit");

                    OrderDetails orderDetails = new OrderDetails(detailId, orderId, productId, productName, quantity, price, unit);
                    orderDto.orderDetails().add(orderDetails); // Tambahkan ke daftar detail yang sudah ada
                }
            }
            return new ArrayList<>(orderMap.values()); // Kembalikan daftar semua OrderDto
        });
    }

    public List<OrderDto> getAllOrders() {
        final String sql = "SELECT o.*, od.*, u.name, u.email " +
                           "FROM orders o " +
                           "JOIN users u ON o.customer_id = u.user_id " +
                           "LEFT JOIN order_details od ON o.order_id = od.order_id " +
                           "ORDER BY o.order_id, od.detail_order_id"; 

        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<String, OrderDto> orderMap = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                                         .withZone(ZoneId.systemDefault());


            while (rs.next()) {
                final String orderId = rs.getString("order_id");
                OrderDto orderDto = orderMap.get(orderId);

                // Jika OrderDto belum ada di map, buat yang baru
                if (orderDto == null) {
                    final OffsetDateTime orderDateTime = rs.getTimestamp("order_date") == null? null : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                    String orderDate = (orderDateTime != null) ? formatter.format(orderDateTime) : null;
                    System.out.println("ck tanggal "+orderDate);
                    

                    final String customerId = rs.getString("customer_id");
                    final String customerName = rs.getString("name");
                    final String customerEmail = rs.getString("email");
                    final String deliveryAddress = rs.getString("delivery_address");
                    final String phone = rs.getString("phone");
                    final Order.Status statusEnum = Order.Status.valueOf(rs.getString("status"));
                    final String createdBy = rs.getString("created_by");
                    final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                    final Integer shippingCost = rs.getInt("shipping_cost");
                    final String courier = rs.getString("courier");
                    final String shippingMethod = rs.getString("shipping_method");
                    final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                    final String paymentMethod = rs.getString("payment_method");
                    final OffsetDateTime paymentTime = rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                    orderDto = new OrderDto(orderId, orderDate, customerId, customerName,customerEmail, deliveryAddress, phone, statusEnum,
                            createdBy,  createdAt, shippingCost, courier, shippingMethod, estimatedDeliveryDate,paymentMethod,paymentTime,
                            new ArrayList<>()); // Inisialisasi daftar detail kosong
                    orderMap.put(orderId, orderDto);
                }

                // Tambahkan detail pesanan jika ada
                final String detailId = rs.getString("detail_order_id");
                if (detailId != null) {
                    final String productId = rs.getString("product_id");
                    final String productName = rs.getString("product_name");
                    final Integer quantity = rs.getInt("quantity");
                    final BigDecimal price = rs.getBigDecimal("price");
                    final Integer unit = rs.getInt("unit");

                    OrderDetails orderDetails = new OrderDetails(detailId, orderId, productId, productName, quantity, price, unit);
                    orderDto.orderDetails().add(orderDetails); // Tambahkan ke daftar detail yang sudah ada
                }
            }
            return new ArrayList<>(orderMap.values()); // Kembalikan daftar semua OrderDto
        });
    }
    

    public List<OrderDto> findByCustomerId(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return Collections.emptyList(); // Mengembalikan daftar kosong jika customerId tidak valid
        }

        return jdbcTemplate.query(
            (java.sql.Connection con) -> {
                // Query SQL Anda sudah benar. ORDER BY o.order_id penting untuk pengelompokan di Java.
                final PreparedStatement ps = con.prepareStatement(
                    "select o.*, od.*, u.name, u.email " +
                    "from orders o " +
                    "join users u on o.customer_id = u.user_id " +
                    "left join order_details od on o.order_id = od.order_id " +
                    "WHERE o.customer_id = ? ORDER BY o.order_id, od.detail_order_id" // Tambahkan ORDER BY detail_order_id juga
                );
                ps.setString(1, customerId);
                return ps;
            },
            (java.sql.ResultSet rs) -> {
                // Menggunakan LinkedHashMap untuk menjaga urutan pesanan saat mereka ditemukan
                Map<String, OrderDto> orderMap = new LinkedHashMap<>();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                while (rs.next()) {
                    final String currentOrderId = rs.getString("order_id");

                    // Cek apakah OrderDto untuk orderId ini sudah dibuat
                    OrderDto orderDto = orderMap.get(currentOrderId);

                    // Jika belum ada, buat OrderDto baru
                    if (orderDto == null) {
                        final OffsetDateTime formattedSaleDate = rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                        final OffsetDateTime orderDateTime = rs.getTimestamp("order_date") == null? null : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                        String orderDate = (orderDateTime != null) ? formatter.format(orderDateTime) : null;
                        final String customerName = rs.getString("name");
                        final String customerEmail = rs.getString("email");
                        final String deliveryAddress = rs.getString("delivery_address");
                        final String phone = rs.getString("phone");
                        final Order.Status statusEnum = Order.Status.valueOf(rs.getString("status"));
                        final String createdBy = rs.getString("created_by");
                        final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                        final Integer shippingCost = rs.getInt("shipping_cost");
                        final String courier = rs.getString("courier");
                        final String shippingMethod = rs.getString("shipping_method");
                        final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                        final String paymentMethod = rs.getString("payment_method");
                        final OffsetDateTime paymentTime = rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);


                        // Buat OrderDto baru dengan daftar detail yang kosong
                        orderDto = new OrderDto(currentOrderId, orderDate, customerId, customerName,customerEmail, deliveryAddress, phone, statusEnum,
                                                createdBy, createdAt, shippingCost, courier, shippingMethod, estimatedDeliveryDate,paymentMethod,paymentTime,
                                                new ArrayList<>()); // Inisialisasi list detail kosong di sini
                        orderMap.put(currentOrderId, orderDto); // Simpan di map
                    }

                    // Tambahkan detail pesanan ke OrderDto yang sesuai
                    final String detailId = rs.getString("detail_order_id");
                    if (detailId != null) { // Pastikan ada detail pesanan (tidak NULL dari LEFT JOIN)
                        final String productId = rs.getString("product_id");
                        final String productName = rs.getString("product_name");
                        final Integer quantity = rs.getInt("quantity");
                        final BigDecimal price = rs.getBigDecimal("price");
                        final Integer unit = rs.getInt("unit");

                        OrderDetails orderDetails = new OrderDetails(detailId, currentOrderId, productId, productName, quantity, price, unit);
                        // Tambahkan detail ini ke daftar detail dari OrderDto yang sedang aktif
                        orderDto.orderDetails().add(orderDetails);
                    }
                }
                // Kembalikan semua OrderDto yang terkumpul dalam bentuk List
                return new ArrayList<>(orderMap.values());
            }
        );
    }

    public Optional<OrderDto> findWithDetailByOrderId(String id) {
        if (id == null || id.isEmpty()) { 
            return Optional.empty();
        }

        return Optional.ofNullable(jdbcTemplate.query(
        (java.sql.Connection con) -> { 
            final PreparedStatement ps = con.prepareStatement(
                "select o.*, od.*, u.name, u.email " +
                "from orders o " +
                "join users u on o.customer_id = u.user_id " +
                "left join order_details od on o.order_id = od.order_id " +
                "WHERE o.order_id = ?"
            );
            ps.setString(1, id);
            return ps;
        },
        (java.sql.ResultSet rs) -> { 
            OrderDto orderDto = null;
            List<OrderDetails> orderDetailsList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                if (orderDto == null) {
                    final OffsetDateTime orderDateTime = rs.getTimestamp("order_date") == null? null : rs.getTimestamp("order_date").toInstant().atOffset(ZoneOffset.UTC);
                    String orderDate = (orderDateTime != null) ? formatter.format(orderDateTime) : null;
                    final String customerId = rs.getString("customer_id");
                    final String customerName = rs.getString("name");
                    final String customerEmail = rs.getString("email");
                    final String deliveryAddress = rs.getString("delivery_address");
                    final String phone = rs.getString("phone");
                    final Order.Status statusEnum = Order.Status.valueOf(rs.getString("status")); // Pastikan ini benar
                    final String createdBy = rs.getString("created_by");
                    final OffsetDateTime createdAt = rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toInstant().atOffset(ZoneOffset.UTC);
                    final Integer shippingCost = rs.getInt("shipping_cost");
                    final String courier = rs.getString("courier");
                    final String shippingMethod = rs.getString("shipping_method");
                    final OffsetDateTime estimatedDeliveryDate = rs.getTimestamp("estimated_delivery_date") == null ? null : rs.getTimestamp("estimated_delivery_date").toInstant().atOffset(ZoneOffset.UTC);
                    final String paymentMethod = rs.getString("payment_method");
                    final OffsetDateTime paymentTime = rs.getTimestamp("payment_date") == null ? null : rs.getTimestamp("payment_date").toInstant().atOffset(ZoneOffset.UTC);

                    orderDto = new OrderDto(id, orderDate, customerId, customerName,customerEmail, deliveryAddress, phone, statusEnum, // Gunakan statusEnum
                                            createdBy, createdAt, shippingCost, courier, shippingMethod, estimatedDeliveryDate,paymentMethod,paymentTime,
                                            null); 
                }

                final String detailId = rs.getString("detail_order_id");
                if (detailId != null) {
                    final String productId = rs.getString("product_id");
                    final String productName = rs.getString("product_name");
                    final Integer quantity = rs.getInt("quantity");
                    final BigDecimal price = rs.getBigDecimal("price");
                    final Integer unit = rs.getInt("unit");

                    OrderDetails orderDetails = new OrderDetails(detailId, id, productId, productName, quantity, price, unit);
                    orderDetailsList.add(orderDetails);
                }
            }

            if (orderDto != null) {
                return new OrderDto(
                    orderDto.orderId(),
                    orderDto.orderDate(),
                    orderDto.customerId(),
                    orderDto.customerName(),
                    orderDto.customerEmail(),
                    orderDto.deliveryAddress(),
                    orderDto.phone(),
                    orderDto.status(),
                    orderDto.createdBy(),
                    orderDto.createdAt(),
                    orderDto.shippingCost(),
                    orderDto.courier(),
                    orderDto.shippingMethod(),
                    orderDto.estimatedDeliveryDate(),
                    orderDto.paymentMethod(),
                    orderDto.paymentTime(),
                    orderDetailsList 
                );
            }
            return null;
        }
    ));
    }

    public Long sumSales(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));
        try {
            String sql = "SELECT SUM(od.quantity * od.price) AS totalPenjualan FROM orders o JOIN order_details od ON o.order_id = od.order_id where o.payment_method IS NOT NULL AND payment_date  BETWEEN ? AND ?";
            Long totalPenjualan = jdbcTemplate.queryForObject(sql, new Object[]{startTimestamp, endTimestamp}, Long.class);            
            System.out.println("ceekkkkkkk -> "+totalPenjualan);
            return totalPenjualan != null ? totalPenjualan : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public Long sumOrdersDay() {
        try {
            String sql = "SELECT COALESCE( SUM(od.quantity * od.price),0) AS totalPenjualan FROM orders o JOIN order_details od ON o.order_id = od.order_id where o.payment_method IS NOT NULL AND payment_date = CURRENT_DATE";
            Long totalPenjualan = jdbcTemplate.queryForObject(sql,  Long.class);   
            System.out.println("sumOrder : "+totalPenjualan);         
            return totalPenjualan != null ? totalPenjualan : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public Long sumOrdersBeforeDay(String startDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);

        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        try {
            String sql = "SELECT COALESCE( SUM(od.quantity * od.price),0) AS totalPenjualan FROM orders o JOIN order_details od ON o.order_id = od.order_id where o.payment_method IS NOT NULL AND payment_date < ?";
            Long totalPenjualan = jdbcTemplate.queryForObject(sql,new Object[]{startTimestamp},  Long.class);   
            System.out.println("sumOrder -> "+totalPenjualan);         
            return totalPenjualan != null ? totalPenjualan : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }
    public Long updateOrderStatus(String authId, String id, Order.Status status) {
        try {
            return (long) jdbcTemplate.update(con -> {
                final PreparedStatement ps = con.prepareStatement(
                        "UPDATE " + Order.TABLE_NAME + " SET status = ? WHERE order_id = ?");
                ps.setString(1, status.toString());
                ps.setString(3, id);
                return ps;
            });
        } catch (Exception e) {
            log.error("Gagal update status Auction: {}", e.getMessage());
            return 0L;
        }
    }

    public Long sumByPaymentMethod(String startDate, String endDate, String paymentMethod) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
        LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

        // Set start timestamp to the start of the day
        Timestamp startTimestamp = Timestamp.valueOf(startLocalDate.atStartOfDay());
        // Set end timestamp to the end of the day (23:59:59)
        Timestamp endTimestamp = Timestamp.valueOf(endLocalDate.atTime(LocalTime.MAX));

        try {
            String sql = "SELECT SUM(od.quantity * od.price) FROM orders o JOIN order_details od ON o.order_id = od.order_id WHERE o.order_date BETWEEN ? AND ? AND o.payment_method = ?";
            Long jumlah = jdbcTemplate.queryForObject(sql, new Object[]{startTimestamp, endTimestamp, paymentMethod}, Long.class);
            return jumlah != null ? jumlah : 0L;
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public boolean paid(final Order order) {
        final String sql = "UPDATE " + Order.TABLE_NAME + " SET status = ?, payment_method = ?, payment_date = ?  WHERE order_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, order.status().name());
                ps.setString(2,order.paymentMethod());
                OffsetDateTime paymentOffsetTime = order.paymentDate();
                if (paymentOffsetTime != null) {
                    ps.setTimestamp(3, Timestamp.from(paymentOffsetTime.toInstant()));
                } else {
                    ps.setNull(3, java.sql.Types.TIMESTAMP); // Set null jika paymentTime null
                }
                ps.setString(4, order.orderId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Gagal untuk update order : {}", e.getMessage());
            return false;
        }
    }
}

