package tokoibuelin.storesystem.util;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MacUtils {

    MacUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static long unixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String hmacSha256String(String hexKey, String data) {
        final byte[] key = HexUtils.hexToBytes(hexKey);
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        return HexUtils.bytesToHex(hmacSha256(32, key, data.getBytes(StandardCharsets.UTF_8)));
    }

    public static byte[] hmacSha256(String hexKey, String data) {
        final byte[] key = HexUtils.hexToBytes(hexKey);
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        return hmacSha256(32, key, data.getBytes(StandardCharsets.UTF_8));
    }

    public static String weakKeyHmacSha256String(String hexKey, String data) {
        final byte[] key = HexUtils.hexToBytes(hexKey);
        return HexUtils.bytesToHex(hmacSha256(20, key, data.getBytes(StandardCharsets.UTF_8)));
    }

    public static byte[] weakKeyHmacSha256(String hexKey, String data) {
        final byte[] key = HexUtils.hexToBytes(hexKey);
        return hmacSha256(20, key, data.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] hmacSha256(byte[] key, String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        return hmacSha256(32, key, data.getBytes(StandardCharsets.UTF_8));
    }

    public static String hmacSha256String(byte[] key, String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        return HexUtils.bytesToHex(hmacSha256(32, key, data.getBytes(StandardCharsets.UTF_8)));
    }

    public static byte[] weakKeyHmacSha256(byte[] key, String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        return hmacSha256(20, key, data.getBytes(StandardCharsets.UTF_8));
    }

    public static String weakKeyHmacSha256Key(byte[] key, String data) {
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        return HexUtils.bytesToHex(hmacSha256(20, key, data.getBytes(StandardCharsets.UTF_8)));
    }

    private static byte[] hmacSha256(int minimalKeySize, byte[] key, byte[] data) {
        if (key.length < minimalKeySize) {
            throw new IllegalArgumentException(String.format("Key must be %d-bit", minimalKeySize << 3));
        }
        if (data == null) {
            throw new IllegalArgumentException("Data must be not null.");
        }
        try {
            final String algorithm = "HMacSHA256";
            final Mac mac = Mac.getInstance("HMacSHA256");
            mac.init(new SecretKeySpec(key, algorithm));
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static String sha160(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must be not null.");
        }
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return HexUtils.bytesToHex(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static String sha256(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must be not null.");
        }
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexUtils.bytesToHex(digest.digest(key.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
