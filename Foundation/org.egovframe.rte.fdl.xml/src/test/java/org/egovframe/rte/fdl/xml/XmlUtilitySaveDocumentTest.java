package org.egovframe.rte.fdl.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractXMLUtility}의 XML 저장이 OutputStream을 누수 없이 처리하고,
 * 반복 저장에서도 정상 동작하는지 검증한다.
 *
 * <p>스트림 미종료 시 반복 저장에서 FD 누적·Windows 파일 잠금 발생 (try-with-resources 적용 확인 특성 테스트)</p>
 */
class XmlUtilitySaveDocumentTest {

    private Document newSimpleDocument() throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createElement("root"));
        return doc;
    }

    private void saveDocument(AbstractXMLUtility util, Document doc, String path) throws Throwable {
        Method m = AbstractXMLUtility.class.getDeclaredMethod("saveDocument", Document.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(util, doc, path);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    @DisplayName("동일 경로로 반복 저장해도 매번 유효한 XML이 기록된다(스트림 누수 없음)")
    void repeatedSaveWritesValidXml(@TempDir Path tempDir) throws Throwable {
        EgovDOMValidatorService util = new EgovDOMValidatorService();
        Path target = tempDir.resolve("out.xml");

        // 반복 저장 : 스트림 미종료 시 FD 누적·잠금 잔존
        for (int i = 0; i < 50; i++) {
            saveDocument(util, newSimpleDocument(), target.toString());
        }

        assertTrue(Files.exists(target));
        // 마지막 저장 결과가 잘 형성된(well-formed) XML로 재파싱되는지 확인
        Document reparsed = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(target.toFile());
        assertEquals("root", reparsed.getDocumentElement().getNodeName());
    }
}
