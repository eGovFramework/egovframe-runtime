package org.egovframe.rte.itl.integration.message.simple;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SimpleMessageTest {

    @Test
    public void testSetBodyRejectsNullValue() {
        // EgovIntegrationMessage#setBody의 javadoc: body의 value 값 중 null이 있으면 IllegalArgumentException.
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

}
