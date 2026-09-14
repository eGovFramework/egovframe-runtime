package org.egovframe.rte.fdl.logging.util;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovRequestIds 의 요청 식별자 승계 규칙(허용 문자·길이)을 경계값으로 검증한다.
 */
public class EgovRequestIdsTest {

    private static final Pattern UUID = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Test
    public void testWellFormedIdsAreAcceptedAsIs() {
        String[] accepted = {"0af7651916cd43dd8448eb211c80319c", "req-2026.09.10:1234", "a1b2-c3d4_e5", "x", "a".repeat(128)};
        for (String id : accepted) {
            assertTrue(EgovRequestIds.isAcceptable(id), id);
            assertEquals(id, EgovRequestIds.acceptOrGenerate(id));
        }
    }

    @Test
    public void testIdLongerThanLimitIsReplaced() {
        String id = "a".repeat(129);

        assertFalse(EgovRequestIds.isAcceptable(id));
        String generated = EgovRequestIds.acceptOrGenerate(id);
        assertNotEquals(id, generated);
        assertTrue(UUID.matcher(generated).matches(), generated);
    }

    @Test
    public void testHostileIdsAreReplaced() {
        String[] hostile = {"abc\r\nX-Injected: 1", "abc\u00a0evil", "abc\u200bevil", "abc\u2028evil", "abc\u0085evil",
                "abc\u0000", " abc", "abc ", "abc def", "abc\tdef", "\ud55c\uae00", "abc<script>", "a/b", "a=b", "a,b", "a;b", "a|b",
                "a%0d%0a"};
        for (String id : hostile) {
            assertFalse(EgovRequestIds.isAcceptable(id), id);
            String generated = EgovRequestIds.acceptOrGenerate(id);
            assertTrue(UUID.matcher(generated).matches(), generated);
        }
    }

    @Test
    public void testNullOrEmptyGeneratesFreshUuid() {
        assertTrue(UUID.matcher(EgovRequestIds.acceptOrGenerate(null)).matches());
        assertTrue(UUID.matcher(EgovRequestIds.acceptOrGenerate("")).matches());
        assertNotEquals(EgovRequestIds.acceptOrGenerate(null), EgovRequestIds.acceptOrGenerate(null));
    }

}
