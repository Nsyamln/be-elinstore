package tokoibuelin.storesystem.util;


import java.util.Arrays;

public final class HexUtils {

    public static final String BLANK_STRING = "";
    public static final byte[] EMPTY_BYTE = new byte[0];

    private static final char[] HEXDUMP_TABLE = new char[256 * 4];
    private static final byte[] HEX2B = new byte[Character.MAX_VALUE + 1];

    static {
        final char[] DIGITS = "0123456789abcdef".toCharArray();
        for (int i = 0; i < 256; i++) {
            HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 0x0F];
            HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 0x0F];
        }
        Arrays.fill(HEX2B, (byte) -1);
        HEX2B['0'] = (byte) 0;
        HEX2B['1'] = (byte) 1;
        HEX2B['2'] = (byte) 2;
        HEX2B['3'] = (byte) 3;
        HEX2B['4'] = (byte) 4;
        HEX2B['5'] = (byte) 5;
        HEX2B['6'] = (byte) 6;
        HEX2B['7'] = (byte) 7;
        HEX2B['8'] = (byte) 8;
        HEX2B['9'] = (byte) 9;
        HEX2B['A'] = (byte) 10;
        HEX2B['B'] = (byte) 11;
        HEX2B['C'] = (byte) 12;
        HEX2B['D'] = (byte) 13;
        HEX2B['E'] = (byte) 14;
        HEX2B['F'] = (byte) 15;
        HEX2B['a'] = (byte) 10;
        HEX2B['b'] = (byte) 11;
        HEX2B['c'] = (byte) 12;
        HEX2B['d'] = (byte) 13;
        HEX2B['e'] = (byte) 14;
        HEX2B['f'] = (byte) 15;
    }

    HexUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String bytesToHex(byte[] bytes) {
        final int fromIndex = 0;
        final int length;
        if (bytes == null || (length = bytes.length) == 0) {
            return BLANK_STRING;
        }
        final int endIndex = fromIndex + length;
        final char[] buf = new char[length << 1];
        int srcIdx = fromIndex;
        int dstIdx = 0;
        for (; srcIdx < endIndex; srcIdx++, dstIdx += 2) {
            System.arraycopy(HEXDUMP_TABLE, (bytes[srcIdx] & 0xFF) << 1, buf, dstIdx, 2);
        }
        return new String(buf);
    }

    public static byte[] hexToBytes(String hexStream) {
        if (hexStream == null || hexStream.isEmpty()) {
            return EMPTY_BYTE;
        }
        int length = hexStream.length();
        if ((length & 1) != 0) {
            throw new IllegalArgumentException(String.format("Invalid length: %d", length));
        }
        int fromIndex = 0;
        if (hexStream.charAt(0) == '0' && hexStream.charAt(1) == 'x') {
            fromIndex += 2;
            length -= 2;
        }
        final byte[] bytes = new byte[length >>> 1];
        for (int i = 0; i < length; i += 2) {
            int hi = HEX2B[hexStream.charAt(fromIndex + i)];
            int lo = HEX2B[hexStream.charAt(fromIndex + i + 1)];
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException(
                        String.format(
                                "Invalid hex byte '%s' at index %d of '%s'",
                                hexStream.subSequence(fromIndex + i, fromIndex + i + 2), fromIndex + i, hexStream));
            }
            bytes[i >>> 1] = (byte) ((hi << 4) + lo);
        }
        return bytes;
    }
}
