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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * Data 처리하다 Error 발생 시 Skip 에 관한 정보를 DB로 처리하는 클래스
 *
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @see <pre>
 * << 개정이력(Modification Information) >>
 * 수정일               수정자               수정내용
 * ------      --------     ---------------------------
 * 2012. 07.30  배치실행개발팀    최초 생성
 * </pre>
 */
public class EgovErrorLogTasklet implements Tasklet, StepExecutionListener {

	protected final Log logger = LogFactory.getLog(getClass());

	private JdbcTemplate jdbcTemplate;

	//jobName
	private String jobName;

	//stepExecution
	private StepExecution stepExecution;

	private String stepName;

	/**
	 * Error_Log 테이블에 Skip Error 에 대한 정보를 저장
	 */
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		Assert.notNull(this.stepName, "Step name not set.  Either this class was not registered as a listener "
				+ "or the key 'stepName' was not found in the Job's ExecutionContext.");
		this.jdbcTemplate.update("insert into ERROR_LOG values (?, ?, '" + getSkipCount() + " records were skipped!')", jobName, stepName);
		return RepeatStatus.FINISHED;
	}

	/**
	 * skipCount 정보를 Execution 에서 가져옴
	 */
	private int getSkipCount() {
		if (stepExecution == null || stepName == null) {
			return 0;
		}
		for (StepExecution execution : stepExecution.getJobExecution().getStepExecutions()) {
			if (execution.getStepName().equals(stepName)) {
				return execution.getSkipCount();
			}
		}
		return 0;
	}

	/**
	 * DataSource 셋팅
	 */
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	/**
	 * Step 수행전에 처리되는 로직, 해당 stepName 을 JobExecution의 Context에서 제거함
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.jobName = stepExecution.getJobExecution().getJobInstance().getJobName().trim();
		this.stepName = (String) stepExecution.getJobExecution().getExecutionContext().get("stepName");
		this.stepExecution = stepExecution;
		stepExecution.getJobExecution().getExecutionContext().remove("stepName");
	}

	/**
	 * Step 단계이후 수행
	 */
	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		return null;
	}

}
