package org.egovframe.rte.itl.integration.message.typed;

import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.egovframe.rte.itl.integration.type.RecordType;
import org.egovframe.rte.itl.integration.type.Type;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TypedMessageTest {

    private static final RecordType bodyType = new RecordType("recordA", "RecordA", new HashMap<String, Type>() {
        /**
         *  serialVersion UID
         */
        private static final long serialVersionUID = 1L;

        {
            put("stringValue", PrimitiveType.STRING);
        }
    });

    @Test
    public void testSetAttachmentsRejectsNullValue() {
        // EgovIntegrationMessage#setAttachments의 javadoc: attachments의 value 값 중 null이 있으면 IllegalArgumentException.
        TypedMessage message = new TypedMessage(bodyType);
        Map<String, Object> attachments = new HashMap<String, Object>();
        attachments.put("key", null);

        assertThrows(IllegalArgumentException.class, () -> message.setAttachments(attachments));
    }

    @Test
    public void testSetAttachmentsRejectsNullValueAmongMultipleEntries() {
        // 여러 entry 중 뒤쪽 하나만 value가 null이어도 검증을 통과해선 안 된다.
        TypedMessage message = new TypedMessage(bodyType);
        Map<String, Object> attachments = new HashMap<String, Object>();
        attachments.put("first", "ok");
        attachments.put("second", null);

        assertThrows(IllegalArgumentException.class, () -> message.setAttachments(attachments));
    }

    @Test
    public void testPutAttachmentRejectsNullValue() {
        // EgovIntegrationMessage#putAttachment의 javadoc: attachment 값이 null이면 IllegalArgumentException.
        TypedMessage message = new TypedMessage(bodyType);

        assertThrows(IllegalArgumentException.class, () -> message.putAttachment("key", null));
    }

}
