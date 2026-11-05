package org.egovframe.rte.psl.orm.ibatis.support;

import org.junit.Test;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.ResultSet;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * BLOB 역직렬화 allowlist(CWE-502 방어) 동작 확인.
 */
public class BlobSerializableTypeHandlerTest {

    static class Denied implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    static class Registered implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    private Object read(Object value) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(value);
        }
        final byte[] bytes = baos.toByteArray();
        DefaultLobHandler lobHandler = new DefaultLobHandler() {
            @Override
            public java.io.InputStream getBlobAsBinaryStream(ResultSet rs, int columnIndex) {
                return new ByteArrayInputStream(bytes);
            }
        };
        BlobSerializableTypeHandler handler = new BlobSerializableTypeHandler(lobHandler);
        ResultSet rs = createNiceMock(ResultSet.class);
        replay(rs);
        return handler.getResult(rs, 1);
    }

    @Test
    public void allowedClassIsDeserialized() throws Exception {
        assertEquals("hello", read("hello"));
    }

    @Test
    public void unlistedClassIsRejected() throws Exception {
        try {
            read(new Denied());
            fail("deserialization of non-allowlisted class must be rejected");
        } catch (Exception expected) {
            // InvalidClassException 등
        }
    }

    @Test
    public void classBecomesAllowedAfterRegistration() throws Exception {
        BlobSerializableTypeHandler.addAllowedClass(Registered.class);
        assertEquals(Registered.class, read(new Registered()).getClass());
    }
}
