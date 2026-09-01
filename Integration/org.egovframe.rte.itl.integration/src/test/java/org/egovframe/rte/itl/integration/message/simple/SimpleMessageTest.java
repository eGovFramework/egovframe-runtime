package org.egovframe.rte.itl.integration.message.simple;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleMessageTest {

    @Test
    public void testSetBodyRejectsNullValue() {
        // SimpleMessage 생성자 javadoc: body의 value 값 중 null이 있으면 IllegalArgumentException.
        SimpleMessage message = new SimpleMessage();
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("key", null);

        assertThrows(IllegalArgumentException.class, () -> message.setBody(body));
    }

    @Test
    public void testSetAttachmentsRejectsNullValue() {
        // EgovIntegrationMessage#setAttachments의 javadoc: attachments의 value 값 중 null이 있으면 IllegalArgumentException.
        SimpleMessage message = new SimpleMessage();
        Map<String, Object> attachments = new HashMap<String, Object>();
        attachments.put("key", null);

        assertThrows(IllegalArgumentException.class, () -> message.setAttachments(attachments));
    }

    @Test
    public void testPutAttachmentRejectsNullValue() {
        // EgovIntegrationMessage#putAttachment의 javadoc: attachment 값이 null이면 IllegalArgumentException.
        SimpleMessage message = new SimpleMessage();

        assertThrows(IllegalArgumentException.class, () -> message.putAttachment("key", null));
    }

    @Test
    public void testSetBodyRejectsNullValueAmongMultipleEntries() {
        // 여러 entry 중 뒤쪽 하나만 value가 null이어도 검증을 통과해선 안 된다.
        SimpleMessage message = new SimpleMessage();
        Map<String, Object> body = new HashMap<String, Object>();
        body.put("first", "ok");
        body.put("second", null);

        assertThrows(IllegalArgumentException.class, () -> message.setBody(body));
    }

}
