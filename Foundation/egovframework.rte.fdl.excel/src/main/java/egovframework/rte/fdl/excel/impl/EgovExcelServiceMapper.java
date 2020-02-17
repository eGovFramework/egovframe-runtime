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

import java.util.Iterator;
import java.util.List;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

import org.mybatis.spring.SqlSessionTemplate;

/**
 * 엑셀서비스을 처리하는 Mapper 클래스.
 * 
 * <p><b>NOTE:</b> 엑셀배치 서비스를 제공하기 위해 구현한 Mapper클래스이다.</p>
 * 
 * @author 실행환경 개발팀 윤성종
 * @since 2009.06.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *     수정일		수정자           수정내용
 *  ------------    --------    ---------------------------
 *   2014.05.07		이기하           최초 생성
 *
 * </pre>
 */
public class EgovExcelServiceMapper extends EgovAbstractMapper {

	@SuppressWarnings("unused")
	private SqlSessionTemplate sqlSessionTemplate = null;

    public EgovExcelServiceMapper(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate  = sqlSessionTemplate;
		super.setSqlSessionTemplate(sqlSessionTemplate);
	}

    /**
     * 엑셀서비스의 배치업로드를 실행한다.
     * 
     * @param queryId <code>String</code>
     * @param list <code>List&lt;Object&gt;</code>
     * @return
     */
	public Integer batchInsert(String queryId, List<Object> list) {
		Iterator<Object> itr = list.iterator();

		int count = 0;

		while (itr.hasNext()) {
			count += insert(queryId, itr.next());
		}

		return count;
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
		int count = 0;

		int size = list.size();

		for (int i = start; i < size; i++) {
			count += insert(queryId, list.get(i));
		}

		return count;
    }

}
