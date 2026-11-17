/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.egovframe.rte.psl.orm.ibatis.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * iBATIS TypeHandler implementation for arbitrary objects that get serialized to BLOBs.
 * Retrieves the LobHandler to use from SqlMapClientFactoryBean at config time.
 *
 * <p>Can also be defined in generic iBATIS mappings, as DefaultLobCreator will
 * work with most JDBC-compliant database drivers. In this case, the field type
 * does not have to be BLOB: For databases like MySQL and MS SQL Server, any
 * large enough binary type will work.
 *
 * @author Juergen Hoeller
 * @see org.egovframe.rte.psl.orm.ibatis.SqlMapClientFactoryBean#setLobHandler
 * @since 1.1.5
 * @deprecated as of Spring 3.2, in favor of the native Spring support
 * in the Mybatis follow-up project (http://code.google.com/p/mybatis/)
 */
@Deprecated
public class BlobSerializableTypeHandler extends AbstractLobTypeHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobSerializableTypeHandler.class);

    /**
     * Allowlist of class names permitted for deserialization from BLOB.
     * Prevents deserialization of untrusted data (e.g. RCE via malicious payloads, CWE-502).
     * Add application domain types via {@link #addAllowedClass(Class)} or {@link #addAllowedClassName(String)}.
     *
     * <p><b>보안 주의:</b> 이 목록은 JVM 전역(static)이므로, 여기에 클래스를 추가하면 이 TypeHandler를
     * 사용하는 애플리케이션의 모든 BLOB 역직렬화 지점에 동일하게 적용된다. 신뢰할 수 있는 자체 도메인
     * 타입만 추가할 것.</p>
     */
    private static final Set<String> ALLOWED_CLASS_NAMES = ConcurrentHashMap.newKeySet();

    static {
        ALLOWED_CLASS_NAMES.add(String.class.getName());
        ALLOWED_CLASS_NAMES.add(Integer.class.getName());
        ALLOWED_CLASS_NAMES.add(Long.class.getName());
        ALLOWED_CLASS_NAMES.add(Double.class.getName());
        ALLOWED_CLASS_NAMES.add(Float.class.getName());
        ALLOWED_CLASS_NAMES.add(Boolean.class.getName());
        ALLOWED_CLASS_NAMES.add(Short.class.getName());
        ALLOWED_CLASS_NAMES.add(Byte.class.getName());
        ALLOWED_CLASS_NAMES.add(Number.class.getName());
        ALLOWED_CLASS_NAMES.add(java.util.Date.class.getName());
        ALLOWED_CLASS_NAMES.add(java.sql.Timestamp.class.getName());
        ALLOWED_CLASS_NAMES.add(java.sql.Date.class.getName());
    }

    public static void addAllowedClass(Class<?> clazz) {
        if (clazz != null) {
            addAllowedClassName(clazz.getName());
        }
    }

    public static void addAllowedClassName(String className) {
        if (className != null && !className.isEmpty() && ALLOWED_CLASS_NAMES.add(className)) {
            LOGGER.warn("BlobSerializableTypeHandler allowed-class list widened at runtime: '{}' added " +
                    "(this affects all BLOB deserialization application-wide).", className);
        }
    }

    /**
     * 허용 목록에 있는 클래스만 역직렬화하는 ObjectInputStream (Java 8 호환: resolveClass 오버라이드).
     */
    private static class AllowlistObjectInputStream extends ObjectInputStream {

        AllowlistObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            if (!ALLOWED_CLASS_NAMES.contains(desc.getName())) {
                throw new InvalidClassException(desc.getName(), "Unauthorized deserialization attempt");
            }
            return super.resolveClass(desc);
        }
    }

    /**
     * Constructor used by iBATIS: fetches config-time LobHandler from
     * SqlMapClientFactoryBean.
     *
     * @see org.egovframe.rte.psl.orm.ibatis.SqlMapClientFactoryBean#getConfigTimeLobHandler
     */
    public BlobSerializableTypeHandler() {
        super();
    }

    /**
     * Constructor used for testing: takes an explicit LobHandler.
     */
    protected BlobSerializableTypeHandler(LobHandler lobHandler) {
        super(lobHandler);
    }

    @Override
    protected void setParameterInternal(
            PreparedStatement ps, int index, Object value, String jdbcType, LobCreator lobCreator)
            throws SQLException, IOException {

        if (value != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            try {
                oos.writeObject(value);
                oos.flush();
                lobCreator.setBlobAsBytes(ps, index, baos.toByteArray());
            } finally {
                oos.close();
            }
        } else {
            lobCreator.setBlobAsBytes(ps, index, null);
        }
    }

    @Override
    protected Object getResultInternal(ResultSet rs, int index, LobHandler lobHandler)
            throws SQLException, IOException {

        InputStream is = lobHandler.getBlobAsBinaryStream(rs, index);
        if (is != null) {
            ObjectInputStream ois = new AllowlistObjectInputStream(is);
            try {
                return ois.readObject();
            } catch (ClassNotFoundException ex) {
                throw new SQLException("Could not deserialize BLOB contents: " + ex.getMessage());
            } finally {
                ois.close();
            }
        } else {
            return null;
        }
    }

    public Object valueOf(String s) {
        return s;
    }

}
