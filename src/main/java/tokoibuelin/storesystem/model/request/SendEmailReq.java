package tokoibuelin.storesystem.model.request;

public record SendEmailReq(
    String name,
    String email,
    String subject,
    String message
) {
    
}
