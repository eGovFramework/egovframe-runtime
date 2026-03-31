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

    /**
     * Allowlist of class names permitted for deserialization from BLOB.
     * Prevents deserialization of untrusted data (e.g. RCE via malicious payloads).
     * Add application domain types via {@link #addAllowedClass(Class)} or {@link #addAllowedClassName(String)}.
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
        ALLOWED_CLASS_NAMES.add(java.util.Date.class.getName());
        ALLOWED_CLASS_NAMES.add(java.sql.Timestamp.class.getName());
        ALLOWED_CLASS_NAMES.add(java.sql.Date.class.getName());
    }

    public static void addAllowedClass(Class<?> clazz) {
        if (clazz != null) {
            ALLOWED_CLASS_NAMES.add(clazz.getName());
        }
    }

    public static void addAllowedClassName(String className) {
        if (className != null && !className.isEmpty()) {
            ALLOWED_CLASS_NAMES.add(className);
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
    protected void setParameterInternal(PreparedStatement ps, int index, Object value, String jdbcType, LobCreator lobCreator) throws SQLException, IOException {
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
    protected Object getResultInternal(ResultSet rs, int index, LobHandler lobHandler) throws SQLException, IOException {
        InputStream is = lobHandler.getBlobAsBinaryStream(rs, index);
        // 2026.02.28 KISA 보안취약점 조치
        if (is != null) {
            ObjectInputFilter filter = info -> {
                Class<?> serialClass = info.serialClass();
                if (serialClass == null) {
                    return ObjectInputFilter.Status.UNDECIDED;
                }
                return ALLOWED_CLASS_NAMES.contains(serialClass.getName())
                        ? ObjectInputFilter.Status.ALLOWED
                        : ObjectInputFilter.Status.REJECTED;
            };
            ObjectInputStream ois = new ObjectInputStream(is);
            ois.setObjectInputFilter(filter);
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
