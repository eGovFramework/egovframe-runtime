package org.egovframe.rte.fdl.logging.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * 외부에서 받은 요청 ID 를 로그에 싣기 전에 형식 검증하는지 확인한다(로그 인젝션 방지, CWE-117).
 */
public class EgovRequestIdsSecurityTest {

    @Test
    public void 정상_형식은_그대로_승계한다() {
        assertEquals("req-1234.abc:9_Z", EgovRequestIds.acceptOrGenerate("req-1234.abc:9_Z"));
    }

    @Test
    public void 개행_제어문자_과대_길이는_새_ID_로_대체한다() {
        StringBuilder huge = new StringBuilder();
        for (int i = 0; i < 129; i++) {
            huge.append('a');
        }
        for (String hostile : new String[]{"id\r\nFAKE LOG LINE", "id x", "id\u0000", "<script>", "a b",
                huge.toString(), "", null}) {
            String accepted = EgovRequestIds.acceptOrGenerate(hostile);
            assertNotEquals(String.valueOf(hostile), hostile, accepted);
            assertTrue(accepted, EgovRequestIds.isAcceptable(accepted));
        }
    }

}
