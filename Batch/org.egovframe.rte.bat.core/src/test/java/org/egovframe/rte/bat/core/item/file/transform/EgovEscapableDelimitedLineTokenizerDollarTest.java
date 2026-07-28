package org.egovframe.rte.bat.core.item.file.transform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * {@link EgovEscapableDelimitedLineTokenizer}가 {@code $}를 구분자로 사용할 때
 * 정규식 replacement의 그룹 참조 오용으로 IllegalArgumentException을 던지지 않는지 검증한다.
 */
class EgovEscapableDelimitedLineTokenizerDollarTest {

    @Test
    void setDelimiter_dollar_doesNotThrow() {
        EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer();
        // 수정 전에는 getRegexDelimiter()의 replaceAll("[$]","[$]")에서
        // IllegalArgumentException: Illegal group reference 가 발생했다.
        assertDoesNotThrow(() -> tokenizer.setDelimiter("$"),
                "$ 구분자 설정이 예외를 던지면 안 된다");
    }

    @Test
    void doTokenize_dollarDelimiter_splitsColumns() {
        EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer();
        tokenizer.setDelimiter("$");
        List<String> tokens = tokenizer.doTokenize("a$b$c");
        assertEquals(List.of("a", "b", "c"), tokens, "$ 구분자로 컬럼이 분리되어야 한다");
    }
}
