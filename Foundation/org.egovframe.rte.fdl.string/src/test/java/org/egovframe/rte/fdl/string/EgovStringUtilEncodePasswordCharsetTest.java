package org.egovframe.rte.fdl.string;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Regression test for {@link EgovStringUtil#encodePassword(String, String)}.
 *
 * <p>encodePassword digested {@code password.getBytes()} using the platform default charset,
 * so a non-ASCII password (e.g. a Korean password) produced a different hash on hosts with
 * different default charsets (UTF-8 vs. EUC-KR vs. windows-1252). That breaks authentication
 * consistency across environments. The password must be encoded with a fixed charset (UTF-8).</p>
 *
 * <p>The expected value is computed here over the UTF-8 bytes explicitly, so the assertion holds
 * regardless of the JVM default charset. Run with {@code -Dfile.encoding=ISO-8859-1} to see the
 * pre-fix code fail (it would digest ISO-8859-1 bytes) while the fixed code passes.</p>
 *
 * @author EricSeokgon
 */
class EgovStringUtilEncodePasswordCharsetTest {

    private static String sha256HexUtf8(String s) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    @Test
    @DisplayName("encodePassword hashes over UTF-8 bytes, independent of the platform default charset")
    void encodePassword_usesUtf8ForNonAsciiPassword() throws Exception {
        String password = "비밀번호가나다"; // "비밀번호가나다" (non-ASCII)

        String expected = sha256HexUtf8(password);
        String actual = EgovStringUtil.encodePassword(password, "SHA-256");

        assertEquals(expected, actual,
                "encodePassword must digest the UTF-8 encoding of the password, "
                        + "not the platform default charset");
    }
}
