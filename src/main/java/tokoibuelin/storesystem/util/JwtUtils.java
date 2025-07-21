package tokoibuelin.storesystem.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class JwtUtils {

    public static Jwt hs256Parse(String jwt, byte[] bytesKey) {
        if (jwt == null) {
            throw new IllegalArgumentException("JWT token must be not null.");
        }
        if (bytesKey.length < 32) {
            throw new IllegalArgumentException(
                    "The signing key's size is "
                            + (bytesKey.length << 3)
                            + " bits which is not secure enough for the HS256 algorithm.  The JWT JWA Specification (RFC 7518, Section 3.2) states that keys used with HS256 MUST have a size >= 256 bits (the key size must be greater than or equal to the hash output size).  Consider using the io.jsonwebtoken.security.Keys class's 'secretKeyFor(SignatureAlgorithm.HS256)' method to create a key guaranteed to be secure enough for HS256.  See https://tools.ietf.org/html/rfc7518#section-3.2 for more information.");
        }

        String base64UrlEncodedHeader = null;
        String base64UrlEncodedPayload = null;
        String base64UrlEncodedDigest = null;

        int delimiterCount = 0;
        int offset = 0;
        final char[] chars = jwt.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '.') {
                if (delimiterCount == 0) {
                    base64UrlEncodedHeader = new String(chars, offset, i);
                    offset = i + 1;
                    delimiterCount += 1;
                } else if (delimiterCount == 1) {
                    base64UrlEncodedPayload = new String(chars, offset, i - base64UrlEncodedHeader.length() - 1);
                    offset = i + 1;
                    delimiterCount += 1;
                    base64UrlEncodedDigest = new String(
                            chars,
                            offset,
                            chars.length
                                    - base64UrlEncodedHeader.length()
                                    - base64UrlEncodedPayload.length()
                                    - 2);
                }
            }
        }

        final String data = base64UrlEncodedHeader + "." + base64UrlEncodedPayload;
        final byte[] bytes = MacUtils.hmacSha256(HexUtils.bytesToHex(bytesKey), data);
        if (Base64Utils.base64UrlEncode(bytes).equals(base64UrlEncodedDigest)) {
            return new Jwt(base64UrlEncodedHeader, base64UrlEncodedPayload, base64UrlEncodedDigest, true);
        } else {
            return new Jwt(
                    base64UrlEncodedHeader, base64UrlEncodedPayload, base64UrlEncodedDigest, false);
        }
    }

    public static String hs256Tokenize(Header header, Payload payload, byte[] bytesKey) {
        if (header == null) {
            throw new IllegalArgumentException("JWT header must be not null.");
        }
        if (payload == null) {
            throw new IllegalArgumentException("JWT payload must be not null.");
        }
        if (bytesKey.length < 32) {
            throw new IllegalArgumentException(
                    "The signing key's size is "
                            + (bytesKey.length << 3)
                            + " bits which is not secure enough for the HS256 algorithm.  The JWT JWA Specification (RFC 7518, Section 3.2) states that keys used with HS256 MUST have a size >= 256 bits (the key size must be greater than or equal to the hash output size).  Consider using the io.jsonwebtoken.security.Keys class's 'secretKeyFor(SignatureAlgorithm.HS256)' method to create a key guaranteed to be secure enough for HS256.  See https://tools.ietf.org/html/rfc7518#section-3.2 for more information.");
        }
        final String headerB64Url = Base64Utils.base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
        final String payloadB64Url = Base64Utils.base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
        final String algorithm = "HmacSHA256";
        final String data = headerB64Url + "." + payloadB64Url;
        try {
            final Key key = new SecretKeySpec(bytesKey, algorithm);
            final Mac mac = Mac.getInstance(algorithm);
            mac.init(key);
            final byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return data + "." + Base64Utils.base64UrlEncode(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new AssertionError(e);
        }
    }

    public static final class Jwt {

        private final String header;
        private final String payload;
        private final String signature;
        private final boolean valid;

        private Jwt(
                final String header, final String payload, final String digest, final boolean valid) {
            this.header = new String(Base64Utils.base64UrlDecode(header));
            this.payload = new String(Base64Utils.base64UrlDecode(payload));
            this.signature = HexUtils.bytesToHex(Base64Utils.base64UrlDecode(digest));
            this.valid = valid;
        }

        public String header() {
            return header;
        }

        public String payload() {
            return payload;
        }

        public String signature() {
            return signature;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Jwt jwt = (Jwt) o;
            return valid == jwt.valid
                    && header.equals(jwt.header)
                    && payload.equals(jwt.payload)
                    && signature.equals(jwt.signature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(header, payload, signature, valid);
        }

        @Override
        public String toString() {
            return "Jwt{"
                    + "header='"
                    + header
                    + '\''
                    + ", payload='"
                    + payload
                    + '\''
                    + ", signature='"
                    + signature
                    + '\''
                    + ", valid="
                    + valid
                    + '}';
        }
    }

    public static final class Header {

        private final Map<String, Object> map = new HashMap<>();

        public Header add(String name, Object value) {
            map.put(name, value);
            return this;
        }

        @Override
        public String toString() {
            final KeyValue kv = new KeyValue("", "{", "}", ":", ",", true);
            map.forEach(kv::add);
            return kv.toString();
        }
    }

    public static final class Payload extends KeyValue {

        public Payload() {
            super("", "{", "}", ":", ",", true);
        }

        public Payload add(String name, Object value) {
            super.add(name, value);
            return this;
        }
    }

    private static class KeyValue {

        private final String name;
        private final String start;
        private final String end;
        private final String delimiter;
        private final String separator;
        private final boolean quoteString;
        private final ValueHolder holderHead = new ValueHolder();
        private ValueHolder holderTail = holderHead;

        protected KeyValue(
                String name,
                String start,
                String end,
                String delimiter,
                String separator,
                boolean quoteString) {
            this.name = name;
            this.start = start;
            this.end = end;
            this.delimiter = delimiter;
            this.separator = separator;
            this.quoteString = quoteString;
        }

        public KeyValue add(String name, Object value) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Invalid name");
            }
            ValueHolder valueHolder = addHolder();
            valueHolder.name = name;
            valueHolder.value = value;
            return this;
        }

        private void appendStringValue(StringBuilder builder, Object value) {
            if (quoteString && value instanceof CharSequence) {
                builder.append("\"").append(value).append("\"");
            } else {
                builder.append(value);
            }
        }

        private void appendArrayValue(StringBuilder builder, Object value) {
            String arrayString = value.toString();
            if (quoteString) {
                builder.append('\"').append(arrayString).append('\"');
            } else {
                builder.append(arrayString);
            }
        }

        protected ValueHolder addHolder() {
            ValueHolder valueHolder = new ValueHolder();
            holderTail = holderTail.next = valueHolder;
            return valueHolder;
        }

        @Override
        public String toString() {
            String nextSeparator = "";
            StringBuilder builder = new StringBuilder(32).append(name).append(start);
            for (ValueHolder valueHolder = holderHead.next; valueHolder != null; valueHolder = valueHolder.next) {
                Object value = valueHolder.value;
                value = value == null ? "null" : value;
                builder.append(nextSeparator);
                nextSeparator = separator;
                if (quoteString) {
                    builder.append('\"').append(valueHolder.name).append('\"').append(delimiter);
                } else {
                    builder.append(valueHolder.name).append(delimiter);
                }
                if (value.getClass().isArray()) {
                    appendArrayValue(builder, value);
                } else {
                    appendStringValue(builder, value);
                }
            }
            return builder.append(end).toString();
        }

        static final class ValueHolder {

            private String name;
            private Object value;
            private ValueHolder next;
        }
    }

}
