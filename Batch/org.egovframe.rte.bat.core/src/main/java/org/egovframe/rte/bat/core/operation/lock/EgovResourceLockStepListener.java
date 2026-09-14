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
package org.egovframe.rte.bat.core.operation.lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Step 시작 전에 자원 락을 얻고 종료 시 해제하는 {@link StepExecutionListener}.
 *
 * <p>소유자 식별자는 {@code jobName#jobExecutionId} 로 자동 구성되어 실행 단위로 구분된다. 대기 시간 안에 얻지 못하면
 * {@link EgovResourceLockException} 으로 Step 을 실패시키고, Step 성공·실패와 무관하게 종료 시 락을 해제한다.</p>
 *
 * <pre>
 * &lt;bean class="org.egovframe.rte.bat.core.operation.lock.EgovResourceLockStepListener"&gt;
 *     &lt;constructor-arg ref="egovResourceLock"/&gt;
 *     &lt;constructor-arg value="/data/batch/out/daily.dat"/&gt;   &lt;!-- resourceId --&gt;
 *     &lt;property name="waitMillis" value="10000"/&gt;
 * &lt;/bean&gt;
 * </pre>
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
public class EgovResourceLockStepListener implements StepExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovResourceLockStepListener.class);

    private final EgovJdbcResourceLock resourceLock;
    private final String resourceId;

    private long waitMillis = 0L;
    private long pollMillis = 500L;

    /**
     * @param resourceLock 락 구현
     * @param resourceId   이 Step 이 배타적으로 쓰는 자원 식별자
     * @throws IllegalArgumentException 인자가 null 이거나 비어 있는 경우
     */
    public EgovResourceLockStepListener(EgovJdbcResourceLock resourceLock, String resourceId) {
        if (resourceLock == null) {
            throw new IllegalArgumentException("resourceLock must not be null");
        }
        if (resourceId == null || resourceId.trim().isEmpty()) {
            throw new IllegalArgumentException("resourceId must not be empty");
        }
        this.resourceLock = resourceLock;
        this.resourceId = resourceId;
    }

    /**
     * 락 획득 최대 대기 시간(ms). 기본 0 = 즉시 1회 시도.
     */
    public void setWaitMillis(long waitMillis) {
        this.waitMillis = waitMillis;
    }

    /**
     * 락 획득 재시도 간격(ms). 기본 500.
     */
    public void setPollMillis(long pollMillis) {
        this.pollMillis = pollMillis;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String ownerId = ownerId(stepExecution);
        boolean acquired = resourceLock.acquire(resourceId, ownerId, waitMillis, pollMillis);
        if (!acquired) {
            throw new EgovResourceLockException("Failed to acquire batch resource lock '" + resourceId + "' for "
                    + ownerId + " within " + waitMillis + "ms; another execution is using the resource");
        }
        LOGGER.info("Batch resource lock acquired: '{}' by {}", resourceId, ownerId);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        String ownerId = ownerId(stepExecution);
        if (resourceLock.release(resourceId, ownerId)) {
            LOGGER.info("Batch resource lock released: '{}' by {}", resourceId, ownerId);
        }
        return stepExecution.getExitStatus();
    }

    private String ownerId(StepExecution stepExecution) {
        return stepExecution.getJobExecution().getJobInstance() == null
                ? "job#" + stepExecution.getJobExecutionId()
                : stepExecution.getJobExecution().getJobInstance().getJobName() + "#" + stepExecution.getJobExecutionId();
    }

}
