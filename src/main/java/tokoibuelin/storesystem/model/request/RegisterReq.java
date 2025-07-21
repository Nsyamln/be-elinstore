package tokoibuelin.storesystem.model.request;

public record RegisterReq(
        String name,
        String email,
        String password,
        String phone
) {
}
