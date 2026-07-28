package org.egovframe.rte.bat.core.item.file.transform;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.file.transform.Range;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EgovFixedByteTokenizerTest {

    @Test
    public void testDefaultWindowsLineCrlf() throws Exception {
        EgovFixedByteTokenizer tokenizer = createTokenizer();
        byte[] line = "ABCDEF\r\n".getBytes(StandardCharsets.US_ASCII);

        assertEquals(Arrays.asList("ABC", "DEF"), tokenizer.doTokenize(line, StandardCharsets.US_ASCII.name()));
    }

    @Test
    public void testUnixLineCrlf() throws Exception {
        EgovFixedByteTokenizer tokenizer = createTokenizer();
        tokenizer.setOsType("UNIX");
        byte[] line = "ABCDEF\n".getBytes(StandardCharsets.US_ASCII);

        assertEquals(Arrays.asList("ABC", "DEF"), tokenizer.doTokenize(line, StandardCharsets.US_ASCII.name()));
    }

    private EgovFixedByteTokenizer createTokenizer() {
        EgovFixedByteTokenizer tokenizer = new EgovFixedByteTokenizer();
        tokenizer.setColumns(new Range[]{new Range(1, 3), new Range(4, 6)});
        return tokenizer;
    }

}
