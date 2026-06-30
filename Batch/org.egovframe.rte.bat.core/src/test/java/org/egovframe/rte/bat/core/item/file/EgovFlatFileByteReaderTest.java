package org.egovframe.rte.bat.core.item.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EgovFlatFileByteReaderTest {

    @Test
    public void testOsTypeIsIsolatedPerReader() {
        EgovFlatFileByteReader<Object> readerA = new EgovFlatFileByteReader<>();
        readerA.setOsType("UNIX");
        EgovFlatFileByteReader<Object> readerB = new EgovFlatFileByteReader<>();
        readerB.setOsType("WINDOWS");

        // Regression guard: the old shared static value was overwritten by reader B.
        assertEquals(1, readerA.getLineCrlf());
        assertEquals(2, readerB.getLineCrlf());
    }

    @Test
    public void testDefaultOsTypeIsWindows() {
        EgovFlatFileByteReader<Object> reader = new EgovFlatFileByteReader<>();

        assertEquals(2, reader.getLineCrlf());
    }

}
