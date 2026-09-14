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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * 실행 정책을 적용하는 {@link JobLauncher} 데코레이터.
 *
 * <p>Spring Batch 기본 동작은 <b>같은 JobInstance(같은 파라미터)</b>의 중복만 막으므로, 다른 파라미터로 같은 Job 을 동시에
 * 또 띄우는 것은 막지 못한다. 이 데코레이터는 실행 시작 전에 {@link JobExplorer}(배치 메타테이블)를 조회해 다음 정책을
 * 적용한다. 메타테이블을 공유하는 멀티서버 환경에서도 같은 기준으로 동작한다.</p>
 * <ul>
 *   <li><b>같은 Job 이름 중복 실행 차단</b>({@code blockDuplicateJobName}, 기본 true) — 위반 시 Spring Batch 표준
 *       {@link JobExecutionAlreadyRunningException}</li>
 *   <li><b>전체 동시 실행 Job 수 제한</b>({@code maxConcurrentJobs}, 기본 0 = 무제한) — 위반 시
 *       {@link EgovBatchPolicyViolationException}</li>
 *   <li><b>고아 RUNNING 무시</b>({@code staleExecutionTimeoutMillis}, 기본 0 = 사용 안 함) — 서버 다운 등으로 RUNNING 으로
 *       남은 실행이 lastUpdated 기준 임계를 넘으면 집계에서 제외한다</li>
 * </ul>
 *
 * <pre>
 * &lt;bean id="policyJobLauncher" class="org.egovframe.rte.bat.core.operation.EgovJobExecutionPolicyLauncher"&gt;
 *     &lt;constructor-arg ref="jobLauncher"/&gt;
 *     &lt;constructor-arg ref="jobExplorer"/&gt;
 *     &lt;property name="maxConcurrentJobs" value="5"/&gt;
 *     &lt;property name="staleExecutionTimeoutMillis" value="3600000"/&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * <p>조회와 실행 사이에 다른 서버가 같은 Job 을 띄우는 경합까지 막지는 않는다. 그 수준의 배타 실행이 필요하면 DB 락 같은
 * 별도 수단을 함께 쓴다.</p>
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
public class EgovJobExecutionPolicyLauncher implements JobLauncher {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovJobExecutionPolicyLauncher.class);

    private final JobLauncher delegate;
    private final JobExplorer jobExplorer;

    private boolean blockDuplicateJobName = true;
    private int maxConcurrentJobs = 0;
    private long staleExecutionTimeoutMillis = 0L;

    /**
     * @param delegate    실제 실행을 맡길 JobLauncher
     * @param jobExplorer 실행 중 JobExecution 을 조회할 JobExplorer
     * @throws IllegalArgumentException 인자가 null 인 경우
     */
    public EgovJobExecutionPolicyLauncher(JobLauncher delegate, JobExplorer jobExplorer) {
        if (delegate == null || jobExplorer == null) {
            throw new IllegalArgumentException("delegate and jobExplorer must not be null");
        }
        this.delegate = delegate;
        this.jobExplorer = jobExplorer;
    }

    /**
     * 같은 Job 이름 중복 실행 차단 여부(기본 true).
     */
    public void setBlockDuplicateJobName(boolean blockDuplicateJobName) {
        this.blockDuplicateJobName = blockDuplicateJobName;
    }

    /**
     * 전체 동시 실행 Job 수 상한(기본 0 = 무제한).
     */
    public void setMaxConcurrentJobs(int maxConcurrentJobs) {
        this.maxConcurrentJobs = maxConcurrentJobs;
    }

    /**
     * lastUpdated 가 이 시간(ms) 이상 갱신되지 않은 RUNNING 실행을 고아로 보아 집계에서 제외한다(기본 0 = 사용 안 함).
     */
    public void setStaleExecutionTimeoutMillis(long staleExecutionTimeoutMillis) {
        this.staleExecutionTimeoutMillis = staleExecutionTimeoutMillis;
    }

    @Override
    public JobExecution run(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {

        if (blockDuplicateJobName) {
            long runningSameName = countRunning(job.getName());
            if (runningSameName > 0) {
                throw new JobExecutionAlreadyRunningException(
                        "Job '" + job.getName() + "' is already running (" + runningSameName
                                + " execution(s)); duplicate launch is blocked by policy");
            }
        }

        if (maxConcurrentJobs > 0) {
            long totalRunning = 0;
            for (String jobName : jobExplorer.getJobNames()) {
                totalRunning += countRunning(jobName);
            }
            if (totalRunning >= maxConcurrentJobs) {
                throw new EgovBatchPolicyViolationException(
                        "Max concurrent jobs exceeded: running=" + totalRunning + ", max=" + maxConcurrentJobs
                                + " (launch of '" + job.getName() + "' rejected)");
            }
        }

        LOGGER.debug("Execution policy passed - launching job '{}'", job.getName());
        return delegate.run(job, jobParameters);
    }

    private long countRunning(String jobName) {
        Set<JobExecution> running = jobExplorer.findRunningJobExecutions(jobName);
        if (running == null || running.isEmpty()) {
            return 0;
        }
        if (staleExecutionTimeoutMillis <= 0) {
            return running.size();
        }
        long count = 0;
        for (JobExecution execution : running) {
            if (!isStale(execution)) {
                count++;
            }
        }
        return count;
    }

    private boolean isStale(JobExecution execution) {
        LocalDateTime lastUpdated = execution.getLastUpdated();
        if (lastUpdated == null) {
            lastUpdated = execution.getStartTime();
        }
        if (lastUpdated == null) {
            return false;
        }
        boolean stale = Duration.between(lastUpdated, LocalDateTime.now()).toMillis() > staleExecutionTimeoutMillis;
        if (stale) {
            LOGGER.warn("Ignoring stale RUNNING execution id={} of job '{}' (lastUpdated={})", execution.getId(),
                    execution.getJobInstance() == null ? "?" : execution.getJobInstance().getJobName(), lastUpdated);
        }
        return stale;
    }

}
