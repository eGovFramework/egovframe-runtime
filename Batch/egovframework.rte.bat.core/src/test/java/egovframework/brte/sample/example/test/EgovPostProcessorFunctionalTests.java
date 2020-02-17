/*
 * Copyright 2006-2007 the original author or authors. * * Licensed under the Apache License, Version 2.0 (the "License");
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

package egovframework.brte.sample.example.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import egovframework.brte.sample.common.domain.trade.CustomerCredit;
import egovframework.brte.sample.testcase.test.EgovAbstractIoSampleTests;

/**
 * 작업 실행시 후행 처리 설정 테스트 
 * @author 배치실행개발팀
 * @since 2012. 06.28
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.28  배치실행개발팀     최초 생성
 *  </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/jobs/postProcessorJob.xml" })
public class EgovPostProcessorFunctionalTests extends EgovAbstractIoSampleTests {
	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	@Qualifier("jobLauncherTestUtils")
	private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * 배치작업 테스트
	 */
	@Override
	@Test
	public void testUpdateCredit() throws Exception {

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(getUniqueJobParameters());
		/*
		 * 테스트의 결과 보다
		 * 콘솔창의 리스너 수행 여부를 확인할 것
		 */
		assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());

	}

	/**
	 * 배치결과를 다시 읽을 때  reader 설정하는 메소드
	 */
	@Override
	protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
	}

	/**
	 * 잡파라미터를 설정하기 위한 메소드 
	 * @return jobParameters
	 */
	@Override
	protected JobParameters getUniqueJobParameters() {
		return new JobParametersBuilder(super.getUniqueJobParameters()).addString("inputFile", "/egovframework/data/input/delimited.csv")
				.addString("outputFile", "file:./target/test-outputs/delimitedOutput.csv").toJobParameters();
	}

}