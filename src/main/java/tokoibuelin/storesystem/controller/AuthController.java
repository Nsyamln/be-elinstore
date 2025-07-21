package tokoibuelin.storesystem.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tokoibuelin.storesystem.model.request.LoginReq;
import tokoibuelin.storesystem.model.Response;
import tokoibuelin.storesystem.model.request.ResetPasswordReq;
import tokoibuelin.storesystem.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@CrossOrigin(origins = {"http://127.0.0.1:5500","http://127.0.0.1:5173", "http://127.0.0.1:3000"})

public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Response<Object> login(@RequestBody LoginReq req) {
        return userService.login(req);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Implement logic to invalidate user session or token here
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public Response<Object> resetPassword(@RequestBody ResetPasswordReq req) {
        return userService.resetPassword(req);

    }

    @PostMapping("/forget-password")
    public Response<Object> forgetPassword(@RequestBody ResetPasswordReq email) {
        return userService.forgetPassword(email);
    }
    
}
