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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;

/**
 * 데이터 처리후 BATCH_STAGING 에 Insert 하는 라이터
 * @author 배치실행개발팀
 * @since 2012. 07.25
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012. 07.25  배치실행개발팀     최초 생성
 *  </pre>
 */
public class EgovStagingItemWriter<T> extends JdbcDaoSupport implements StepExecutionListener, ItemWriter<T> {

	//BATCH_STAGING의 processed에 관한 변수
	public static final String NEW = "N";
	//BATCH_STAGING의 processed에 관한 변수
	public static final String DONE = "Y";
	//BATCH_STAGING의 processed에 관한 변수
	public static final Object WORKING = "W";

	//incrementer
	private DataFieldMaxValueIncrementer incrementer;
	//stepExecution
	private StepExecution stepExecution;

	/**
	 * 설정 확인
	 */
	protected void initDao() throws Exception {
		super.initDao();
		Assert.notNull(incrementer, "DataFieldMaxValueIncrementer is required - set the incrementer property in the " + ClassUtils.getShortName(EgovStagingItemWriter.class));
	}

	/**
	 * incrementer 설정
	 */

	public void setIncrementer(DataFieldMaxValueIncrementer incrementer) {
		this.incrementer = incrementer;
	}

	/**
	 * BATCH_STAGING에 write함
	 */
	public void write(final List<? extends T> items) {

		final ListIterator<? extends T> itemIterator = items.listIterator();
		getJdbcTemplate().batchUpdate("INSERT into BATCH_STAGING (ID, JOB_ID, VALUE, PROCESSED) values (?,?,?,?)", new BatchPreparedStatementSetter() {

			public int getBatchSize() {
				return items.size();
			}

			public void setValues(PreparedStatement ps, int i) throws SQLException {

				long id = incrementer.nextLongValue();
				long jobId = stepExecution.getJobExecution().getJobId();

				Assert.state(itemIterator.nextIndex() == i, "Item ordering must be preserved in batch sql update");

				byte[] blob = SerializationUtils.serialize((Serializable) itemIterator.next());

				ps.setLong(1, id);
				ps.setLong(2, jobId);
				ps.setBytes(3, blob);
				ps.setString(4, NEW);
			}
		});

	}

	/**
	 * ExitStatus를 null로 줌
	 */
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

	/**
	 * stepExecution 설정
	 */
	public void beforeStep(StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}

}
