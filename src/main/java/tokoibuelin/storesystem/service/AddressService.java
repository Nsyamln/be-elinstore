package tokoibuelin.storesystem.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tokoibuelin.storesystem.entity.Address;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.UpdateAddressReq;
import tokoibuelin.storesystem.model.request.addAddressesReq;
import tokoibuelin.storesystem.repository.AddressRepository;
import tokoibuelin.storesystem.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AddressService extends AbstractService{
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public  AddressService(final AddressRepository addressRepository, final UserRepository userRepository, JdbcTemplate jdbcTemplate){
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Response<Object> updateAddress(final Authentication authentication, final UpdateAddressReq req,final String addressId) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK, User.Role.PEMILIK).orElseGet(() -> {
            Optional<Address> addressOpt = addressRepository.findOneByAddressId (addressId);
            if (addressOpt.isEmpty()) {
                return Response.create("07", "01", "Alamat tidak ditemukan", null);
            }
            Address address = addressOpt.get();
            Address updatedAddress = new Address(
                    address.addressId(),
                    address.userId(),
                    req.street(),
                    req.rt(),
                    req.rw(),
                    req.village(),
                    req.district(),
                    req.city(),
                    req.postalCode()
            );

            if (addressRepository.updateAddress(updatedAddress)) {
                return Response.create("07", "00", "Alamat berhasil diupdate", null);
            } else {
                return Response.create("07", "02", "Gagal mengupdate Alamat", null);
            }
        });
    }

    public Response<Object> addAddress(final Authentication authentication, final addAddressesReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK, User.Role.PEMILIK).orElseGet(() -> {
            Optional<User> addressOpt = userRepository.findById(authentication.id());
            if (addressOpt.isEmpty()) {
                return Response.create("07", "01", "User tidak ditemukan", null);
            }
            Address address = new Address(
                        null,
                        authentication.id(),
                        req.street(),
                        req.rt(),
                        req.rw(),
                        req.village(),
                        req.district(),
                        req.city(),
                        req.postalCode()
                );
                String  save = addressRepository.saveAddress(address);
                if (null == save) {
                    return Response.create("05", "01", "Gagal mendaftarkan sebagai User", null);
                }

            return  Response.create("07", "00", "Sukses", null);
        });
    }

    public String extractCity(String address) {
        // Daftar istilah yang dapat digunakan
        String[] keywords = {"Kota", "Kabupaten", "Kab"};

        for (String keyword : keywords) {
            int keywordIndex = address.indexOf(keyword);
            if (keywordIndex != -1) {
                // Ambil substring setelah keyword
                String afterKeyword = address.substring(keywordIndex + keyword.length()).trim();

                // Ambil nama kota sebelum kode pos
                String[] parts = afterKeyword.split(" ");
                if (parts.length > 1) {
                    return parts[0];
                }
            }
        }
        return null; // atau return "" jika kota tidak ditemukan
    }

    public List<Address> getAddressesByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            // Atau throw custom exception jika userId tidak valid
            return List.of();
        }
        return addressRepository.findByUserId(userId);
    }

    public Address getAddressById(String userId) {
        String sql = "SELECT * FROM addresses WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{userId}, (rs, rowNum) ->
                new Address(
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
    }

    private static final List<String> PRIANGAN_TIMUR_CITIES = Arrays.asList(
            "Bandung", "Tasikmalaya", "Ciamis", "Garut", "Sumedang", "Cianjur", "Banjar", "Pangandaran"
    );


    public Response<Object> deleteAddress(Authentication authentication, String addressId) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PEMILIK, User.Role.PELANGGAN).orElseGet(() -> {
            Optional<Address> addressOpt = addressRepository.findOneByAddressId (addressId);

            if (!addressOpt.isPresent()) {
                return Response.create("10", "02", "ID tidak ditemukan", null);
            }

            Long updatedRows = addressRepository.deleteAddress(addressId);
            if (updatedRows > 0) {
                return Response.create("10", "00", "Berhasil hapus data", null);
            } else {
                return Response.create("10", "01", "Gagal hapus data", null);
            }
        });
    }

    public Response<Object> validateAddress(Address address) {
        if (isValidAddress(address)) {
            return Response.create("ADDR", "200", "Address is valid.", address);
        }

        String errorMessage = generateErrorMessage(address);
        return Response.create("ADDR", "400", errorMessage, null);
    }

    public boolean isValidAddress(Address address) {
        return isCityValid(address.city()) &&
                isRTValid(address.rt()) &&
                isRWValid(address.rw()) &&
                isPostalCodeValid(address.postalCode());
    }

    private boolean isCityValid(String city) {
        return PRIANGAN_TIMUR_CITIES.stream()
                .anyMatch(validCity -> city.toLowerCase().contains(validCity.toLowerCase()));
    }

    private boolean isRTValid(String rt) {
        return rt.matches("^\\d{1,3}$");
    }

    private boolean isRWValid(String rw) {
        return rw.matches("^\\d{1,3}$");
    }

    private boolean isPostalCodeValid(String postalCode) {
        return postalCode.matches("^\\d{5}$");
    }

    private String generateErrorMessage(Address address) {
        if (!isCityValid(address.city())) {
            return "City is not valid.";
        }
        if (!isRTValid(address.rt())) {
            return "RT format is not valid.";
        }
        if (!isRWValid(address.rw())) {
            return "RW format is not valid.";
        }
        if (!isPostalCodeValid(address.postalCode())) {
            return "Postal code format is not valid.";
        }
        return "Address is not valid.";
    }
}
