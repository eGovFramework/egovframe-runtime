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
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Job 종료 시 실행 결과를 key=value 한 줄로 남기는 리스너.
 *
 * <p>전용 로거 {@value #SUMMARY_LOGGER_NAME}(INFO)로 출력하므로 로깅 설정에서 관제·수집 시스템으로 라우팅하기 쉽다.
 * 상세 시계열 지표는 Spring Batch 5 에 내장된 Micrometer 관측(spring.batch.job / spring.batch.step 타이머)을 쓰고,
 * 이 리스너는 실행 단위 요약 한 줄만 담당한다.</p>
 *
 * <p>출력 예:</p>
 * <pre>
 * job=dailyStatsJob executionId=42 status=COMPLETED exitCode=COMPLETED durationMs=15320 steps=2 read=10000 written=9987 skipped=13 filtered=0
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
public class EgovBatchSummaryListener implements JobExecutionListener {

    /** 실행 요약 전용 로거명 */
    public static final String SUMMARY_LOGGER_NAME = "EGOV_BATCH_SUMMARY";

    private static final Logger SUMMARY = LoggerFactory.getLogger(SUMMARY_LOGGER_NAME);

    @Override
    public void afterJob(JobExecution jobExecution) {
        SUMMARY.info(buildSummary(jobExecution));
    }

    /**
     * JobExecution 의 실행 요약 문자열(key=value 한 줄)을 만든다. 건수는 Step 실행 결과를 합산한다.
     *
     * @param jobExecution 요약할 JobExecution
     * @return 요약 한 줄
     */
    static String buildSummary(JobExecution jobExecution) {
        long readCount = 0;
        long writeCount = 0;
        long skipCount = 0;
        long filterCount = 0;
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            readCount += stepExecution.getReadCount();
            writeCount += stepExecution.getWriteCount();
            skipCount += stepExecution.getSkipCount();
            filterCount += stepExecution.getFilterCount();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("job=").append(jobExecution.getJobInstance() == null
                ? "?" : jobExecution.getJobInstance().getJobName());
        builder.append(" executionId=").append(jobExecution.getId());
        builder.append(" status=").append(jobExecution.getStatus());
        builder.append(" exitCode=").append(jobExecution.getExitStatus() == null
                ? "?" : jobExecution.getExitStatus().getExitCode());
        builder.append(" durationMs=").append(durationMillis(jobExecution.getStartTime(), jobExecution.getEndTime()));
        builder.append(" steps=").append(jobExecution.getStepExecutions().size());
        builder.append(" read=").append(readCount);
        builder.append(" written=").append(writeCount);
        builder.append(" skipped=").append(skipCount);
        builder.append(" filtered=").append(filterCount);
        return builder.toString();
    }

    private static long durationMillis(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return -1;
        }
        return Duration.between(startTime, endTime).toMillis();
    }

}
