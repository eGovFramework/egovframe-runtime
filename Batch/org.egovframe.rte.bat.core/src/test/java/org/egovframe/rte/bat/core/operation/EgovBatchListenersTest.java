package org.egovframe.rte.bat.core.operation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovBatchMdcListener 의 MDC 키 주입·제거와 EgovBatchSummaryListener 의 실행 요약 형식을 검증한다.
 */
public class EgovBatchListenersTest {

    @AfterEach
    public void clearMdc() {
        MDC.clear();
    }

    private JobExecution jobExecution(String jobName, long executionId) {
        JobExecution jobExecution = new JobExecution(executionId);
        jobExecution.setJobInstance(new JobInstance(1L, jobName));
        return jobExecution;
    }

    @Test
    public void testMdcListenerPutsAndClearsJobAndStepKeys() {
        EgovBatchMdcListener listener = new EgovBatchMdcListener();
        JobExecution jobExecution = jobExecution("mdcJob", 11L);

        listener.beforeJob(jobExecution);
        assertEquals("mdcJob", MDC.get(EgovBatchMdcListener.MDC_JOB_NAME));
        assertEquals("11", MDC.get(EgovBatchMdcListener.MDC_JOB_EXECUTION_ID));

        StepExecution stepExecution = new StepExecution("step1", jobExecution);
        listener.beforeStep(stepExecution);
        assertEquals("step1", MDC.get(EgovBatchMdcListener.MDC_STEP_NAME));

        listener.afterStep(stepExecution);
        assertNull(MDC.get(EgovBatchMdcListener.MDC_STEP_NAME));
        assertEquals("mdcJob", MDC.get(EgovBatchMdcListener.MDC_JOB_NAME));

        listener.afterJob(jobExecution);
        assertNull(MDC.get(EgovBatchMdcListener.MDC_JOB_NAME));
        assertNull(MDC.get(EgovBatchMdcListener.MDC_JOB_EXECUTION_ID));
    }

    @Test
    public void testSummaryAggregatesStepCounts() {
        JobExecution jobExecution = jobExecution("dailyStatsJob", 42L);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStartTime(LocalDateTime.now().minusSeconds(15));
        jobExecution.setEndTime(LocalDateTime.now());
        StepExecution step1 = jobExecution.createStepExecution("step1");
        step1.setReadCount(10000);
        step1.setWriteCount(9987);
        step1.setReadSkipCount(13);
        StepExecution step2 = jobExecution.createStepExecution("step2");
        step2.setReadCount(500);
        step2.setWriteCount(500);
        step2.setFilterCount(7);

        String summary = EgovBatchSummaryListener.buildSummary(jobExecution);

        assertTrue(summary.startsWith("job=dailyStatsJob executionId=42 status=COMPLETED exitCode=COMPLETED durationMs="), summary);
        assertTrue(summary.endsWith(" steps=2 read=10500 written=10487 skipped=13 filtered=7"), summary);
    }

    @Test
    public void testSummaryHandlesMissingTimesAndInstance() {
        JobExecution jobExecution = new JobExecution(7L);
        jobExecution.setStatus(BatchStatus.FAILED);

        String summary = EgovBatchSummaryListener.buildSummary(jobExecution);

        assertTrue(summary.startsWith("job=? executionId=7 status=FAILED"), summary);
        assertTrue(summary.contains(" durationMs=-1 "), summary);
        assertTrue(summary.endsWith(" steps=0 read=0 written=0 skipped=0 filtered=0"), summary);
    }

}
