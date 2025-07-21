package tokoibuelin.storesystem.util;


import java.util.Arrays;

public final class Base64Utils {

    private static final Base64 BASE_64_URL_SAFE = new Base64(true);
    private static final Base64 BASE_64_URL_UNSAFE = new Base64(false);

    public static String base64UrlEncode(byte[] bytes) {
        return BASE_64_URL_SAFE.encodeToString(bytes, false);
    }

    public static byte[] base64UrlDecode(String str) {
        return BASE_64_URL_SAFE.decodeFast(str.toCharArray());
    }

    public static byte[] base64UrlDecode(char[] chars) {
        return BASE_64_URL_SAFE.decodeFast(chars);
    }

    public static String base64Encode(byte[] bytes) {
        return BASE_64_URL_UNSAFE.encodeToString(bytes, false);
    }

    public static byte[] base64Decode(String str) {
        return BASE_64_URL_UNSAFE.decodeFast(str.toCharArray());
    }

    public static byte[] base64Decode(char[] chars) {
        return BASE_64_URL_UNSAFE.decodeFast(chars);
    }

    /**
     * A very fast and memory efficient class to encode and decode to and from BASE64 or BASE64URL in
     * full accordance with <a href="https://tools.ietf.org/html/rfc4648">RFC 4648</a>.
     *
     * <p>Based initially on MigBase64 with continued modifications for Base64 URL support and
     * JDK-standard code formatting.
     *
     * <p>This encode/decode algorithm doesn't create any temporary arrays as many other codecs do, it
     * only allocates the resulting array. This produces less garbage and it is possible to handle
     * arrays twice as large as algorithms that create a temporary array.
     *
     * <p>There is also a "fast" version of all decode methods that works the same way as the normal
     * ones, but has a few demands on the decoded input. Normally though, these fast versions should
     * be used if the source if the input is known and it hasn't bee tampered with.
     *
     * @author Mikael Grev
     * @author Les Hazlewood
     * @since 0.10.0
     */
    private static final class Base64 { // final and package-protected on purpose

        private static final char[] BASE64_ALPHABET =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
        private static final char[] BASE64URL_ALPHABET =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
        private static final int[] BASE64_IALPHABET = new int[256];
        private static final int[] BASE64URL_IALPHABET = new int[256];
        private static final int IALPHABET_MAX_INDEX = BASE64_IALPHABET.length - 1;

        static {
            Arrays.fill(BASE64_IALPHABET, -1);
            System.arraycopy(BASE64_IALPHABET, 0, BASE64URL_IALPHABET, 0, BASE64_IALPHABET.length);
            for (int i = 0, iS = BASE64_ALPHABET.length; i < iS; i++) {
                BASE64_IALPHABET[BASE64_ALPHABET[i]] = i;
                BASE64URL_IALPHABET[BASE64URL_ALPHABET[i]] = i;
            }
            BASE64_IALPHABET['='] = 0;
            BASE64URL_IALPHABET['='] = 0;
        }

        private final boolean urlsafe;
        private final char[] ALPHABET;
        private final int[] IALPHABET;

        private Base64(boolean urlsafe) {
            this.urlsafe = urlsafe;
            this.ALPHABET = urlsafe ? BASE64URL_ALPHABET : BASE64_ALPHABET;
            this.IALPHABET = urlsafe ? BASE64URL_IALPHABET : BASE64_IALPHABET;
        }

        // ****************************************************************************************
        // *  char[] version
        // ****************************************************************************************

        private String getName() {
            return urlsafe ? "base64url" : "base64"; // RFC 4648 codec names are all lowercase
        }

        /**
         * Encodes a raw byte array into a BASE64 <code>char[]</code> representation in accordance with
         * RFC 2045.
         *
         * @param sArr The bytes to convert. If <code>null</code> or length 0 an empty array will be
         *     returned.
         * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
         *     No line separator will be in breach of RFC 2045 which specifies max 76 per line but will
         *     be a little faster.
         * @return A BASE64 encoded array. Never <code>null</code>.
         */
        private char[] encodeToChar(byte[] sArr, boolean lineSep) {

            // Check special case
            int sLen = sArr != null ? sArr.length : 0;
            if (sLen == 0) {
                return new char[0];
            }

            int eLen = (sLen / 3) * 3; // # of bytes that can encode evenly into 24-bit chunks
            int left = sLen - eLen; // # of bytes that remain after 24-bit chunking. Always 0, 1 or 2

            int cCnt = (((sLen - 1) / 3 + 1) << 2); // # of base64-encoded characters including padding
            int dLen =
                    cCnt
                            + (lineSep
                            ? (cCnt - 1) / 76 << 1
                            : 0); // Length of returned char array with padding and any line separators

            int padCount = 0;
            if (left == 2) {
                padCount = 1;
            } else if (left == 1) {
                padCount = 2;
            }

            char[] dArr = new char[urlsafe ? (dLen - padCount) : dLen];

            // Encode even 24-bits
            for (int s = 0, d = 0, cc = 0; s < eLen; ) {

                // Copy next three bytes into lower 24 bits of int, paying attention to sign.
                int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

                // Encode the int into four chars
                dArr[d++] = ALPHABET[(i >>> 18) & 0x3f];
                dArr[d++] = ALPHABET[(i >>> 12) & 0x3f];
                dArr[d++] = ALPHABET[(i >>> 6) & 0x3f];
                dArr[d++] = ALPHABET[i & 0x3f];

                // Add optional line separator
                if (lineSep && ++cc == 19 && d < dLen - 2) {
                    dArr[d++] = '\r';
                    dArr[d++] = '\n';
                    cc = 0;
                }
            }

            // Pad and encode last bits if source isn't even 24 bits.
            if (left > 0) {
                // Prepare the int
                int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

                // Set last four chars
                dArr[dLen - 4] = ALPHABET[i >> 12];
                dArr[dLen - 3] = ALPHABET[(i >>> 6) & 0x3f];
                // dArr[dLen - 2] = left == 2 ? ALPHABET[i & 0x3f] : '=';
                // dArr[dLen - 1] = '=';
                if (left == 2) {
                    dArr[dLen - 2] = ALPHABET[i & 0x3f];
                } else if (!urlsafe) { // if not urlsafe, we need to include the padding characters
                    dArr[dLen - 2] = '=';
                }
                if (!urlsafe) { // include padding
                    dArr[dLen - 1] = '=';
                }
            }
            return dArr;
        }

        private int ctoi(char c) {
            int i = c > IALPHABET_MAX_INDEX ? -1 : IALPHABET[c];
            if (i < 0) {
                String msg = "Illegal " + getName() + " character: '" + c + "'";
                throw new RuntimeException(msg);
            }
            return i;
        }

        /**
         * Decodes a BASE64 encoded char array that is known to be reasonably well formatted. The
         * preconditions are:<br>
         * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
         * + Line separator must be "\r\n", as specified in RFC 2045 + The array must not contain
         * illegal characters within the encoded string<br>
         * + The array CAN have illegal characters at the beginning and end, those will be dealt with
         * appropriately.<br>
         *
         * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will
         *     throw an exception.
         * @return The decoded array of bytes. May be of length 0.
         */
        final byte[] decodeFast(char[] sArr) {

            // Check special case
            int sLen = sArr != null ? sArr.length : 0;
            if (sLen == 0) {
                return new byte[0];
            }

            int sIx = 0, eIx = sLen - 1; // Start and end index after trimming.

            // Trim illegal chars from start
            while (sIx < eIx && IALPHABET[sArr[sIx]] < 0) {
                sIx++;
            }

            // Trim illegal chars from end
            while (eIx > 0 && IALPHABET[sArr[eIx]] < 0) {
                eIx--;
            }

            // get the padding count (=) (0, 1 or 2)
            int pad = sArr[eIx] == '=' ? (sArr[eIx - 1] == '=' ? 2 : 1) : 0; // Count '=' at end.
            int cCnt = eIx - sIx + 1; // Content count including possible separators
            int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

            int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
            byte[] dArr = new byte[len]; // Preallocate byte[] of exact length

            // Decode all but the last 0 - 2 bytes.
            int d = 0;
            for (int cc = 0, eLen = (len / 3) * 3; d < eLen; ) {

                // Assemble three bytes into an int from four "valid" characters.
                int i =
                        ctoi(sArr[sIx++]) << 18
                                | ctoi(sArr[sIx++]) << 12
                                | ctoi(sArr[sIx++]) << 6
                                | ctoi(sArr[sIx++]);

                // Add the bytes
                dArr[d++] = (byte) (i >> 16);
                dArr[d++] = (byte) (i >> 8);
                dArr[d++] = (byte) i;

                // If line separator, jump over it.
                if (sepCnt > 0 && ++cc == 19) {
                    sIx += 2;
                    cc = 0;
                }
            }

            if (d < len) {
                // Decode last 1-3 bytes (incl '=') into 1-3 bytes
                int i = 0;
                for (int j = 0; sIx <= eIx - pad; j++) {
                    i |= ctoi(sArr[sIx++]) << (18 - j * 6);
                }

                for (int r = 16; d < len; r -= 8) {
                    dArr[d++] = (byte) (i >> r);
                }
            }

            return dArr;
        }

        /**
         * Encodes a raw byte array into a BASE64 <code>String</code> representation i accordance with
         * RFC 2045.
         *
         * @param sArr The bytes to convert. If <code>null</code> or length 0 an empty array will be
         *     returned.
         * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
         *     No line separator will be in breach of RFC 2045 which specifies max 76 per line but will
         *     be a little faster.
         * @return A BASE64 encoded array. Never <code>null</code>.
         */
        final String encodeToString(byte[] sArr, boolean lineSep) {
            // Reuse char[] since we can't create a String incrementally anyway and StringBuffer/Builder
            // would be slower.
            return new String(encodeToChar(sArr, lineSep));
        }
    }
}
