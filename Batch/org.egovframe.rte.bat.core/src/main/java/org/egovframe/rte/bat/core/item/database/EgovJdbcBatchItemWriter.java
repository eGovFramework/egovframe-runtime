/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.bat.core.item.database;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.egovframe.rte.bat.core.item.database.support.EgovItemPreparedStatementSetter;
import org.egovframe.rte.bat.core.reflection.EgovReflectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.util.Assert;

/**
 * EgovJdbcBatchItemWriter EgovItemPreparedStatementSetter인터페이스를 상속받은
 * ItemPreparedStatementSetter이 설정되어 있어야 함.
 *
 * @author 배치실행개발팀
 * @since 2012.07.20
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	배치실행개발팀		최초 생성
 * 2017.02.14	장동한				시큐어코딩(ES)-부적절한 예외 처리[CWE-253, CWE-440, CWE-754]
 * </pre>
 */
public class EgovJdbcBatchItemWriter<T> implements ItemWriter<T>, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovJdbcBatchItemWriter.class);

	// SimpleJdbcOperations
	private JdbcOperations jdbcTemplate;

	// EgovItemPreparedStatementSetter
	private EgovItemPreparedStatementSetter<T> itemPreparedStatementSetter;

	// sql 쿼리
	private String sql;

	// params 초기화
	private String[] params = new String[0];

	// assertUpdates 초기화
	private boolean assertUpdates = true;

	// usingParameters
	private boolean usingParameters;

	private EgovReflectionSupport<T> reflector;

	/**
	 * AssertUpdates 설정 셋팅
	 *
	 * @param assertUpdates
	 */
	public void setAssertUpdates(boolean assertUpdates) {
		this.assertUpdates = assertUpdates;
	}

	/**
	 * sql 셋팅
	 *
	 * @param sql
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * ItemPreparedStatementSetter 셋팅
	 *
	 * @param preparedStatementSetter
	 */
	public void setItemPreparedStatementSetter(EgovItemPreparedStatementSetter<T> preparedStatementSetter) {
		this.itemPreparedStatementSetter = preparedStatementSetter;
	}

	/**
	 * params 셋팅
	 *
	 * @param params
	 */
	public void setParams(String[] params) {
		this.params = params == null ? null : Arrays.asList(params).toArray(new String[params.length]);
	}

	/**
	 * dataSource 셋팅
	 *
	 * @param dataSource
	 */
	public void setDataSource(DataSource dataSource) {
		if (jdbcTemplate == null) {
			this.jdbcTemplate = new JdbcTemplate(dataSource);
		}
	}

	/**
	 * SimpleJdbcTemplate 셋팅
	 *
	 * @param jdbcTemplate
	 */
	public void setSimpleJdbcTemplate(JdbcOperations jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 설정의 properties 셋팅확인
	 */
	public void afterPropertiesSet() {
		Assert.notNull(jdbcTemplate, "A DataSource or a SimpleJdbcTemplate is required.");
		Assert.notNull(sql, "An SQL statement is required.");

		if (params.length != 0) {
			usingParameters = true;
		}

		Assert.notNull(itemPreparedStatementSetter, "Using SQL statement with '?' placeholders requires an EgovMethodMapItemPreparedStatementSetter");

		reflector = new EgovReflectionSupport<T>();
	}

	/**
	 * DB Write 를 위해 적절한 setValues 호출
	 * setValues(item, ps, params, sqlTypes, methodMap) :
	 * setValues(item, ps) : 따로 VO
	 */
	public void write(final List<? extends T> items) throws Exception {
		if (!items.isEmpty()) {
			LOGGER.debug("Executing batch with {} items ", items.size());

			int[] updateCounts = null;
			updateCounts = (int[]) jdbcTemplate.execute(sql, new PreparedStatementCallback<Object>() {
				public Object doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
					// Parameters 가 있으면 item, ps, params, sqlTypes,methodMap 를 파라메터로 받는 setValues call
					// 없으면 item, ps 를 파라메터로 받는 setValues call
					if (usingParameters) {
						String[] sqlTypes = reflector.getSqlTypeArray(params, items.get(0));
						reflector.generateGetterMethodMap(params, items.get(0));
						Map<String, Method> methodMap = reflector.getMethodMap();
						for (T item : items) {
							itemPreparedStatementSetter.setValues(item, ps, params, sqlTypes, methodMap);
							ps.addBatch();
						}
					} else {
						for (T item : items) {
							itemPreparedStatementSetter.setValues(item, ps);
							ps.addBatch();
						}
					}
					return ps.executeBatch();
				}
			});

			if (assertUpdates && (updateCounts != null)) {
				for (int i = 0; i < updateCounts.length; i++) {
					int value = updateCounts[i];
					if (value == 0) {
						throw new EmptyResultDataAccessException("Item " + i + " of " + updateCounts.length + " did not update any rows: [" + items.get(i) + "]", 1);
					}
				}
			}
		}
	}

}
