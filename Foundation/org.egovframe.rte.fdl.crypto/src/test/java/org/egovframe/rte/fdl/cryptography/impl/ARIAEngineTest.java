package org.egovframe.rte.fdl.cryptography.impl;

import org.apache.commons.codec.binary.Hex;
import org.egovframe.rte.fdl.cryptography.impl.aria.ARIAEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * ARIA Engine이 정상적으로 구현되었는지 검증하기 위한 벡터 테스트 (PR #492를 JUnit 4 / Java 8 용으로 이식)
 *
 * 벡터 테스트는 KISA에서 제공한 벡터 파일을 이용하여 검증함 (엔진에 대한 검증이므로 ECB 방식만 테스트)
 * 참조: https://seed.kisa.or.kr/kisa/kcmvp/EgovVerification.do
 *
 * 벡터 파일은 classpath 의 'vector' 디렉터리(src/test/resources/vector)에 위치하며,
 * 파일이 없거나 비어 있으면 테스트가 조용히 통과하지 않도록 실패 처리한다.
 */
@RunWith(Parameterized.class)
public class ARIAEngineTest {

    private static final int[] KEY_LENGTH = {128, 192, 256};

    private static final String KAT = "KAT";
    private static final String MCT = "MCT";
    private static final String MMT = "MMT";

    private final String type;
    private final int keyLength;
    private final String key;
    private final String pt;
    private final String ct;
    // MCT 전용: 다음 차수의 KEY, PT (마지막 차수는 null)
    private final String nextKey;
    private final String nextPt;

    public ARIAEngineTest(String name, String type, int keyLength, String key, String pt, String ct, String nextKey, String nextPt) {
        this.type = type;
        this.keyLength = keyLength;
        this.key = key;
        this.pt = pt;
        this.ct = ct;
        this.nextKey = nextKey;
        this.nextPt = nextPt;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> vectors() throws IOException {
        List<Object[]> params = new ArrayList<Object[]>();
        for (int keyLength : KEY_LENGTH) {
            for (String type : new String[] {KAT, MCT, MMT}) {
                List<String[]> records = readVector("/vector/ARIA-" + keyLength + "_(ECB)_" + type + ".txt");
                for (int i = 0; i < records.size(); i++) {
                    String[] r = records.get(i);
                    String[] next = MCT.equals(type) && i + 1 < records.size() ? records.get(i + 1) : null;
                    String name = "ARIA-" + keyLength + "-ECB " + type + " #" + (MCT.equals(type) ? i : i + 1);
                    params.add(new Object[] {name, type, keyLength, r[0], r[1], r[2],
                            next == null ? null : next[0], next == null ? null : next[1]});
                }
            }
        }
        return params;
    }

    /**
     * 벡터 파일에서 KEY, PT, CT 값을 순서대로 읽는다.
     */
    private static List<String[]> readVector(String resource) throws IOException {
        InputStream in = ARIAEngineTest.class.getResourceAsStream(resource);
        assertNotNull("벡터 파일이 없습니다: " + resource, in);
        List<String[]> records = new ArrayList<String[]>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String key = "";
            String pt = "";
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                String value = line.substring(line.indexOf('=') + 1).trim();
                if (line.startsWith("KEY =")) key = value;
                else if (line.startsWith("PT =")) pt = value;
                else if (line.startsWith("CT =")) records.add(new String[] {key, pt, value});
            }
        }
        assertFalse("벡터 파일에 테스트 케이스가 없습니다: " + resource, records.isEmpty());
        return records;
    }

    @Test
    public void vector() throws Exception {
        if (MCT.equals(type)) {
            verifyMCT();
        } else {
            verifyKATorMMT();
        }
    }

    /*
     * KAT(Known Answer Test) / MMT(Multi-block Message Test) 검증
     *
     * 키(KEY)를 이용해 평문(PT)을 암호화 하고 기대 암호문(CT)과 동일한지 비교한다.
     * MMT 평문은 1~10 블록 길이이며 블록 크기(16바이트)의 배수이므로 패딩 없이 블록 단위로 암호화된다.
     */
    private void verifyKATorMMT() throws Exception {
        ARIAEngine engine = new ARIAEngine(keyLength);
        byte[] result = engine.encrypt(Hex.decodeHex(pt), Hex.decodeHex(key));
        assertEquals(ct.toUpperCase(), Hex.encodeHexString(result, false));
    }

    /*
     * MCT(Monte Carlo Test) 검증
     *
     * 각 차수(COUNT)별로 평문(PT)을 키(KEY)로 1,000번 연쇄 암호화 한 결과가 CT와 같은지,
     * 다음 차수의 KEY(CT[998]||CT[999] 하위 바이트와 XOR), PT(CT[999])가 연쇄 규칙대로 만들어지는지 비교한다.
     */
    private void verifyMCT() throws Exception {
        ARIAEngine engine = new ARIAEngine(keyLength);
        byte[] szKey = Hex.decodeHex(key);
        byte[] cur = Hex.decodeHex(pt);
        byte[] prev = cur;
        for (int i = 0; i < 1000; i++) {
            prev = cur;
            cur = engine.encrypt(cur, szKey);
        }
        assertEquals(ct.toUpperCase(), Hex.encodeHexString(cur, false));
        if (nextKey != null) {
            assertEquals("Next KEY mismatch", nextKey.toUpperCase(), Hex.encodeHexString(nextMctKey(szKey, prev, cur), false));
            assertEquals("Next PT mismatch", nextPt.toUpperCase(), Hex.encodeHexString(cur, false));
        }
    }

    private static byte[] nextMctKey(byte[] key, byte[] prevCt, byte[] ct) {
        // CT[998] || CT[999]의 하위 key.length 바이트를 KEY와 XOR
        byte[] concat = new byte[prevCt.length + ct.length];
        System.arraycopy(prevCt, 0, concat, 0, prevCt.length);
        System.arraycopy(ct, 0, concat, prevCt.length, ct.length);
        byte[] next = new byte[key.length];
        int offset = concat.length - key.length;
        for (int i = 0; i < key.length; i++) {
            next[i] = (byte) (key[i] ^ concat[offset + i]);
        }
        return next;
    }

}
