package tokoibuelin.storesystem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import tokoibuelin.storesystem.model.Authentication;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.RegistEmployReq;
import tokoibuelin.storesystem.model.request.RegistUsersReq;
import tokoibuelin.storesystem.model.request.SendEmailReq;
import tokoibuelin.storesystem.model.request.UpdateNameReq;
import tokoibuelin.storesystem.model.request.UpdatePasswordReq;
import tokoibuelin.storesystem.model.request.UpdatePhoneNumberReq;
import tokoibuelin.storesystem.model.request.UpdateUserReq;
import tokoibuelin.storesystem.model.response.UsersDto;
import tokoibuelin.storesystem.repository.UserRepository;
import tokoibuelin.storesystem.util.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.service.UserService;

import java.util.List;
import java.util.Optional;




@RestController
@RequestMapping("/secured/user")
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:5173", "http://127.0.0.1:3000"})
public class UserController {
    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUserById/{userId}")
    public Optional<UsersDto> getUserById(@PathVariable String userId){
        return userRepository.findWithAddressById(userId);
    }

    @PostMapping("/register-supplier")
    public Response<Object> registerSeller(@RequestBody RegistUsersReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.registerSupplier(authentication, req);
    }

    @PostMapping("/register-pegawai")
    public Response<Object> registerPegawai(@RequestBody RegistEmployReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.registerPegawai(authentication, req);
    }

    @PutMapping("/update-name")
    public Response<Object> updateProfile(@RequestBody UpdateNameReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.updateName(authentication, req);
    }

    @PutMapping("/update-phone")
    public Response<Object> updatePhoneNumber(@RequestBody UpdatePhoneNumberReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.updatePhoneNumber(authentication, req);
    }

    @PutMapping("/update-password")
    public Response<Object> updatePassword(@RequestBody UpdatePasswordReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.updatePassword(authentication, req);
    }

    @PostMapping("/update-dataUser/{userId}")
    public Response<Object> updateDataUser(@RequestBody UpdateUserReq req, @PathVariable String userId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.updateDataUser(authentication, req,userId);
    }

    @DeleteMapping("/delete-user/{userId}")
    public Response<Object> deleteUser(@PathVariable String userId) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.deletedUser(authentication, userId);
    }

    @PostMapping("/send-email")
    public Response<Object> sendEmail(@RequestBody SendEmailReq req) {
        Authentication authentication = SecurityContextHolder.getAuthentication();
        return userService.sendEmail(authentication,req);
    }

    @GetMapping("/getEmploys")
    public ResponseEntity<List<UsersDto>> getEmployWithAddress() {
        List<UsersDto> users = userRepository.getEmployWithAddress();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/getUsers/{role}")
    public ResponseEntity<List<UsersDto>> getUsersWithAddress(@PathVariable String role) {
        List<UsersDto> users = userRepository.getUsersWithAddress(role);
        return ResponseEntity.ok(users);
    }
    
}
