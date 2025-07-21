package tokoibuelin.storesystem.model;


public record Response<T>(String code, String message, T data) {

    public static Response<Object> create(String serviceCode, String responseCode, String message, Object data) {
        return new Response<>(serviceCode + responseCode, message, data);
    }

    public static Response<Object> unauthenticated() {
        return new Response<>("0101", "unauthenticated", null);
    }

    public static Response<Object> unauthorized() {
        return new Response<>("0201", "unauthorized", null);
    }


    public static Response<Object> badRequest() {
        return new Response<>("0301", "bad request", null);
    }

    // private static final String SERVICE_CODE_ADDRESS = "ADDR"; // Contoh service code untuk Address
    public static  String RESPONSE_CODE_SUCCESS = "00";
    public static  String RESPONSE_CODE_NOT_FOUND = "01";
    public static  String RESPONSE_CODE_INVALID_INPUT = "02";
    public static  String RESPONSE_CODE_NO_CONTENT = "03"; // Untuk kasus list kosong

}
