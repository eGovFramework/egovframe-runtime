package org.egovframe.rte.bat.core.operation;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EgovJobExecutionPolicyLauncher 의 중복 실행 차단, 동시 실행 수 제한, 고아 RUNNING 제외를 검증한다.
 */
public class EgovJobExecutionPolicyLauncherTest {

    /** 실행 중 JobExecution 을 조작할 수 있는 JobExplorer 스텁 */
    static class StubJobExplorer extends SimpleJobExplorer {
        final Map<String, Set<JobExecution>> running = new HashMap<>();

        StubJobExplorer() {
            super(null, null, null, null);
        }

        void addRunning(String jobName, long executionId, LocalDateTime lastUpdated) {
            JobExecution execution = new JobExecution(executionId);
            execution.setStatus(BatchStatus.STARTED);
            execution.setLastUpdated(lastUpdated);
            running.computeIfAbsent(jobName, key -> new HashSet<>()).add(execution);
        }

        @Override
        public Set<JobExecution> findRunningJobExecutions(String jobName) {
            return running.getOrDefault(jobName, new HashSet<>());
        }

        @Override
        public List<String> getJobNames() {
            return List.copyOf(running.keySet());
        }
    }

    /** 위임 호출 횟수를 기록하는 JobLauncher 스텁 */
    static class RecordingLauncher implements JobLauncher {
        final AtomicInteger invocations = new AtomicInteger();

        @Override
        public JobExecution run(Job job, JobParameters jobParameters) {
            invocations.incrementAndGet();
            return new JobExecution(999L);
        }
    }

    private static Job job(String name) {
        return new Job() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public void execute(JobExecution execution) {
            }
        };
    }

    @Test
    public void testSameJobNameAlreadyRunningIsBlockedEvenWithDifferentParameters() {
        StubJobExplorer explorer = new StubJobExplorer();
        explorer.addRunning("jobA", 1L, LocalDateTime.now());
        RecordingLauncher delegate = new RecordingLauncher();
        EgovJobExecutionPolicyLauncher launcher = new EgovJobExecutionPolicyLauncher(delegate, explorer);

        assertThrows(JobExecutionAlreadyRunningException.class, () -> launcher.run(job("jobA"), new JobParameters()));
        assertEquals(0, delegate.invocations.get());
    }

    @Test
    public void testDuplicateIsAllowedWhenPolicyDisabled() throws Exception {
        StubJobExplorer explorer = new StubJobExplorer();
        explorer.addRunning("jobA", 1L, LocalDateTime.now());
        RecordingLauncher delegate = new RecordingLauncher();
        EgovJobExecutionPolicyLauncher launcher = new EgovJobExecutionPolicyLauncher(delegate, explorer);
        launcher.setBlockDuplicateJobName(false);

        launcher.run(job("jobA"), new JobParameters());

        assertEquals(1, delegate.invocations.get());
    }

    @Test
    public void testStaleRunningExecutionIsIgnored() throws Exception {
        StubJobExplorer explorer = new StubJobExplorer();
        explorer.addRunning("jobA", 1L, LocalDateTime.now().minusHours(1));
        RecordingLauncher delegate = new RecordingLauncher();
        EgovJobExecutionPolicyLauncher launcher = new EgovJobExecutionPolicyLauncher(delegate, explorer);
        launcher.setStaleExecutionTimeoutMillis(60_000L);

        launcher.run(job("jobA"), new JobParameters());

        assertEquals(1, delegate.invocations.get());
    }

    @Test
    public void testMaxConcurrentJobsIsEnforced() {
        StubJobExplorer explorer = new StubJobExplorer();
        explorer.addRunning("jobA", 1L, LocalDateTime.now());
        explorer.addRunning("jobB", 2L, LocalDateTime.now());
        RecordingLauncher delegate = new RecordingLauncher();
        EgovJobExecutionPolicyLauncher launcher = new EgovJobExecutionPolicyLauncher(delegate, explorer);
        launcher.setMaxConcurrentJobs(2);

        EgovBatchPolicyViolationException ex = assertThrows(EgovBatchPolicyViolationException.class,
                () -> launcher.run(job("jobC"), new JobParameters()));

        assertTrue(ex.getMessage().contains("Max concurrent jobs exceeded"), ex.getMessage());
        assertEquals(0, delegate.invocations.get());
    }

    @Test
    public void testUnderConcurrencyLimitIsLaunched() throws Exception {
        StubJobExplorer explorer = new StubJobExplorer();
        explorer.addRunning("jobA", 1L, LocalDateTime.now());
        RecordingLauncher delegate = new RecordingLauncher();
        EgovJobExecutionPolicyLauncher launcher = new EgovJobExecutionPolicyLauncher(delegate, explorer);
        launcher.setMaxConcurrentJobs(3);

        launcher.run(job("jobC"), new JobParameters());

        assertEquals(1, delegate.invocations.get());
    }

    @Test
    public void testNullDelegateOrExplorerIsRejected() {
        StubJobExplorer explorer = new StubJobExplorer();
        RecordingLauncher delegate = new RecordingLauncher();

        assertThrows(IllegalArgumentException.class, () -> new EgovJobExecutionPolicyLauncher(null, explorer));
        assertThrows(IllegalArgumentException.class, () -> new EgovJobExecutionPolicyLauncher(delegate, null));
    }

}
