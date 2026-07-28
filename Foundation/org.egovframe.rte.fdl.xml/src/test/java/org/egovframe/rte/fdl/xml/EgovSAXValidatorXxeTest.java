package org.egovframe.rte.fdl.xml;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link EgovSAXValidatorService} 의 XXE(XML External Entity) 방어를 검증한다.
 *
 * <p>형제 클래스 {@code EgovDOMValidatorService} 와 달리 SAX 파서에는 외부 엔티티/외부 DTD
 * 차단이 적용되지 않아, 외부 엔티티를 참조하는 XML 입력 시 외부 리소스에 접근(XXE, CWE-611)할
 * 수 있었다. 외부 엔티티 차단이 적용되면 선언된 외부 엔티티를 해석하지 않으므로, 존재하지 않는
 * 파일을 가리키는 외부 엔티티가 있어도 파일 접근 시도(및 그로 인한 예외) 없이 파싱이 완료된다.</p>
 */
class EgovSAXValidatorXxeTest {

    @Test
    @DisplayName("외부 일반 엔티티는 해석되지 않아 외부 리소스에 접근하지 않는다")
    void externalGeneralEntityIsNotResolved() {
        // 외부 엔티티가 해석되면 존재하지 않는 파일에 접근해 예외가 발생한다.
        // 방어가 적용되면 엔티티를 해석하지 않아 예외 없이 정상(well-formed) 파싱된다.
        String xml = "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE root [ <!ENTITY xxe SYSTEM \"file:///nonexistent-xxe-probe-egovframe\"> ]>\n"
                + "<root>&xxe;</root>";

        EgovSAXValidatorService saxValidator = new EgovSAXValidatorService();
        saxValidator.setXML(xml);

        boolean wellFormed = assertDoesNotThrow(() -> saxValidator.parse(false));
        assertTrue(wellFormed, "외부 엔티티 차단 시 외부 리소스 접근 없이 well-formed 파싱되어야 한다");
    }
}
