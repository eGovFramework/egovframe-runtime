package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EgovFixedLengthTokenizer JUnit Test 클래스
 *
 * <p>Range 기반 고정길이 파싱(doTokenize)의 정상 분할, 라인 길이 경계
 * (maxRange 미만/일치/초과), open range, IncorrectLineLengthException 분기를 검증한다.</p>
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
public class EgovFixedLengthTokenizerTest {

    private EgovFixedLengthTokenizer newTokenizer(Range... ranges) {
        EgovFixedLengthTokenizer tokenizer = new EgovFixedLengthTokenizer();
        tokenizer.setColumns(ranges);
        return tokenizer;
    }

    @Test
    public void testDoTokenizeClosedRanges() {
        // maxRange = 10, open = false, 라인 길이가 정확히 maxRange 와 일치하는 정상 케이스
        EgovFixedLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6), new Range(7, 10));

        List<String> actual = tokenizer.doTokenize("ABCDEFGHIJ");

        assertEquals(Arrays.asList("ABC", "DEF", "GHIJ"), actual);
    }

    @Test
    public void testDoTokenizeLineLengthEqualsMaxRange() {
        // 라인 길이 == maxRange 경계값에서 정상 분할되는지 확인
        EgovFixedLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 2), new Range(3, 4));

        List<String> actual = tokenizer.doTokenize("WXYZ");

        assertEquals(Arrays.asList("WX", "YZ"), actual);
    }

    @Test
    public void testDoTokenizeOpenRangeTakesRemainder() {
        // 마지막 range 가 max 값이 없으면(open) 나머지 문자열을 모두 취한다.
        EgovFixedLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4));

        assertEquals(Arrays.asList("ABC", "DEFG"), tokenizer.doTokenize("ABCDEFG"));
        // open 이므로 maxRange 를 초과하는 더 긴 라인도 예외 없이 나머지를 취한다.
        assertEquals(Arrays.asList("ABC", "DEFGHIJKL"), tokenizer.doTokenize("ABCDEFGHIJKL"));
    }

    @Test
    public void testDoTokenizeThrowsWhenLineEmpty() {
        // 라인 길이 0 → IncorrectLineLengthException
        EgovFixedLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6));

        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.doTokenize(""));
    }

    @Test
    public void testDoTokenizeThrowsWhenLineShorterThanMaxRange() {
        // 라인 길이 < maxRange(10) → IncorrectLineLengthException
        EgovFixedLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6), new Range(7, 10));

        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.doTokenize("ABCDEFGHI"));
    }

    @Test
    public void testDoTokenizeThrowsWhenLineLongerThanMaxRange() {
        // open = false 이고 라인 길이 > maxRange(10) → IncorrectLineLengthException
        EgovFixedLengthTokenizer tokenizer =
                newTokenizer(new Range(1, 3), new Range(4, 6), new Range(7, 10));

        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.doTokenize("ABCDEFGHIJK"));
    }

}
