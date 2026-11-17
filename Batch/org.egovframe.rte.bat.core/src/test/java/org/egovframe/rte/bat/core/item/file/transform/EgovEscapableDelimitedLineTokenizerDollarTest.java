package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EgovEscapableDelimitedLineTokenizer}가 {@code $}를 구분자로 사용할 때
 * 정규식 replacement의 그룹 참조 오용으로 IllegalArgumentException을 던지지 않는지 검증한다.
 */
class EgovEscapableDelimitedLineTokenizerDollarTest {

    @Test
    void setDelimiter_dollar_doesNotThrow() {
        EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer();
        // 회귀 : getRegexDelimiter()의 replaceAll("[$]","[$]") → IllegalArgumentException(Illegal group reference)
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
