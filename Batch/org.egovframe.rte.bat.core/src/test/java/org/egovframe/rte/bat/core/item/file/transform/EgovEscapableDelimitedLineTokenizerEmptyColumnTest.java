package org.egovframe.rte.bat.core.item.file.transform;

import org.egovframe.rte.bat.core.item.file.mapping.EgovObjectMapper;
import org.egovframe.rte.bat.mapper.EmpVO2;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link EgovEscapableDelimitedLineTokenizer}가 CSV 후행 빈 컬럼을 보존하는지,
 * 따옴표 필드가 구분자로 끝나는 행을 정확히 자르는지,
 * 그리고 생성자만으로 구분자가 초기화되는지 검증한다.
 */
class EgovEscapableDelimitedLineTokenizerEmptyColumnTest {

    private final EgovEscapableDelimitedLineTokenizer tokenizer = createCommaTokenizer();

    private EgovEscapableDelimitedLineTokenizer createCommaTokenizer() {
        EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer();
        tokenizer.setDelimiter(",");
        return tokenizer;
    }

    @Test
    void doTokenize_trailingEmptyColumns_keepsColumns() {
        Object[][] cases = {
                {"a,b,c,d", List.of("a", "b", "c", "d")},
                {"a,b,,", List.of("a", "b", "", "")},
                {"a,,,", List.of("a", "", "", "")},
                {",,,", List.of("", "", "", "")},
                {"a,b,c,", List.of("a", "b", "c", "")},
                {"a,b,,d", List.of("a", "b", "", "d")}
        };

        for (Object[] testCase : cases) {
            String line = (String) testCase[0];
            @SuppressWarnings("unchecked")
            List<String> expected = (List<String>) testCase[1];
            assertEquals(expected, tokenizer.doTokenize(line), line + "의 빈 컬럼이 유지되어야 한다");
        }
    }

    @Test
    void doTokenize_trailingEmptyColumns_matchesSpringBatchTokenizer() throws Exception {
        DelimitedLineTokenizer springTokenizer = new DelimitedLineTokenizer();
        springTokenizer.setNames("col1", "col2", "col3", "col4");
        springTokenizer.afterPropertiesSet();

        List<String> lines = List.of("a,b,c,d", "a,b,,", "a,,,", ",,,", "a,b,c,", "a,b,,d");

        for (String line : lines) {
            List<String> expected = Arrays.asList(springTokenizer.tokenize(line).getValues());
            List<String> actual = tokenizer.doTokenize(line);

            assertEquals(expected.size(), actual.size(), line + "의 토큰 수가 Spring Batch 표준과 같아야 한다");
            assertEquals(expected, actual, line + "의 토큰 값이 Spring Batch 표준과 같아야 한다");
        }
    }

    @Test
    void doTokenize_trailingEmptyColumn_mapsWithEgovObjectMapper() {
        EgovObjectMapper<EmpVO2> mapper = new EgovObjectMapper<>();
        mapper.setType(EmpVO2.class);
        mapper.setNames(new String[]{"empNo", "empName", "job", "mgr"});
        mapper.afterPropertiesSet();

        List<String> tokens = tokenizer.doTokenize("a,b,c,");
        EmpVO2 vo = assertDoesNotThrow(() -> mapper.mapObject(tokens),
                "EgovObjectMapper는 names 4개와 토큰 4개가 일치하면 IncorrectTokenCountException을 던지면 안 된다");

        assertEquals("a", vo.getEmpNo());
        assertEquals("b", vo.getEmpName());
        assertEquals("c", vo.getJob());
        assertEquals("", vo.getMgr());
    }

    @Test
    void doTokenize_constructorInitializedDelimiter_doesNotThrow() {
        EgovEscapableDelimitedLineTokenizer commaTokenizer = new EgovEscapableDelimitedLineTokenizer();
        assertEquals(List.of("a", "b", "c"), commaTokenizer.doTokenize("a,b,c"),
                "기본 생성자 직후에도 콤마 구분자로 토큰화되어야 한다");

        EgovEscapableDelimitedLineTokenizer pipeTokenizer = new EgovEscapableDelimitedLineTokenizer("|");
        assertEquals(List.of("a", "b", "c"), pipeTokenizer.doTokenize("a|b|c"),
                "구분자 생성자 직후에도 지정 구분자로 토큰화되어야 한다");
    }

    @Test
    void doTokenize_escapeColumns_keepsEscapedDelimiterAndQuote() {
        assertEquals(List.of("\"customer5,c6\"", "90"), tokenizer.doTokenize("\"customer5,c6\",90"),
                "따옴표 안의 구분자는 컬럼을 나누면 안 된다");
        assertEquals(List.of("\"customer8 is \"\"Tom\"\"\"", "70"), tokenizer.doTokenize("\"customer8 is \"\"Tom\"\"\",70"),
                "따옴표 안의 이중 따옴표는 컬럼을 나누면 안 된다");
    }

    @Test
    void doTokenize_quotedTrailingDelimiter_keepsFollowingColumns() {
        Object[][] cases = {
                {"a,\"b,\",", List.of("a", "\"b,\"", "")},
                {"\"b,\",", List.of("\"b,\"", "")},
                {"\"a,b,\",", List.of("\"a,b,\"", "")},
                // 따옴표 필드가 구분자로 끝나도 뒤 컬럼을 삼키지 않는다.
                {"\"b,\",c", List.of("\"b,\"", "c")}
        };

        for (Object[] testCase : cases) {
            String line = (String) testCase[0];
            @SuppressWarnings("unchecked")
            List<String> expected = (List<String>) testCase[1];
            assertEquals(expected, tokenizer.doTokenize(line),
                    line + "의 따옴표 필드와 뒤 컬럼이 정확히 보존되어야 한다");
        }
    }
}
