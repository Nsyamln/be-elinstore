package tokoibuelin.storesystem.model.response;

public record CashFlowDto (
        Long totalPenjualan,
        Long bankBRI,
        Long cash,
        Long shopeepay,
        Long bayarPemasok,
        Long kasMasuk,
        Long kasKeluar,
        Long kasBersih

){
}
