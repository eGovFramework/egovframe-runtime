package org.egovframe.rte.fdl.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * updTextElement(doc, list, path) 가 텍스트가 SharedObject 키와 같은 엘리먼트를 값으로 바꾸고,
 * 나머지 엘리먼트는 그대로 두는지 검증한다.
 */
public class UpdTextNodeTest {

    @TempDir
    Path tempDir;

    @Test
    public void testUpdTextElementReplacesTextEqualToKey() throws Exception {
        String savedPath = tempDir.toString() + File.separator;
        EgovDOMValidatorService service = new EgovDOMValidatorService(savedPath);

        String xml = "<root>\n  <name>old</name>\n  <pet>dog</pet>\n</root>";
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        String outPath = savedPath + "out.xml";
        service.updTextElement(doc, List.of(new SharedObject("old", "new")), outPath);

        String content = Files.readString(Path.of(outPath), StandardCharsets.UTF_8);
        assertTrue(content.contains("<name>new</name>"),
                "텍스트가 키(old)와 같은 엘리먼트는 값(new)으로 바뀌어야 한다: " + content);
        assertTrue(content.contains("<pet>dog</pet>"),
                "키와 텍스트가 다른 엘리먼트는 그대로여야 한다: " + content);
    }
}
