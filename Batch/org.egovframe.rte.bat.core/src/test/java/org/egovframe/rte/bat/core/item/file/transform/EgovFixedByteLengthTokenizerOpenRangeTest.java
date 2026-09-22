package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.IncorrectLineLengthException;
import org.springframework.batch.item.file.transform.Range;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EgovFixedByteLengthTokenizerOpenRangeTest {

    @Test
    void readsEntireAsciiLineWithSingleOpenRange() throws Exception {
        assertEquals(List.of("ABCDE"), tokenizer(1).tokenize("ABCDE", "UTF-8"));
    }

    @Test
    void readsEntireUtf8LineWithSingleOpenRange() throws Exception {
        EgovFixedByteLengthTokenizer tokenizer = tokenizer(1);
        tokenizer.setByteEncoding("UTF-8");
        assertEquals(List.of("가나다"), tokenizer.tokenize("가나다"));
    }

    @Test
    void readsEntireEucKrLineWithSingleOpenRange() throws Exception {
        assertEquals(List.of("가나다"), tokenizer(1).tokenize("가나다", "EUC-KR"));
    }

    @Test
    void readsRemainderStartingAtByteOffset() throws Exception {
        assertEquals(List.of("나다"), tokenizer(4).tokenize("가나다", "UTF-8"));
    }

    @Test
    void preservesMinimumLengthValidation() throws Exception {
        EgovFixedByteLengthTokenizer tokenizer = tokenizer(3);
        assertEquals(List.of("C"), tokenizer.tokenize("ABC", "UTF-8"));
        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.tokenize("AB", "UTF-8"));
    }

    @Test
    void resetsOpenRangeStateWhenColumnsChange() throws Exception {
        EgovFixedByteLengthTokenizer tokenizer = tokenizer(1);
        assertEquals(List.of("ABCDE"), tokenizer.tokenize("ABCDE", "UTF-8"));
        tokenizer.setColumns(new Range[]{new Range(1, 3)});
        assertEquals(List.of("ABC"), tokenizer.tokenize("ABC", "UTF-8"));
        assertThrows(IncorrectLineLengthException.class, () -> tokenizer.tokenize("ABCDE", "UTF-8"));
        tokenizer.setColumns(new Range[]{new Range(1)});
        assertEquals(List.of("ABCDE"), tokenizer.tokenize("ABCDE", "UTF-8"));
    }

    private EgovFixedByteLengthTokenizer tokenizer(int firstByte) {
        EgovFixedByteLengthTokenizer tokenizer = new EgovFixedByteLengthTokenizer();
        tokenizer.setColumns(new Range[]{new Range(firstByte)});
        return tokenizer;
    }
}
