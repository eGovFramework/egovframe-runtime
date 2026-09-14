package org.egovframe.rte.fdl.xml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 저장 경로를 직접 주입하는 생성자가 설정 파일·Spring 컨텍스트 없이 동작하는지 검증한다.
 * 기본 생성자(egovxmlCfg.xml 에서 경로를 읽고 컨텍스트를 바로 닫는 경로)는 기존 테스트가 회귀 검증한다.
 */
public class AbstractXMLUtilityLifecycleTest {

    @TempDir
    Path tempDir;

    /** parse 만 구현한 최소 서브클래스 — 경로 주입 생성자 검증용 */
    private static final class PathInjectedUtility extends AbstractXMLUtility {
        private PathInjectedUtility(String savedPath) {
            super(savedPath);
        }

        @Override
        public boolean parse(boolean isValid) {
            return true;
        }
    }

    @Test
    public void testPathInjectedConstructorSavesUnderInjectedPathWithoutContext() throws Exception {
        String savedPath = tempDir.toString() + File.separator;
        PathInjectedUtility utility = new PathInjectedUtility(savedPath);

        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        utility.createNewXML(doc, "root",
                List.of(new SharedObject("name", "전자정부"), new SharedObject("layer", "fdl.xml")), null);

        Path saved = tempDir.resolve("newXML.xml"); // 경로 미지정 시 savedPath + newXML.xml
        assertTrue(Files.exists(saved), "주입한 경로 아래에 저장되어야 한다");
        String content = Files.readString(saved, StandardCharsets.UTF_8);
        assertTrue(content.contains("<name>전자정부</name>"), content);
        assertTrue(content.contains("<layer>fdl.xml</layer>"), content);
    }

    @Test
    public void testValidatorServicesOfferPathInjectedConstructor() throws Exception {
        String savedPath = tempDir.toString() + File.separator;
        EgovDOMValidatorService dom = new EgovDOMValidatorService(savedPath);
        EgovSAXValidatorService sax = new EgovSAXValidatorService(savedPath);

        dom.setXML("<a/>");
        assertEquals("<a/>", dom.getXML());
        assertTrue(dom.parse(false), "경로 주입 생성자로 만든 DOM 검증기가 파싱해야 한다");

        sax.setXML("<b/>");
        assertEquals("<b/>", sax.getXML());
        assertTrue(sax.parse(false), "경로 주입 생성자로 만든 SAX 검증기가 파싱해야 한다");
    }

    @Test
    public void testDefaultConstructorStillReadsConfiguredPath() throws Exception {
        // 설정 파일 경로로 만든 인스턴스도 종전처럼 동작한다(컨텍스트는 읽은 뒤 바로 닫힌다)
        EgovDOMValidatorService dom = new EgovDOMValidatorService();
        dom.setXML("<c/>");
        assertTrue(dom.parse(false));
    }
}
