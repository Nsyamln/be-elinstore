package tokoibuelin.storesystem.service;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tokoibuelin.storesystem.entity.Address;
import tokoibuelin.storesystem.entity.User;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.request.*;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.response.UserDto;
import tokoibuelin.storesystem.repository.AddressRepository;
import tokoibuelin.storesystem.repository.UserRepository;
import tokoibuelin.storesystem.util.HexUtils;
import tokoibuelin.storesystem.util.JwtUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import tokoibuelin.storesystem.util.Base64Utils;


@Service
public class UserService extends AbstractService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final byte[] jwtKey;
    private final EmailService emailService;

    public UserService(final Environment env, final UserRepository userRepository, final PasswordEncoder passwordEncoder, final AddressRepository addressRepository, final EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        final String skJwtKey = env.getProperty("jwt.secret-key");
        this.jwtKey = HexUtils.hexToBytes(skJwtKey);
        this.addressRepository = addressRepository;
        this.emailService = emailService;
    }

    public Response<Object> login(final LoginReq req) {
        if (req == null) {
            return Response.badRequest();
        }
        final Optional<User> userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isEmpty()) {
            return Response.create("08", "01", "Email  salah", null);
        }
        final User user = userOpt.get();

         String encodedPassword = Base64Utils.base64Encode(req.password().getBytes());
         System.out.println("from req : "+encodedPassword);

        // String pwd = passwordEncoder

        if (!passwordEncoder.matches(req.password(), user.password())) {
            System.out.println("user password : "+user.password());
            return Response.create("08", "02", "password salah", null);
        }
        final Authentication authentication = new Authentication(user.userId(), user.role(), true);
        //SecurityContextHolder.setAuthentication(authentication);
        final long iat = System.currentTimeMillis();
        final long exp = 1000 * 60 * 60 * 24; // 24 hour
        final JwtUtils.Header header = new JwtUtils.Header() //
                .add("typ", "JWT") //
                .add("alg", "HS256"); //
        final JwtUtils.Payload payload = new JwtUtils.Payload() //
                .add("sub", authentication.id()) //
                .add("role", user.role().name()) //
                .add("iat", iat) //
                .add("exp", exp); //
        final String token = JwtUtils.hs256Tokenize(header, payload, jwtKey);
        return Response.create("08", "00", "Sukses", token);
    }

    @Transactional
    public Response<Object> registerSupplier(final Authentication authentication, final RegistUsersReq req) {
        // Periksa precondition
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {

            // Validasi request
            if (req == null) {
                return Response.badRequest();
            }

            // Encode password
            final String encoded = passwordEncoder.encode(req.password());

            // Buat objek User
            final User user = new User(
                    null, // ID akan dihasilkan setelah penyimpanan
                    req.name(),
                    req.email(),
                    encoded,
                    User.Role.PEMASOK,
                    req.phone(),
                    authentication.id(),
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null
            );

            // Simpan user
            final String savedId = userRepository.saveUser(user);

            // Jika user berhasil disimpan, simpan alamat
            if (savedId != null) {
                Address address = new Address(
                        null, // ID alamat akan dihasilkan setelah penyimpanan
                        savedId, // ID user yang baru disimpan
                        req.street(),
                        req.rt(),
                        req.rw(),
                        req.village(),
                        req.district(),
                        req.city(),
                        req.postalCode()
                );

                // Simpan alamat
                String addressSavedId = addressRepository.saveAddress(address);

                // Jika alamat berhasil disimpan
                if (addressSavedId != null) {
                    return Response.create("05", "00", "Sukses", savedId);
                }
            }

            // Jika ada yang gagal
            return Response.create("05", "01", "Gagal mendaftarkan supplier", null);
        });
    }

    public Response<Object> registerPegawai(final Authentication authentication, final RegistEmployReq req) {
        // Periksa precondition
        return precondition(authentication, User.Role.ADMIN,User.Role.PEMILIK).orElseGet(() -> {

            // Validasi request
            if (req == null) {
                return Response.badRequest();
            }

            // Encode password
            final String encoded = passwordEncoder.encode(req.password());

            // Buat objek User
            final User user = new User(
                null, 
                req.name(),
                req.email(),
                encoded,
                User.Role.fromString(req.role()),
                req.phone(),
                authentication.id(),
                null,
                null,
                OffsetDateTime.now(),
                null,
                null
            );

            // Simpan user
            final String savedId = userRepository.saveUser(user);

            // Jika user berhasil disimpan, simpan alamat
            if (savedId != null) {
                Address address = new Address(
                        null, // ID alamat akan dihasilkan setelah penyimpanan
                        savedId, // ID user yang baru disimpan
                        req.street(),
                        req.rt(),
                        req.rw(),
                        req.village(),
                        req.district(),
                        req.city(),
                        req.postalCode()
                );

                // Simpan alamat
                String addressSavedId = addressRepository.saveAddress(address);

                // Jika alamat berhasil disimpan
                if (addressSavedId != null) {
                    return Response.create("05", "00", "Sukses", savedId);
                }
            }

            // Jika ada yang gagal
            return Response.create("05", "01", "Gagal mendaftarkan supplier", null);
        });
    }

    @Transactional
    public Response<Object> registerBuyer( final RegisterReq req) {
            if (req == null) {
                return Response.badRequest();
            }

            Optional<User> existingUser = userRepository.findByEmail(req.email());
            if (existingUser.isPresent()) {
                return Response.create("05", "02", "Email sudah terdaftar", null);
            }



            final String encoded = passwordEncoder.encode(req.password());
            final User user = new User(
                    null,
                    req.name(),
                    req.email(),
                    encoded, 
                    User.Role.PELANGGAN,
                    req.phone(),
                    null, 
                    null,
                    null,
                    OffsetDateTime.now(),
                    null,
                    null
            );

            final String savedId = userRepository.saveUser(user); // mengubah dari Long menjadi String

            if (savedId != null) {
            // Lakukan update pada kolom created_by dengan ID yang baru didapat
            boolean updated = userRepository.updateUserCreatedBy(savedId, savedId);

            if (updated) {
                // Jika update berhasil, kembalikan response sukses
                return Response.create("05", "00", "Sukses", savedId);
            } else {
                // Jika update gagal, lempar RuntimeException agar transaksi di-rollback
                throw new RuntimeException("Gagal memperbarui kolom 'created_by' untuk user dengan ID: " + savedId);
                // Atau, Anda bisa mengembalikan Response.create("05", "01", "Gagal memperbarui created_by User", null);
                // Tergantung pada bagaimana Anda ingin menangani kegagalan update ini.
            }
        }

        // Jika savedId null (userRepository.saveUser gagal)
        return Response.create("05", "01", "Gagal mendaftarkan sebagai User", null);

    }


    public Response<Object> forgetPassword(final ResetPasswordReq email) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email.email());
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "Pengguna tidak ditemukan", null);
            } else {
                // Generate reset token
                String resetToken = generateResetToken();
                
                boolean tokenSaved = userRepository.saveResetToken(email.email(), resetToken);
                
                if (tokenSaved) {
                    // Send reset email
                    emailService.sendPasswordResetEmail(email.email(), resetToken);
                    return Response.create("07", "00", "Link reset password telah dikirim ke email Anda", null);
                } else {
                    return Response.create("07", "04", "Gagal menyimpan token reset", null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.create("07", "03", "Terjadi kesalahan", e.getMessage());
        }
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    public Response<Object> resetPassword(final ResetPasswordReq req) {
        try {
            // Validate the reset token
            Optional<String> emailOpt = userRepository.validateResetToken(req.token());
            if (emailOpt.isEmpty()) {
                return Response.create("07", "05", "Token reset password tidak valid atau sudah kadaluarsa", null);
            }

            String email = emailOpt.get();
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "Pengguna tidak ditemukan", null);
            }

            User user = userOpt.get();
            String newEncodedPassword = passwordEncoder.encode(req.newPassword());
            String updatedUserId = userRepository.resetPassword(user.userId(), newEncodedPassword);
            
            if (updatedUserId == null) {
                return Response.create("07", "04", "Gagal memperbarui password", null);
            }

            // Delete the used token
            userRepository.deleteResetToken(req.token());

            UserDto userDto = new UserDto(user.userId(), user.name());
            return Response.create("07", "00", "Password berhasil diperbarui", userDto);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.create("07", "07", "Terjadi kesalahan", e.getMessage());
        }
    }

    public Response<Object> deletedUser(Authentication authentication, String userId) {
        return precondition(authentication, User.Role.ADMIN).orElseGet(() -> {
            Optional<User> userOpt = userRepository.findById(userId);
            System.out.println("masuk");
            if (!userOpt.isPresent()) {
                return Response.create("10", "02", "ID tidak ditemukan", null);
            }

            User dataUser = userOpt.get();

            if (dataUser.deletedAt() != null) {
                return Response.create("10", "03", "Data sudah dihapus", null);
            }

            User updatedUser = new User(
                    dataUser.userId(),
                    dataUser.name(),
                    dataUser.email(),
                    dataUser.password(),
                    dataUser.role(),
                    dataUser.phone(),
                    dataUser.createdBy(),
                    dataUser.updatedBy(),
                    authentication.id(),
                    dataUser.createdAt(),
                    dataUser.updatedAt(),
                    OffsetDateTime.now());

            Long updatedRows = userRepository.deletedUser(updatedUser);

            System.out.println("cek rows "+updatedRows);
            if (updatedRows > 0) {
                return Response.create("10", "00", "Berhasil hapus data", null);
            } else {
                return Response.create("10", "01", "Gagal hapus data", null);
            }
        });
    }

    @Transactional
    public Response<Object> updateName(final Authentication authentication, final UpdateNameReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK).orElseGet(() -> {
            Optional<User> userOpt = userRepository.findById(authentication.id());
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "User tidak ditemukan", null);
            }

            User user = userOpt.get();
            User updatedUser = new User(
                    user.userId(),
                    req.name() != null ? req.name() : user.name(),
                    user.email(),
                    user.password(), 
                    user.role(),
                    user.phone(),
                    user.createdBy(),
                    authentication.id(),
                    user.deletedBy(),
                    user.createdAt(),
                    OffsetDateTime.now(),
                    user.deletedAt()
            );

            if (userRepository.updateNameUser(updatedUser)) {
               
                return Response.create("07", "00", "Nama berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal memperbarui Nama", null);
            }
        });
    }

    @Transactional
    public Response<Object> updatePhoneNumber(final Authentication authentication, final UpdatePhoneNumberReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK).orElseGet(() -> {
            Optional<User> userOpt = userRepository.findById(authentication.id());
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "User tidak ditemukan", null);
            }
            System.out.println("cek newPhoneNumber : "+req.phoneNumber());
            User user = userOpt.get();
            User updatedUser = new User(
                    user.userId(),
                    user.name(),
                    user.email(),
                    user.password(), 
                    user.role(),
                    req.phoneNumber() !=null ?req.phoneNumber():user.phone(),
                    user.createdBy(),
                    authentication.id(),
                    user.deletedBy(),
                    user.createdAt(),
                    OffsetDateTime.now(),
                    user.deletedAt()
            );

            if (userRepository.updateTelpUser(updatedUser)) {
               
                return Response.create("07", "00", "Nomor telepon berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal memperbarui nomor telepon", null);
            }
        });
    }

    @Transactional
    public Response<Object> updateDataUser(final Authentication authentication, final UpdateUserReq req, String userId) {
    return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK).orElseGet(() -> {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return Response.create("07", "01", "User tidak ditemukan", null);
        }

        User existingUser = userOpt.get();

        // Update User Entity
        User updatedUser = new User(
            existingUser.userId(),
            req.name() != null ? req.name() : existingUser.name(),
            req.email() != null ? req.email() : existingUser.email(),
            existingUser.password(),
            existingUser.role(),
            req.phone() != null ? req.phone() : existingUser.phone(),
            existingUser.createdBy(),
            authentication.id(), // updated_by
            existingUser.deletedBy(),
            existingUser.createdAt(),
            OffsetDateTime.now(), // updated_at
            existingUser.deletedAt()
        );
        Optional<Address> addressOpt = addressRepository.findOneByUserId(userId);
        Address existingAddress = addressOpt.get();
        // Update Address Entity
        Address updatedAddress = new Address(
            existingAddress.addressId(),
            existingAddress.userId(),
            req.street() != null ? req.street() : null,
            req.rt() != null ? req.rt() : null,
            req.rw() != null ? req.rw() : null,
            req.village() != null ? req.village() : null,
            req.district() != null ? req.district() : null,
            req.city() != null ? req.city() : null,
            req.postalCode() != null ? req.postalCode() : null
        );

        boolean userUpdated = userRepository.updateUsers(updatedUser);
        boolean addressUpdated = addressRepository.updateAddress(updatedAddress);

        if (userUpdated || addressUpdated) {
            return Response.create("07", "00", "Data pengguna berhasil diperbarui", null);
        } else {
            return Response.create("07", "02", "Gagal memperbarui data pengguna", null);
        }
    });
}

    @Transactional
    public Response<Object> updatePassword(final Authentication authentication, final UpdatePasswordReq req) {
        return precondition(authentication, User.Role.ADMIN, User.Role.PELANGGAN, User.Role.PEMASOK).orElseGet(() -> {
            Optional<User> userOpt = userRepository.findById(authentication.id());
            if (userOpt.isEmpty()) {
                return Response.create("07", "01", "User tidak ditemukan", null);
            }

            final String encoded = passwordEncoder.encode(req.newPassword());

            User user = userOpt.get();
            User updatedUser = new User(
                    user.userId(),
                    user.name(),
                    user.email(),
                    encoded, 
                    user.role(),
                    user.phone(),
                    user.createdBy(),
                    authentication.id(),
                    user.deletedBy(),
                    user.createdAt(),
                    OffsetDateTime.now(),
                    user.deletedAt()
            );

            if (userRepository.updatePassUser(updatedUser)) {
               
                return Response.create("07", "00", "Password berhasil diperbarui", null);
            } else {
                return Response.create("07", "02", "Gagal memperbarui Password", null);
            }
        });
    }
    

    public Response<Object> sendEmail(Authentication authentication, SendEmailReq req) {
        try {
            emailService.sendContactFormEmail(req);
            return Response.create("06", "00", "Pesan berhasil dikirim", null);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.create("06", "01", "Gagal mengirim pesan", e.getMessage());
        }
    }

    
}
