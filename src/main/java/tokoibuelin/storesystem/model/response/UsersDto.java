package tokoibuelin.storesystem.model.response;

public record UsersDto (
    String userId,
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
