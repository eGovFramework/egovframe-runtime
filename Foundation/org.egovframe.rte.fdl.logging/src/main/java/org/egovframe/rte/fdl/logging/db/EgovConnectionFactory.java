/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * log4j2의 JDBCAppender에서 사용하는 Connection 객체를 생성해주는 클래스
 * 
 * <p>Spring의 dataSource 를 injection받아 싱글톤으로 만들고
 * JDBCAppender가 Connection객체를 호출할 수 있도록 getDatabaseConnection() 메소드를 제공한다.</p>
 * 
 * @author Daniela Kwon
 * @since 2014.04.15
 * @version 3.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2014.04.15	Daniela Kwon		최초생성
 * </pre>
 */
public class EgovConnectionFactory {

	private static interface Singleton {
        final EgovConnectionFactory INSTANCE = new EgovConnectionFactory();
    }

	private DataSource dataSource;

	/**
	 * dataSource 지정한다.
	 * @param dataSource Spring에서 관리하는 dataSource
	 */
	public void setDataSource(DataSource dataSource) {
		Singleton.INSTANCE.dataSource = dataSource;
	}

	/**
	 * Spring에서 설정한 dataSource를 통해 Connection을 생성하고 리턴한다. 
	 * @return Connection
	 * @exception SQLException
	 */
	public static Connection getDatabaseConnection() throws SQLException {
		return Singleton.INSTANCE.dataSource.getConnection();
    }

}
