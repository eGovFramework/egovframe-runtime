package org.egovframe.rte.bat.core.launch.support;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EgovCommandLineRunner}의 -next 옵션이 증가 파라미터와 명령행 파라미터를 함께 전달하는지 검증한다.
 */
class EgovCommandLineRunnerNextJobParametersTest {

	private static final String JOB_NAME = "nextSampleJob";

	@Test
	void nextOption_keepsIncrementedAndCommandLineParameters() throws Exception {
		JobParameters actual = runNext("targetDate=20260101,java.lang.String,false");

		assertFalse(actual.isEmpty(), "-next 실행 시 JobParameters가 비어 있으면 안 된다");
		assertEquals(1L, actual.getLong("run.id"), "증가 파라미터가 유지되어야 한다");
		assertEquals("20260101", actual.getString("targetDate"), "명령행 파라미터가 유지되어야 한다");
		assertEquals(String.class, actual.getParameter("targetDate").getType(), "명령행 파라미터의 타입이 보존되어야 한다");
		assertFalse(actual.getParameter("targetDate").isIdentifying(), "명령행에서 지정한 identifying 여부가 보존되어야 한다");
	}

	@Test
	void nextOption_commandLineParameterOverridesIncrementedParameter() throws Exception {
		JobParameters actual = runNext("run.id=99,java.lang.Long,true");

		assertEquals(99L, actual.getLong("run.id"), "이름이 같으면 명령행 파라미터가 증가 파라미터를 덮어써야 한다");
		assertEquals(Long.class, actual.getParameter("run.id").getType(), "덮어쓴 파라미터의 타입이 보존되어야 한다");
		assertTrue(actual.getParameter("run.id").isIdentifying(), "덮어쓴 파라미터의 identifying 여부가 보존되어야 한다");
	}

	/** -next 옵션으로 Job을 실행하고 JobLauncher에 실제로 전달된 JobParameters를 반환한다. */
	private static JobParameters runNext(String... parameters) throws Exception {
		AtomicReference<JobParameters> captured = new AtomicReference<>();
		EgovCommandLineRunner runner = new EgovCommandLineRunner();
		inject(runner, "launcher", stubJobLauncher(captured));
		inject(runner, "jobExplorer", stubJobExplorer());

		int exit = runner.start("org/egovframe/rte/bat/core/launch/support/next-job-context.xml", JOB_NAME, parameters,
				Set.of("-next"));

		JobParameters actual = captured.get();
		assertNotNull(actual, "JobParameters가 전달되어야 한다: " + EgovCommandLineRunner.getErrorMessage());
		assertEquals(0, exit, "완료 ExitStatus는 0으로 매핑되어야 한다");
		return actual;
	}

	private static void inject(EgovCommandLineRunner runner, String fieldName, Object value) throws Exception {
		Field field = EgovCommandLineRunner.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(runner, value);
	}

	private static JobLauncher stubJobLauncher(AtomicReference<JobParameters> captured) {
		return (job, jobParameters) -> {
			captured.set(jobParameters);
			JobExecution jobExecution = new JobExecution(1L);
			jobExecution.setExitStatus(ExitStatus.COMPLETED);
			return jobExecution;
		};
	}

	private static JobExplorer stubJobExplorer() {
		InvocationHandler handler = (proxy, method, args) -> {
			if ("getJobInstances".equals(method.getName())) {
				return List.of();
			}
			return null;
		};
		return (JobExplorer) Proxy.newProxyInstance(JobExplorer.class.getClassLoader(),
				new Class<?>[] { JobExplorer.class }, handler);
	}

	public static class NextSampleJob implements Job {

		private final JobParametersIncrementer incrementer = new RunIdIncrementer();
		private final JobParametersValidator validator = new DefaultJobParametersValidator();

		@Override
		public String getName() {
			return JOB_NAME;
		}

		@Override
		public boolean isRestartable() {
			return true;
		}

		@Override
		public JobParametersIncrementer getJobParametersIncrementer() {
			return incrementer;
		}

		@Override
		public JobParametersValidator getJobParametersValidator() {
			return validator;
		}

		@Override
		public void execute(JobExecution execution) {
		}
	}
}
