package org.egovframe.rte.itl.webservice;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.egovframe.rte.itl.integration.EgovIntegrationMessageHeader;
import org.egovframe.rte.itl.integration.message.simple.SimpleMessageHeader;
import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class EgovWebServiceMessageHeaderNullResultCodeTest {

    /** SOAP 요청 헤더에 resultCode 가 없는 경우를 JAXB 언마샬로 재현한다. */
    private EgovWebServiceMessageHeader unmarshalRequestHeaderWithoutResultCode() throws Exception {
        String xml = "<egovWebServiceMessageHeader>"
                + "<integrationId>test</integrationId>"
                + "<providerOrganizationId>org0</providerOrganizationId>"
                + "</egovWebServiceMessageHeader>";
        JAXBContext ctx = JAXBContext.newInstance(EgovWebServiceMessageHeader.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        return unmarshaller.unmarshal(new StreamSource(new StringReader(xml)),
                EgovWebServiceMessageHeader.class).getValue();
    }

    @Test
    public void readSideToleratesMissingResultCode() throws Exception {
        EgovWebServiceMessageHeader requestHeader = unmarshalRequestHeaderWithoutResultCode();
        assertNotNull(requestHeader.getIntegrationId());
        assertNull(requestHeader.getResultCode());
        System.out.println("unmarshalled getResultCode() = " + requestHeader.getResultCode());
    }

    /** ServiceBridgeImpl.doService() 가 응답 헤더를 만들 때 쓰는 복사 생성자. */
    @Test
    public void copyConstructorAcceptsHeaderWithoutResultCode() throws Exception {
        EgovWebServiceMessageHeader requestHeader = unmarshalRequestHeaderWithoutResultCode();
        EgovWebServiceMessageHeader responseHeader = new EgovWebServiceMessageHeader(requestHeader);
        assertNull(responseHeader.getResultCode());
    }

    /** 형제 구현은 같은 복사 연산을 null 로 수행한다. */
    @Test
    public void siblingSimpleMessageHeaderCopiesNullResultCode() {
        SimpleMessageHeader src = new SimpleMessageHeader();
        EgovIntegrationMessageHeader copy = new SimpleMessageHeader(src);
        assertNull(copy.getResultCode());
        System.out.println("SimpleMessageHeader copy OK, resultCode = " + copy.getResultCode());
    }
}
