/*
 * Copyright 2006-2007 the original author or authors. *
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
package egovframework.brte.sample.example.support;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * DB 파티션 처리를 위해 테이블의 범위를 자동으로 지정하여 각각의 파티션에 할당하는 클래스 Data 들이
 * 균일하게 분산되어 있으면 최적의 상태로 작동됨
 *
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 *
 *      <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
 */
public class EgovColumnRangePartitioner implements Partitioner {

	private JdbcTemplate jdbcTemplate;

	// table
	private String table;

	// column
	private String column;

	/**
	 * Data 가 있는 SQL 테이블 이름 셋팅
	 */
	public void setTable(String table) {
		this.table = table;
	}

	/**
	 * 파티션을 위한 컬럼명 셋팅
	 */
	public void setColumn(String column) {
		this.column = column;
	}

	/**
	 * DB 연결을 위한 DataSource
	 */
	public void setDataSource(DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * DB테이블을 파션하는 것은 컬럼안의 Data들이 "균일하게 분산" 되어 있다고 가정한다. ExecutionContext 값은
	 * <code>minValue</code> 와 <code>maxValue</code> 의 키를 갖으며, 이 키를 기준으로 각각의
	 * 파티션에서 고려해야할 값들의 범위가 정해진다.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		// 테이블의 특정컬럼의 가장 작은 값
		int min = jdbcTemplate.queryForObject("SELECT MIN(" + column + ") from " + table, Integer.class);

		//  테이블의 특정컬럼의 가장 큰 값
		int max = jdbcTemplate.queryForObject("SELECT MAX(" + column + ") from " + table, Integer.class);

		// 하나의 Execution에서 지정될 Data의 범위(크기)
		int targetSize = (max - min) / gridSize + 1;

		Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();
		int number = 0;
		int start = min;
		// targetSize 만큼 의 Data
		int end = start + targetSize - 1;

		// 파티션된 범위의 수만큼 ExecutionContext를 생성하고 minVlaue 와 maxValue를 셋팅
		while (start <= max) {

			ExecutionContext value = new ExecutionContext();
			result.put("partition" + number, value);

			if (end >= max) {
				end = max;
			}
			value.putInt("minValue", start);
			value.putInt("maxValue", end);
			start += targetSize;
			end += targetSize;
			number++;
		}

		return result;

	}

}
