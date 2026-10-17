package org.egovframe.rte.fdl.crypto.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.codec.binary.Hex;
import org.egovframe.rte.fdl.crypto.impl.aria.ARIAEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/*
 * ARIA Engine이 정상적으로 구현되었는지 검증하기 위한 벡터 테스트
 * 
 * 벡터 테스트는 KISA에서 제공한 벡터 파일을 이용하여 검증함
 * 참조: https://seed.kisa.or.kr/kisa/kcmvp/EgovVerification.do
 * 
 * 벡터 테스트를 위한 벡터 파일은 'src/test/resources/vector'에 위치함
 */
public class ARIAEngineTest {
    private static final int[] KEY_LENGTH = {128, 192, 256};

    /*
     * KAT(Known Answer Test) 검증
     * 
     * 정답이 이미 알려진 입력으로 암호 알고리즘이 정상적으로 구현되었는지 검증하는 테스트.
     * 키(KEY)를 이용해, 평문(PT)을 암호화 하고 기대 암호문(CT)과 동일한지 비교합니다.
     */
    @TestFactory 
    Stream<DynamicTest> ecbKAT() {
        List<DynamicTest> tests = new ArrayList<>();
        for (int keyLength : KEY_LENGTH) {

            // KAT 벡터 파일 읽기
            File file = new File("src/test/resources/vector/ARIA-" + keyLength + "_(ECB)_KAT.txt");
            List<String> lines;
            try {
                lines = java.nio.file.Files.readAllLines(file.toPath());
            } catch (Exception ex) {
                continue;
            }

            String key = "";
            String pt = "";
            String ct;
            int caseNum = 1;
            for (String line : lines) {
                line = line.trim();
                String trim = line.substring(line.indexOf('=') + 1).trim();

                // 벡터 파일에서 KEY, PT, CT 값 읽기
                if (line.startsWith("KEY =")) key = trim;
                else if (line.startsWith("PT =")) pt = trim;
                else if (line.startsWith("CT =")) {
                    ct = trim;
                    String testName = "ARIA-" + keyLength + "-ECB KAT #" + caseNum;
                    String finalKey = key;
                    String finalPt = pt;
                    String finalCt = ct;

                    tests.add(DynamicTest.dynamicTest(testName, () -> {
                        ARIAEngine engine = new ARIAEngine(keyLength);
                        byte[] sbuffer = Hex.decodeHex(finalPt);
                        byte[] szKey = Hex.decodeHex(finalKey);

                        // szKey: KEY, sbuffer: PT
                        byte[] result = engine.encrypt(sbuffer, szKey);
                        String resultHex = Hex.encodeHexString(result, false);

                        // 파일에서 읽어온 CT 값과, 암호화 결과물인 resultHex값이 동일한지 비교함
                        Assertions.assertEquals(finalCt.toUpperCase(), resultHex, "Failed at " + testName);
                    }));

                    caseNum++;
                }
            }
        }

        return tests.stream();
    }

    /*
     * MCT(Monte Carlo Test) 검증
     * 
     * 여러 번 연쇄하여 암호화 해도 결과가 맞는지 검증하는 테스트.
     * 각 차수(COUNT)별로 평문(PT)을 키(KEY)로 1,000번 암호화 한 결과(CT)를, 다음 차수의 평문으로 사용합니다.
     * (* 블록 암호 MCT는 일반적으로 암호화 횟수를 명시적으로 지정하지 않은 경우 차수별로 1,000번 반복이 표준임.)
     * 
     * 즉, COUNT=99의 1,000번 반복 암호화 최종 결과는 COUNT=0의 평문 값을 키로 100,000번 암호화 한것과 동일한 결과가 나와야 함.
     */
    @TestFactory 
    Stream<DynamicTest> ecbMCT() throws Exception {
        List<DynamicTest> tests = new ArrayList<>();
        for (int keyLength : KEY_LENGTH) {

            // MCT 벡터 파일 읽기
            File file = new File("src/test/resources/vector/ARIA-" + keyLength + "_(ECB)_MCT.txt");
            List<String> lines;
            try {
                lines = java.nio.file.Files.readAllLines(file.toPath());
            } catch (Exception ex) {
                continue;
            }

            // 벡터 파일에서 COUNT별 KEY, PT, CT 값 읽기
            List<String[]> records = new ArrayList<>();
            String key = "";
            String pt = "";
            for (String line : lines) {
                line = line.trim();
                String trim = line.substring(line.indexOf('=') + 1).trim();
                if (line.startsWith("KEY =")) key = trim;
                else if (line.startsWith("PT =")) pt = trim;
                else if (line.startsWith("CT =")) records.add(new String[] {key, pt, trim});
            }
            
            // 0 ~ 99 차수 반복
            for (int count = 0; count < records.size(); count++) {
                String[] testCase = records.get(count);
                String[] next = count + 1 < records.size() ? records.get(count + 1) : null;
                String testName = "ARIA-" + keyLength + "-ECB MCT COUNT " + count;

                tests.add(DynamicTest.dynamicTest(testName, () -> {
                    ARIAEngine engine = new ARIAEngine(keyLength);
                    byte[] szKey = Hex.decodeHex(testCase[0]);
                    byte[] ct = Hex.decodeHex(testCase[1]);
                    byte[] prevCt = ct;

                    // 직전 CT를 다음 PT로 사용하여 1,000회 연쇄 암호화
                    for (int i = 0; i < 1000; i++) {
                        prevCt = ct;
                        ct = engine.encrypt(ct, szKey);
                    }

                    // 1000번째 암호화 결과가 파일의 CT 값과 동일한지 비교함
                    Assertions.assertEquals(testCase[2].toUpperCase(), Hex.encodeHexString(ct, false), "Failed at " + testName);

                    // 다음 COUNT의 KEY, PT가 연쇄 규칙대로 만들어지는지 비교함
                    if (next != null) {
                        byte[] nextKey = nextMctKey(szKey, prevCt, ct);
                        Assertions.assertEquals(next[0].toUpperCase(), Hex.encodeHexString(nextKey, false), "Next KEY mismatch at " + testName);
                        Assertions.assertEquals(next[1].toUpperCase(), Hex.encodeHexString(ct, false), "Next PT mismatch at " + testName);
                    }
                }));
            }
        }

        return tests.stream();
    }

    /*
     * MMT(Multi-block Message Test) 검증
     *
     * 여러 블록으로 이루어진 메시지를 암호화 해도 결과가 맞는지 검증하는 테스트.
     * 키(KEY)를 이용해, 1~10 블록 길이의 평문(PT)을 ECB 모드로 암호화 하고 기대 암호문(CT)과 동일한지 비교합니다.
     */
    @TestFactory
    Stream<DynamicTest> ecbMMT() throws Exception {
        List<DynamicTest> tests = new ArrayList<>();
        for (int keyLength : KEY_LENGTH) {

            // MMT 벡터 파일 읽기
            File file = new File("src/test/resources/vector/ARIA-" + keyLength + "_(ECB)_MMT.txt");
            List<String> lines;
            try {
                lines = java.nio.file.Files.readAllLines(file.toPath());
            } catch (Exception ex) {
                continue;
            }

            String key = "";
            String pt = "";
            int caseNum = 1;
            for (String line : lines) {
                line = line.trim();
                String trim = line.substring(line.indexOf('=') + 1).trim();

                // 벡터 파일에서 KEY, PT, CT 값 읽기
                if (line.startsWith("KEY =")) key = trim;
                else if (line.startsWith("PT =")) pt = trim;
                else if (line.startsWith("CT =")) {
                    String testName = "ARIA-" + keyLength + "-ECB MMT #" + caseNum;
                    String finalKey = key;
                    String finalPt = pt;
                    String finalCt = trim;

                    tests.add(DynamicTest.dynamicTest(testName, () -> {
                        ARIAEngine engine = new ARIAEngine(keyLength);
                        byte[] sbuffer = Hex.decodeHex(finalPt);
                        byte[] szKey = Hex.decodeHex(finalKey);

                        // 평문 길이가 블록 크기(16바이트)의 배수이므로 패딩 없이 블록 단위로 암호화됨
                        byte[] result = engine.encrypt(sbuffer, szKey);
                        String resultHex = Hex.encodeHexString(result, false);

                        // 파일에서 읽어온 CT 값과, 암호화 결과물인 resultHex값이 동일한지 비교함
                        Assertions.assertEquals(finalCt.toUpperCase(), resultHex, "Failed at " + testName);
                    }));

                    caseNum++;
                }
            }
        }

        return tests.stream();
    }

    private static byte[] nextMctKey(byte[] key, byte[] prevCt, byte[] ct) {
        // CT[998] || CT[999]의 하위 key.length 바이트를 KEY와 XOR
        byte[] concat = new byte[prevCt.length + ct.length];
        System.arraycopy(prevCt, 0, concat, 0, prevCt.length);
        System.arraycopy(ct, 0, concat, prevCt.length, ct.length);

        byte[] nextKey = new byte[key.length];
        int offset = concat.length - key.length;
        for (int i = 0; i < key.length; i++) {
            nextKey[i] = (byte) (key[i] ^ concat[offset + i]);
        }

        return nextKey;
    }
}
