package tokoibuelin.storesystem.model.request;

public record addAddressesReq(
    String street,
    String rt,
    String rw,
    String village,
    String district,
    String city,
    String postalCode) {
}
