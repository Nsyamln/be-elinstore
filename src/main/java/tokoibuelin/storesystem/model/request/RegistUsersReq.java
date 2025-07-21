package tokoibuelin.storesystem.model.request;


public record RegistUsersReq(
        String name,
        String email,
        String password,
        String phone,
        String street,
        String rt,
        String rw,
        String village,
        String district,
        String city,
        String postalCode
) {
}
