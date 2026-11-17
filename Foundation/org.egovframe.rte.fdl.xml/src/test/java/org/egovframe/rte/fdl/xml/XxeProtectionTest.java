package org.egovframe.rte.fdl.xml;

import org.junit.Test;
import org.xml.sax.SAXException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * DOCTYPE/외부 엔티티가 포함된 XML(XXE)이 DOM/SAX 파서 양쪽에서 거부되는지 확인한다.
 */
public class XxeProtectionTest {

    private static final String XXE = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/hostname\">]>"
            + "<foo>&xxe;</foo>";

    private static final String PLAIN = "<?xml version=\"1.0\"?><foo>bar</foo>";

    private void assertRejected(AbstractXMLUtility service) throws Exception {
        service.setXML(XXE);
        try {
            assertFalse(service.parse(false));
        } catch (SAXException | RuntimeException expected) {
            // DOCTYPE 거부(SAXException)도 정상 동작이다.
        }
    }

    @Test
    public void domRejectsDoctype() throws Exception {
        assertRejected(new EgovDOMValidatorService());
    }

    @Test
    public void saxRejectsDoctype() throws Exception {
        assertRejected(new EgovSAXValidatorService());
    }

    @Test
    public void wellFormedXmlStillParses() throws Exception {
        EgovDOMValidatorService dom = new EgovDOMValidatorService();
        dom.setXML(PLAIN);
        assertTrue(dom.parse(false));
        EgovSAXValidatorService sax = new EgovSAXValidatorService();
        sax.setXML(PLAIN);
        assertTrue(sax.parse(false));
    }
}
