package tokoibuelin.storesystem.model.request;

public record UpdateAddressReq(String street,
                               String rt,
                               String rw,
                               String village,
                               String district,
                               String city,
                               String postalCode) {
}
