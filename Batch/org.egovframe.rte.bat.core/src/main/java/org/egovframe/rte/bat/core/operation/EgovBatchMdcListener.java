/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.bat.core.operation;

import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Job·Step 식별자를 SLF4J MDC 에 싣는 리스너.
 *
 * <p>Log4j2 RoutingAppender 와 조합하면 <b>Job 별 로그 파일 분리</b>를 재기동 없이 구성할 수 있다.</p>
 *
 * <pre>
 * &lt;Routing name="batchRouting"&gt;
 *     &lt;Routes pattern="$${ctx:batchJobName}"&gt;
 *         &lt;Route&gt;
 *             &lt;File name="job-${ctx:batchJobName}" fileName="logs/batch/${ctx:batchJobName}.log"&gt;...&lt;/File&gt;
 *         &lt;/Route&gt;
 *     &lt;/Routes&gt;
 * &lt;/Routing&gt;
 * </pre>
 *
 * <p>MDC 키: {@value #MDC_JOB_NAME}, {@value #MDC_JOB_EXECUTION_ID}, {@value #MDC_STEP_NAME}.
 * 멀티스레드 Step(taskExecutor)에서는 MDC 가 워커 스레드로 자동 전파되지 않는다.</p>
 *
 * @author 실행환경 개발팀
 * @since 5.1
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.10	실행환경 개발팀		최초 생성
 * </pre>
 */
public class EgovBatchMdcListener implements JobExecutionListener, StepExecutionListener {

    /** Job 이름 MDC 키 */
    public static final String MDC_JOB_NAME = "batchJobName";
    /** JobExecution ID MDC 키 */
    public static final String MDC_JOB_EXECUTION_ID = "batchJobExecutionId";
    /** Step 이름 MDC 키 */
    public static final String MDC_STEP_NAME = "batchStepName";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put(MDC_JOB_NAME, jobExecution.getJobInstance().getJobName());
        MDC.put(MDC_JOB_EXECUTION_ID, String.valueOf(jobExecution.getId()));
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        MDC.remove(MDC_JOB_NAME);
        MDC.remove(MDC_JOB_EXECUTION_ID);
        MDC.remove(MDC_STEP_NAME);
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        MDC.put(MDC_STEP_NAME, stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        MDC.remove(MDC_STEP_NAME);
        return stepExecution.getExitStatus();
    }

}
