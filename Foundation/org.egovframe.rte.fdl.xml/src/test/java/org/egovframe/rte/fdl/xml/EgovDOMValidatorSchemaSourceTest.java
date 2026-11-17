package org.egovframe.rte.fdl.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link EgovDOMValidatorService}가 스키마 등록 여부를 스키마 파일로 판단하는지 검증한다.
 *
 * <p>{@link EgovSAXValidatorService}와 같이 {@code getSCHEMAFile()} 기준이어야 함
 * ({@code getXMLFile()} 기준이면 XML 입력 형태에 따라 스키마 모드가 바뀜) : 파일·문자열 입력 결과 동일 여부로 확인</p>
 */
class EgovDOMValidatorSchemaSourceTest {

    private static final String XML = "<person><name>x</name></person>";

    private static final String XSD =
            "<?xml version=\"1.0\"?>\n"
                    + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "  <xs:element name=\"person\">\n"
                    + "    <xs:complexType><xs:sequence>\n"
                    + "      <xs:element name=\"name\" type=\"xs:string\"/>\n"
                    + "    </xs:sequence></xs:complexType>\n"
                    + "  </xs:element>\n"
                    + "</xs:schema>\n";

    /**
     * parse(true) 결과의 비교용 문자열 (정상 종료면 반환값, 예외면 예외 타입과 메시지).
     */
    private String outcome(EgovDOMValidatorService validator) {
        try {
            return "returned " + validator.parse(true);
        } catch (Exception e) {
            return e.getClass().getName() + ": " + e.getMessage();
        }
    }

    private Path write(Path dir, String name, String content) throws Exception {
        Path path = dir.resolve(name);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        return path;
    }

    @Test
    @DisplayName("스키마 파일이 없으면 XML을 파일로 줘도 스키마 검증이 켜지지 않는다")
    void schemaModeStaysOffWithoutSchemaFile(@TempDir Path tempDir) throws Exception {
        Path xmlFile = write(tempDir, "person.xml", XML);

        EgovDOMValidatorService fromFile = new EgovDOMValidatorService();
        fromFile.setXMLFile(xmlFile.toString());

        EgovDOMValidatorService fromString = new EgovDOMValidatorService();
        fromString.setXML(XML);

        String fileOutcome = outcome(fromFile);
        // cvc-* : XML Schema 검증 오류 코드 (스키마 미지정이므로 없어야 함)
        assertFalse(fileOutcome.contains("cvc-"), "스키마 없이 스키마 검증이 수행되었다: " + fileOutcome);
        assertEquals(outcome(fromString), fileOutcome);
    }

    @Test
    @DisplayName("스키마 파일을 주면 XML을 문자열로 줘도 스키마가 등록된다")
    void schemaFileIsRegisteredForStringXml(@TempDir Path tempDir) throws Exception {
        Path xmlFile = write(tempDir, "person.xml", XML);
        Path schemaFile = write(tempDir, "person.xsd", XSD);

        EgovDOMValidatorService fromFile = new EgovDOMValidatorService();
        fromFile.setXMLFile(xmlFile.toString());
        fromFile.setSCHEMAFile(schemaFile.toString());

        EgovDOMValidatorService fromString = new EgovDOMValidatorService();
        fromString.setXML(XML);
        fromString.setSCHEMAFile(schemaFile.toString());

        assertEquals(outcome(fromFile), outcome(fromString));
    }
}
