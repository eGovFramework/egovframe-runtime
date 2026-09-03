package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EgovEscapableDelimitedLineTokenizer}가 정규식 메타문자를 구분자로 쓸 때
 * 이스케이프를 거쳐 리터럴로 분리되는지 검증한다.
 * 같은 패키지의 {@link EgovDelimitedLineTokenizer}는 indexOf 로 분리하므로
 * 동일 입력에 대해 같은 결과가 나와야 한다.
 */
class EgovEscapableDelimitedLineTokenizerMetaCharTest {

    @Test
    void doTokenize_metaCharDelimiter_splitsColumns() {
        for (String delimiter : new String[]{"(", ")", "{", "}", "^", "[", "]"}) {
            EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer(delimiter);
            List<String> tokens = tokenizer.doTokenize("a" + delimiter + "b" + delimiter + "c");
            assertEquals(List.of("a", "b", "c"), tokens,
                    "[" + delimiter + "] 구분자로 컬럼이 분리되어야 한다");
        }
    }

    @Test
    void doTokenize_metaCharDelimiter_matchesDelimitedLineTokenizer() {
        for (String delimiter : new String[]{"(", ")", "{", "}", "^", "[", "]"}) {
            String line = "a" + delimiter + "b" + delimiter + "c";
            List<String> expected = new EgovDelimitedLineTokenizer(delimiter).doTokenize(line);
            List<String> actual = new EgovEscapableDelimitedLineTokenizer(delimiter).doTokenize(line);
            assertEquals(expected, actual,
                    "[" + delimiter + "] 구분자 분리 결과가 EgovDelimitedLineTokenizer 와 같아야 한다");
        }
    }
}
