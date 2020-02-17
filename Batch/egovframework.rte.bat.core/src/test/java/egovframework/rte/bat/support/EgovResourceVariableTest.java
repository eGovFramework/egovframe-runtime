package egovframework.rte.bat.support;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import egovframework.rte.bat.core.launch.support.EgovBatchRunner;

/**
 * EgovResourceVariable JUnit Test 클래스
 *
 * @author 장동한
 * @since 2017.12.08
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2017.12.08  장동한           최초 생성
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/batch-runner-context.xml", "/egovframework/batch/jobs/delimitedToDelimitedJob-ResourceVariable.xml" })
public class EgovResourceVariableTest {

	@Autowired
	private EgovBatchRunner egovBatchRunner;
	
	@Autowired
	private EgovResourceVariable egovResourceVariable;
	
	@Test
	public void testJobRun() throws Exception {
		
		String jobName = "delimitedToDelimitedJob-ResourceVariable";

		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		//jobParametersBuilder.addString("inputFile", "egovframework/data/input/delimited.csv");
		jobParametersBuilder.addLong("timestamp", new Date().getTime());
		
		String jobParameters = egovBatchRunner.convertJobParametersToString(jobParametersBuilder.toJobParameters());
		
		long executionId = egovBatchRunner.start(jobName, jobParameters);

		//Job Name 확인
		assertEquals(jobName, egovBatchRunner.getJobInstance(executionId).getJobName());
		
		//Job Parameters 확인
		assertEquals(jobParameters.toString().replaceAll(" ", ""), egovBatchRunner.getJobOperator().getParameters(executionId).toString());
			
		//Resource Variable 확인
		assertEquals("file:./target/test-classes/egovframework/data/input/delimited.csv", egovResourceVariable.getVariable("input.resource"));
		
		//Job 실행 결과 확인
		assertEquals(BatchStatus.COMPLETED, egovBatchRunner.getJobExecution(executionId).getStatus());

	}
	
}
