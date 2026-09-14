package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EgovDelimitedLineTokenizerMultiDelimiterTest {

    @Test
    void splitsMultiCharacterDelimiterFromConstructor() throws Exception {
        assertEquals(List.of("A", "B", "C"), new EgovDelimitedLineTokenizer("||").tokenize("A||B||C"));
    }

    @Test
    void splitsLongerDelimiterFromSetter() throws Exception {
        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer();
        tokenizer.setDelimiter("<->");
        assertEquals(List.of("가", "나", "다"), tokenizer.tokenize("가<->나<->다"));
    }

    @Test
    void preservesConsecutiveEmptyFields() throws Exception {
        assertEquals(List.of("A", "", "B"), new EgovDelimitedLineTokenizer("||").tokenize("A||||B"));
    }

    @Test
    void preservesLeadingAndTrailingEmptyFields() throws Exception {
        assertEquals(List.of("", "A", ""), new EgovDelimitedLineTokenizer("||").tokenize("||A||"));
    }

    @Test
    void splitsDelimiterOnlyLine() throws Exception {
        assertEquals(List.of("", ""), new EgovDelimitedLineTokenizer("||").tokenize("||"));
    }

    @Test
    void advancesPastDelimiterAfterQuotedField() throws Exception {
        assertEquals(List.of("A||B", "C"), new EgovDelimitedLineTokenizer("||").tokenize("\"A||B\"||C"));
    }

    @Test
    void advancesPastDelimiterBeforeQuotedField() throws Exception {
        assertEquals(List.of("A", "B||C", "D"), new EgovDelimitedLineTokenizer("||").tokenize("A||\"B||C\"||D"));
    }

    @Test
    void endsAfterQuotedFieldContainingDelimiter() throws Exception {
        assertEquals(List.of("A", "B||C"), new EgovDelimitedLineTokenizer("||").tokenize("A||\"B||C\""));
    }

    @Test
    void preservesLineWithoutDelimiter() throws Exception {
        assertEquals(List.of("ABC"), new EgovDelimitedLineTokenizer("||").tokenize("ABC"));
    }

    @Test
    void preservesSingleCharacterDelimiter() throws Exception {
        assertEquals(List.of("", "A", "", "B", ""), new EgovDelimitedLineTokenizer(",").tokenize(",A,,B,"));
    }

    @Test
    void handlesEmptyAndNullLines() throws Exception {
        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer("||");
        assertEquals(List.of(), tokenizer.tokenize(""));
        assertEquals(List.of(), tokenizer.tokenize(null));
    }

    @Test
    void rejectsEmptyOrNullDelimiterInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new EgovDelimitedLineTokenizer(""));
        assertThrows(IllegalArgumentException.class, () -> new EgovDelimitedLineTokenizer(null));
    }

    @Test
    void rejectsEmptyOrNullDelimiterInSetter() {
        EgovDelimitedLineTokenizer tokenizer = new EgovDelimitedLineTokenizer();
        assertThrows(IllegalArgumentException.class, () -> tokenizer.setDelimiter(""));
        assertThrows(IllegalArgumentException.class, () -> tokenizer.setDelimiter(null));
    }
}
