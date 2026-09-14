package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EgovLineTokenizerEncodingTest {

    @Test
    void delimitedTokenizerReturnsTokensWithExplicitEncoding() throws Exception {
        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer();

        assertEquals(List.of("A", "B", ""), tokenizer.tokenize("A,B,", "UTF-8"));
        assertEquals(List.of("가", "나"), tokenizer.tokenize("가,나", "EUC-KR"));
    }

    @Test
    void escapableTokenizerPreservesQuotedDelimiter() throws Exception {
        EgovEscapableDelimitedLineTokenizer tokenizer = new EgovEscapableDelimitedLineTokenizer();

        assertEquals(List.of("\"A,B\"", "C"), tokenizer.tokenize("\"A,B\",C", "UTF-8"));
    }

    @Test
    void fixedLengthTokenizerUsesCharacterRanges() throws Exception {
        EgovFixedLengthTokenizer tokenizer = fixedLengthTokenizer();

        assertEquals(List.of("가나", "AB"), tokenizer.tokenize("가나AB", "UTF-8"));
        assertEquals(List.of("가나", "AB"), tokenizer.tokenize("가나AB", "EUC-KR"));
    }

    @Test
    void emptyAndNullDelimitedLinesKeepSingleArgumentBehavior() throws Exception {
        EgovAbstractLineTokenizer[] tokenizers = {
                new EgovDelimitedLineTokenizer(), new EgovEscapableDelimitedLineTokenizer()
        };
        for (EgovAbstractLineTokenizer tokenizer : tokenizers) {
            assertEquals(tokenizer.tokenize(""), tokenizer.tokenize("", "UTF-8"));
            assertEquals(tokenizer.tokenize(null), tokenizer.tokenize(null, "UTF-8"));
        }
    }

    @Test
    void fixedLengthTokenizerStillRejectsIncorrectLengths() {
        EgovFixedLengthTokenizer tokenizer = fixedLengthTokenizer();
        for (String line : new String[]{null, "", "ABC", "ABCDE"}) {
            assertThrows(IncorrectLineLengthException.class, () -> tokenizer.tokenize(line, "UTF-8"));
        }
    }

    @Test
    void byteTokenizerStillUsesExplicitUtf8Encoding() throws Exception {
        EgovFixedByteLengthTokenizer tokenizer = new EgovFixedByteLengthTokenizer();
        tokenizer.setByteEncoding("EUC-KR");
        tokenizer.setColumns(new Range[]{new Range(1, 3), new Range(4, 6)});

        assertEquals(List.of("가", "나"), tokenizer.tokenize("가나", "UTF-8"));
    }

    @Test
    void byteTokenizerStillUsesExplicitEucKrEncoding() throws Exception {
        EgovFixedByteLengthTokenizer tokenizer = new EgovFixedByteLengthTokenizer();
        tokenizer.setByteEncoding("UTF-8");
        tokenizer.setColumns(new Range[]{new Range(1, 2), new Range(3, 4)});

        assertEquals(List.of("가", "나"), tokenizer.tokenize("가나", "EUC-KR"));
    }

    private EgovFixedLengthTokenizer fixedLengthTokenizer() {
        EgovFixedLengthTokenizer tokenizer = new EgovFixedLengthTokenizer();
        tokenizer.setColumns(new Range[]{new Range(1, 2), new Range(3, 4)});
        return tokenizer;
    }
}
