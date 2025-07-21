package tokoibuelin.storesystem.model.request;

public record ResetPasswordReq(String email, String newPassword, String token) {
}
