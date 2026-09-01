package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EgovFixedByteLengthTokenizer JUnit Test 클래스
 *
 * <p>인코딩을 적용한 byte 기준 고정길이 절단(doTokenize)을 검증한다.
 * ASCII 정상 분할, UTF-8/EUC-KR 멀티바이트(한글) 경계, open range,
 * 라인 길이 경계(byte 길이 0/미만/초과)의 IncorrectLineLengthException 분기를 포함한다.</p>
 *
 * @version 1.0
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일        수정자           수정내용
 * -------      -------------  ----------------------
 * 2026.07.28  eGovFrame        최초 생성
 * @since 2026.07.28
 */
public class EgovFixedByteLengthTokenizerTest {

    private EgovFixedByteLengthTokenizer newTokenizer(Range... ranges) {
        EgovFixedByteLengthTokenizer tokenizer = new EgovFixedByteLengthTokenizer();
        tokenizer.setColumns(ranges);
        return tokenizer;
    }

    @Test
    public void testDoTokenizeAsciiWithExplicitEncoding() throws Exception {
        // ASCII 는 UTF-8 에서 1byte 이므로 byte 절단이 문자 절단과 동일하다.
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6), new Range(7, 10));

        List<String> actual = tokenizer.doTokenize("ABCDEFGHIJ", "UTF-8");

        assertEquals(Arrays.asList("ABC", "DEF", "GHIJ"), actual);
    }

    @Test
    public void testDoTokenizeAsciiWithDefaultEncoding() throws Exception {
        // 인코딩 미지정 시 byteEncoding(기본 charset)이 사용된다. ASCII 라 charset 에 무관하게 안전하다.
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 2), new Range(3, 4));

        List<String> actual = tokenizer.doTokenize("WXYZ");

        assertEquals(Arrays.asList("WX", "YZ"), actual);
    }

    @Test
    public void testDoTokenizeUtf8KoreanBoundary() throws Exception {
        // UTF-8 에서 한글은 각 3byte. "가나다" = 9byte, range 도 byte 기준으로 지정한다.
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6), new Range(7, 9));

        List<String> actual = tokenizer.doTokenize("가나다", "UTF-8");

        assertEquals(Arrays.asList("가", "나", "다"), actual);
    }

    @Test
    public void testDoTokenizeEucKrKoreanBoundary() throws Exception {
        // EUC-KR 에서 한글은 각 2byte. "가나다" = 6byte, range 도 byte 기준으로 지정한다.
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 2), new Range(3, 4), new Range(5, 6));

        List<String> actual = tokenizer.doTokenize("가나다", "EUC-KR");

        assertEquals(Arrays.asList("가", "나", "다"), actual);
    }

    @Test
    public void testDoTokenizeOpenRangeTakesRemainderBytes() throws Exception {
        // 마지막 range 가 open 이면 나머지 byte 를 인코딩에 맞춰 취한다. (EUC-KR 2byte/한글)
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 2), new Range(3));

        List<String> actual = tokenizer.doTokenize("가나다", "EUC-KR");

        assertEquals(Arrays.asList("가", "나다"), actual);
    }

    @Test
    public void testDoTokenizeThrowsWhenLineEmpty() {
        // byte 길이 0 → IncorrectLineLengthException
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6));

        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.doTokenize("", "UTF-8"));
    }

    @Test
    public void testDoTokenizeThrowsWhenShorterThanMaxRange() {
        // byte 길이 < maxRange(9) → IncorrectLineLengthException
        // "가나" UTF-8 = 6byte < 9
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6), new Range(7, 9));

        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.doTokenize("가나", "UTF-8"));
    }

    @Test
    public void testDoTokenizeThrowsWhenLongerThanMaxRange() {
        // open = false 이고 byte 길이 > maxRange(6) → IncorrectLineLengthException
        // "가나다" UTF-8 = 9byte > 6
        EgovFixedByteLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6));

        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.doTokenize("가나다", "UTF-8"));
    }

}
