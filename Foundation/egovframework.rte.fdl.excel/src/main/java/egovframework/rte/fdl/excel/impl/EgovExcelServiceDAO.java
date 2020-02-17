/*
 * Copyright 2008-2014 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.fdl.excel.impl;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import egovframework.rte.psl.orm.ibatis.SqlMapClientCallback;
import egovframework.rte.psl.orm.ibatis.support.SqlMapClientDaoSupport;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapExecutor;

/**
 * 엑셀서비스을 처리하는 DAO 클래스.
 * 
 * <p><b>NOTE:</b> 엑셀 서비스를 제공하기 위해 구현한 DAO클래스이다.</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.06.01  윤성종           최초 생성
 *
 * </pre>
 */
@SuppressWarnings("deprecation")
public class EgovExcelServiceDAO extends SqlMapClientDaoSupport {

	@SuppressWarnings("unused")
	private SqlMapClient sqlMapClient = null;

    public EgovExcelServiceDAO(SqlMapClient sqlMapClient) {
        this.sqlMapClient = sqlMapClient;
        super.setSqlMapClient(sqlMapClient);
    }

    /**
     * 엑셀서비스의 배치업로드를 실행한다.
     * 
     * @param queryId <code>String</code>
     * @param list <code>List&lt;Object&gt;</code>
     * @return
     */
	public Integer batchInsert(final String queryId, final List<Object> list) {
        return (Integer) getSqlMapClientTemplate().execute(
            new SqlMapClientCallback<Object>() {

                public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {

                    executor.startBatch();

                    for (Iterator<Object> itr = list.iterator(); itr.hasNext();) {
                        executor.insert(queryId, itr.next());
                    }

                    return executor.executeBatch();
                }
            });
    }

    /**
     * 엑셀서비스의 배치업로드를 실행한다.
     * 업로드할 엑셀의 시작 위치를 정하여 지정한 셀부터 업로드한다.
     * 
     * @param queryId <code>String</code>
     * @param list <code>List&lt;Object&gt;</code>
     * @param start <code>int</code>
     * @return
     */
	public Integer batchInsert(final String queryId, final List<Object> list, final int start) {
        return (Integer) getSqlMapClientTemplate().execute(
            new SqlMapClientCallback<Object>() {

                public Object doInSqlMapClient(SqlMapExecutor executor) throws SQLException {

                    executor.startBatch();

                    int size = list.size();

                    for (int i = start; i < size; i++) {
                        executor.insert(queryId, list.get(i));
                    }

                    return executor.executeBatch();
                }
            });
    }

}