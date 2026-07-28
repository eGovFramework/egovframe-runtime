package org.egovframe.rte.bat.core.item.file;

import org.egovframe.rte.bat.core.item.file.mapping.EgovByteLineMapper;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.ByteArrayResource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void readLine_returnsActualBytesRead_forShorterFinalRecord() throws Exception {
        // 첫 레코드는 length(5)+CRLF(2)=7바이트를 꽉 채우고, 마지막 레코드는 3바이트뿐인 상황을 재현한다.
        byte[] content = "AAAAA\r\nBB\n".getBytes(StandardCharsets.US_ASCII);

        List<byte[]> capturedLines = new ArrayList<>();
        EgovByteLineMapper<String> lineMapper = new EgovByteLineMapper<String>() {
            @Override
            public String mapLine(byte[] line, int lineNumber) {
                capturedLines.add(Arrays.copyOf(line, line.length));
                return new String(line, StandardCharsets.US_ASCII);
            }
        };

        EgovFlatFileByteReader<String> reader = new EgovFlatFileByteReader<>();
        reader.setResource(new ByteArrayResource(content));
        reader.setLineMapper(lineMapper);
        reader.setLength(5);

        reader.open(new ExecutionContext());
        try {
            String firstRecord = reader.read();
            String secondRecord = reader.read();
            String thirdRecord = reader.read();

            assertEquals("AAAAA\r\n", firstRecord);
            assertEquals("BB\n", secondRecord,
                    "짧은 마지막 레코드는 실제로 읽은 바이트만 반환해야 한다 — 재사용 버퍼에 남은 이전 레코드의 잔여 바이트가 섞이면 안 된다");
            assertNull(thirdRecord);

            assertEquals(3, capturedLines.get(1).length,
                    "두 번째(마지막) 레코드는 3바이트만 mapLine에 전달되어야 한다");
        } finally {
            reader.close();
        }
    }

}
