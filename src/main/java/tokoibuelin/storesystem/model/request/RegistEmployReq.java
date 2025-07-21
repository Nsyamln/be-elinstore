package tokoibuelin.storesystem.model.request;

public record RegistEmployReq(
    String name,
    String email,
    String password,
    String phone,
    String role,
    String street,
    String rt,
    String rw,
    String village,
    String district,
    String city,
    String postalCode
) {
    
}
