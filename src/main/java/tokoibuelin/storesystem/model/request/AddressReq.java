package tokoibuelin.storesystem.model.request;

public record AddressReq(
                         String userId,
                         String street,
                         String rt,
                         String rw,
                         String village,
                         String district,
                         String city,
                         String postalCode) {
}
