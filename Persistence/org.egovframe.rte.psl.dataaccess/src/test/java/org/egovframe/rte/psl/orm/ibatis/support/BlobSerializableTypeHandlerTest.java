package org.egovframe.rte.psl.orm.ibatis.support;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 허용목록에 적힌 타입을 실제로 되읽을 수 있는지 확인한다.
 *
 * <p>ObjectInputFilter 는 스트림에 담긴 상위 클래스마다 다시 불리므로,
 * 래퍼 타입을 읽으려면 java.lang.Number 도 허용목록에 있어야 한다.
 * java.sql.Timestamp 가 java.util.Date 덕분에 통과하는 것과 같은 이유다.</p>
 */
public class BlobSerializableTypeHandlerTest {

    @Test
    public void testGetResultInternalReadsAllowedTypes() throws Exception {
        Object[] allowed = {"문자열", Integer.valueOf(42), Long.valueOf(42L), Double.valueOf(4.2d),
                Float.valueOf(4.2f), Boolean.TRUE, Short.valueOf((short) 4), Byte.valueOf((byte) 4),
                new java.util.Date(0L), new java.sql.Timestamp(0L), new java.sql.Date(0L)};

        for (Object value : allowed) {
            assertEquals(value, readBack(value),
                    "허용목록에 있는 " + value.getClass().getName() + " 은 되읽을 수 있어야 한다");
        }
    }

    private Object readBack(Object value) throws Exception {
        LobHandler lobHandler = stubLobHandler(serialize(value));
        return new BlobSerializableTypeHandler(lobHandler).getResultInternal(null, 1, lobHandler);
    }

    private byte[] serialize(Object value) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        try {
            oos.writeObject(value);
        } finally {
            oos.close();
        }
        return baos.toByteArray();
    }

    private LobHandler stubLobHandler(byte[] stored) {
        return (LobHandler) Proxy.newProxyInstance(
                LobHandler.class.getClassLoader(),
                new Class<?>[]{LobHandler.class},
                (proxy, method, args) -> {
                    if ("getBlobAsBinaryStream".equals(method.getName())) {
                        return (InputStream) new ByteArrayInputStream(stored);
                    }
                    return null;
                });
    }
}
