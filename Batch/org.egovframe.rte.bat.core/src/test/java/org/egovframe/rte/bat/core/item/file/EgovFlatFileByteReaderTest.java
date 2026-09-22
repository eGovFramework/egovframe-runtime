package org.egovframe.rte.bat.core.item.file;

import org.egovframe.rte.bat.core.item.file.mapping.EgovByteLineMapper;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.support.CompositeItemStream;
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

    @Test
    void executionContextKeyMustNotCollideWithFlatFileItemReader() throws Exception {
        // TaskletStep 은 Step 에 등록된 ItemStream 들을 CompositeItemStream 하나로 묶어
        // 같은 ExecutionContext 에 재시작 상태를 저장한다.
        // 표준 FlatFileItemReader 가 함께 등록된 Step 을 재현한다.
        EgovFlatFileByteReader<String> byteReader = newByteReader();
        FlatFileItemReader<String> flatFileReader = newFlatFileReader();

        CompositeItemStream stepStreams = new CompositeItemStream();
        stepStreams.register(flatFileReader);
        stepStreams.register(byteReader);

        ExecutionContext executionContext = new ExecutionContext();
        stepStreams.open(executionContext);
        flatFileReader.read();
        byteReader.read();
        byteReader.read();
        byteReader.read();
        stepStreams.update(executionContext);
        stepStreams.close();

        // 재시작: 저장된 ExecutionContext 로 두 reader 를 다시 연다.
        EgovFlatFileByteReader<String> restartedByteReader = newByteReader();
        FlatFileItemReader<String> restartedFlatFileReader = newFlatFileReader();
        restartedByteReader.open(executionContext);
        restartedFlatFileReader.open(executionContext);
        try {
            assertEquals("DDDDD\r\n", restartedByteReader.read(),
                    "3건을 처리했으므로 4번째 레코드부터 다시 읽어야 한다");
            assertEquals("2", restartedFlatFileReader.read(),
                    "표준 FlatFileItemReader 는 1건만 처리했으므로 2번째 라인부터 다시 읽어야 한다 "
                            + "— EgovFlatFileByteReader 가 같은 키를 쓰면 처리하지 않은 라인이 건너뛰어진다");
        } finally {
            restartedByteReader.close();
            restartedFlatFileReader.close();
        }
    }

    private EgovFlatFileByteReader<String> newByteReader() {
        EgovFlatFileByteReader<String> reader = new EgovFlatFileByteReader<>();
        reader.setResource(new ByteArrayResource(
                "AAAAA\r\nBBBBB\r\nCCCCC\r\nDDDDD\r\n".getBytes(StandardCharsets.US_ASCII)));
        reader.setLineMapper(new EgovByteLineMapper<String>() {
            @Override
            public String mapLine(byte[] line, int lineNumber) {
                return new String(line, StandardCharsets.US_ASCII);
            }
        });
        reader.setLength(5);
        return reader;
    }

    private FlatFileItemReader<String> newFlatFileReader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<>();
        reader.setResource(new ByteArrayResource("1\n2\n3\n4\n".getBytes(StandardCharsets.US_ASCII)));
        reader.setLineMapper(new PassThroughLineMapper());
        return reader;
    }

}
