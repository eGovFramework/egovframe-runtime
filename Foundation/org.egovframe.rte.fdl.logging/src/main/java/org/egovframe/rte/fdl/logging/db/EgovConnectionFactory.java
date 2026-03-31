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
package org.egovframe.rte.fdl.logging.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * log4j2의 JDBCAppender에서 사용하는 Connection 객체를 생성해주는 클래스
 *
 * <p>Spring의 DataSource를 주입받아 싱글톤으로 사용되며,
 * JDBCAppender가 Connection 객체를 사용할 수 있도록 getDatabaseConnection() 메서드를 제공한다.</p>
 *
 * @author Daniela Kwon
 * @version 3.0
 * @since 2014.04.15
 */
public class EgovConnectionFactory {

    private DataSource dataSource;

    public EgovConnectionFactory() {
    }

    /**
     * 설정된 DataSource를 통해 Connection을 생성하여 반환한다.
     *
     * @return Connection 객체
     * @throws SQLException SQL 예외 발생 시
     */
    public static Connection getDatabaseConnection() throws SQLException {
        if (Holder.INSTANCE.dataSource == null) {
            throw new IllegalStateException("DataSource is not set. Please call setDataSource() before using this method.");
        }
        return Holder.INSTANCE.dataSource.getConnection();
    }

    /**
     * DataSource를 설정한다.
     *
     * @param dataSource Spring에서 관리하는 DataSource
     */
    public void setDataSource(DataSource dataSource) {
        Holder.INSTANCE.dataSource = dataSource;
    }

    private static class Holder {
        private static final EgovConnectionFactory INSTANCE = new EgovConnectionFactory();
    }

}
