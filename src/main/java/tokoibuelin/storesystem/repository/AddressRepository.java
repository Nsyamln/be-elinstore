package tokoibuelin.storesystem.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import tokoibuelin.storesystem.entity.Address;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AddressRepository {
    private static final Logger log = LoggerFactory.getLogger(AddressRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public AddressRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void testInsertAddress() {
        try {
            Connection connection = jdbcTemplate.getDataSource().getConnection();
            String sql = "INSERT INTO addresses (user_id, street, rt, rw, village, district, city, postal_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, "US005");
            ps.setString(2, "Jl Sukapura");
            ps.setString(3, "021");
            ps.setString(4, "002");
            ps.setString(5, "Desa Sukaraja");
            ps.setString(6, "Kecamatan Cikoneng");
            ps.setString(7, "Kabupaten Ciamis");
            ps.setString(8, "46268");
            int updateCount = ps.executeUpdate();
            System.out.println("Rows affected: " + updateCount);
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                System.out.println("Generated key: " + generatedKeys.getString(1));
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    public String saveAddress(final Address address) {
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            int updateCount = jdbcTemplate.update(con -> {
                PreparedStatement ps = address.insert(con);
                return ps;
            }, keyHolder);

            if (updateCount != 1) {
                log.warn("Update count was not 1, it was: {}", updateCount);
                return null;
            }

            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("address_id")) {
                return (String) keys.get("address_id");
            }

            return null;
        } catch (Exception e) {
            log.error("Error during saveAddress: {}", e.getMessage());
            return null;
        }
    }

    public Optional<Address> findOneByUserId(String userId) {
    final String sql = "SELECT * FROM addresses WHERE user_id = ?";
    return jdbcTemplate.query(con -> {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, userId);
        return ps;
    }, rs -> {
        if (rs.next()) {
            return Optional.of(new Address(
                rs.getString("address_id"),
                rs.getString("user_id"),
                rs.getString("street"),
                rs.getString("rt"),
                rs.getString("rw"),
                rs.getString("village"),
                rs.getString("district"),
                rs.getString("city"),
                rs.getString("postal_code")
            ));
        } else {
            return Optional.empty(); // tidak ditemukan
        }
    });
}

    public Optional<Address> findOneByAddressId(String addressId) {
        System.out.println("ID address nya : " + addressId);
        if (addressId == null || addressId.isEmpty()) {
            return Optional.empty();
        }

        String sql = "SELECT address_id, user_id, street, rt, rw, village, district, city, postal_code FROM " + Address.TABLE_NAME + " WHERE address_id = ?";

        try {
            Address address = jdbcTemplate.queryForObject(
                sql,
                new Object[]{addressId}, 
                (rs, rowNum) -> { 
                    final String retrievedAddressId = rs.getString("address_id");
                    final String userId = rs.getString("user_id");
                    final String street = rs.getString("street");
                    final String rt = rs.getString("rt");
                    final String rw = rs.getString("rw");
                    final String village = rs.getString("village");
                    final String district = rs.getString("district");
                    final String city = rs.getString("city");
                    final String postalCode = rs.getString("postal_code");
                    return new Address(retrievedAddressId, userId, street, rt, rw, village, district, city, postalCode);
                }
            );
            return Optional.of(address); 
        } catch (EmptyResultDataAccessException e) {
            System.out.println("Alamat dengan ID " + addressId + " tidak ditemukan.");
            return Optional.empty(); 
        }
    }


    public List<Address> findByUserId(String userId) {
        System.out.println("Mencari daftar alamat untuk ID User: " + userId);
        if (userId == null || userId.trim().isEmpty()) {
            return List.of(); 
        }

        String sql = "SELECT * FROM " + Address.TABLE_NAME + " WHERE user_id=?";
        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> {
            
            final String addressId = rs.getString("address_id");
            final String street = rs.getString("street");
            final String rt = rs.getString("rt");
            final String rw = rs.getString("rw");
            final String village = rs.getString("village");
            final String district = rs.getString("district");
            final String city = rs.getString("city");
            final String postalCode = rs.getString("postal_code");
            return new Address(addressId, userId, street, rt, rw, village, district, city, postalCode);
        });
    }
    public String formatAddress(String addressId) {
        String sql = "SELECT street, rt, rw, village, district, city, postal_code FROM addresses WHERE address_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{addressId}, (rs, rowNum) -> {
            String street = rs.getString("street");
            String rt = rs.getString("rt");
            String rw = rs.getString("rw");
            String village = rs.getString("village");
            String district = rs.getString("district");
            String city = rs.getString("city");
            String postalCode = rs.getString("postal_code");
            return String.format("%s, RT %s/RW %s, %s, %s, %s, %s", street, rt, rw, village, district, city, postalCode);
        });
    }

    public boolean updateAddress(final Address address) {
        final String sql = "UPDATE " + Address.TABLE_NAME + " SET street = ?, rt = ?, rw=?, village = ?, district=?, city=?, postal_code=? WHERE address_id = ?";
        try {
            int rowsAffected = jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, address.street());
                ps.setString(2,address.rt());
                ps.setString(3,address.rw());
                ps.setString(4, address.village());
                ps.setString(5, address.district());
                ps.setString(6, address.city());
                ps.setString(7, address.postalCode());
                ps.setString(8,address.addressId());
                return ps;
            });
            return rowsAffected > 0;
        } catch (Exception e) {
            log.error("Failed to update Address: {}", e.getMessage());
            return false;
        }
    }

    public long deleteAddress(String addressId) {
        try {
            String sql = "DELETE FROM " + Address.TABLE_NAME + "  WHERE address_id=?";
            return jdbcTemplate.update(sql, addressId);
        } catch (Exception e) {
            log.error("Gagal untuk menghapus kategori: {}", e.getMessage());
            return 0L;
        }
    }
}
