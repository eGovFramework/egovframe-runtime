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

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

/**
 * Skip 이 일어날 경우 Exit Status 를 변경하는 클래스
 * @since 2012. 07.30
 * @see
 * <pre>
 * << 개정이력(Modification Information) >>  
 * 수정일               수정자               수정내용 
 * -------    --------     --------------------------- 
 * 2012.07.30  배치실행개발팀         최초 생성 
 * </pre>
 */
public class EgovSkipCheckingDecider implements JobExecutionDecider {

	/**
	 * skip된 step 상태에 따라 FlowExecution의 Status를 바꾼다.
	 */
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		if (!stepExecution.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode()) && stepExecution.getSkipCount() > 0) {
			return new FlowExecutionStatus("COMPLETED WITH SKIPS");
		} else {
			return new FlowExecutionStatus(ExitStatus.COMPLETED.getExitCode());
		}
	}
}