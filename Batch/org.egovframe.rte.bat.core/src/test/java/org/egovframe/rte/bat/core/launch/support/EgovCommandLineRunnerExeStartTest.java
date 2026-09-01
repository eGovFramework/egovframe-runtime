package org.egovframe.rte.bat.core.launch.support;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link EgovCommandLineRunner}의 JobExecution 페이징 offset이 호출마다 0에서 시작하는지 검증한다.
 * (offset을 static 필드로 두면 이전 호출의 누적값이 남아 두 번째 호출부터 빈 결과를 얻는 버그가 있었다)
 */
class EgovCommandLineRunnerExeStartTest {

	@SuppressWarnings("unchecked")
	private static List<JobExecution> invoke(EgovCommandLineRunner runner, String jobId) throws Exception {
		Method m = EgovCommandLineRunner.class.getDeclaredMethod("getJobExecutionsWithStatusGreaterThan", String.class,
				BatchStatus.class);
		m.setAccessible(true);
		return (List<JobExecution>) m.invoke(runner, jobId, BatchStatus.STOPPING);
	}

	/** getJobInstances(name,0,100)에서만 인스턴스를 반환하고, 다음 페이지는 비어 있는 JobExplorer 스텁. */
	private static JobExplorer stubJobExplorer(String jobName, JobInstance instance, JobExecution execution) {
		InvocationHandler handler = (proxy, method, args) -> {
			switch (method.getName()) {
			case "getJobInstances":
				int start = (int) args[1];
				return start == 0 ? List.of(instance) : List.of();
			case "getJobExecutions":
				return List.of(execution);
			default:
				return null;
			}
		};
		return (JobExplorer) Proxy.newProxyInstance(JobExplorer.class.getClassLoader(),
				new Class<?>[] { JobExplorer.class }, handler);
	}

	@Test
	void pagingOffset_resetsPerCall() throws Exception {
		String jobName = "sampleJob"; // 숫자가 아니어야 페이징 조회 경로로 진입

		JobInstance instance = new JobInstance(1L, jobName);
		JobExecution failed = new JobExecution(10L);
		failed.setStatus(BatchStatus.FAILED); // STOPPING보다 상위

		EgovCommandLineRunner runner = new EgovCommandLineRunner();
		Field f = EgovCommandLineRunner.class.getDeclaredField("jobExplorer");
		f.setAccessible(true);
		f.set(runner, stubJobExplorer(jobName, instance, failed));

		List<JobExecution> first = invoke(runner, jobName);
		List<JobExecution> second = invoke(runner, jobName);

		assertFalse(first.isEmpty(), "첫 호출은 실패 JobExecution을 찾아야 한다");
		assertFalse(second.isEmpty(), "두 번째 호출도 offset이 0으로 리셋되어 동일하게 찾아야 한다");
		assertEquals(first.size(), second.size(), "호출마다 동일한 결과여야 한다");
	}
}
