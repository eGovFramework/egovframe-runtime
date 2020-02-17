/*
 * Copyright 2006-2007 the original author or authors.
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

package egovframework.brte.sample.testcase.test;

import egovframework.brte.sample.common.domain.trade.CustomerCredit;

import org.junit.runner.RunWith;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
* 여러 파일로 배치작업을 수행하는 테스트
* @author 배치실행개발팀
* @since 2012. 06.27
* @version 1.0
* @see
*/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/egovframework/batch/jobs/multiResourceIoJob.xml")
public class EgovMultiResourceFunctionalTests extends EgovAbstractIoSampleTests {

	/**
	 * 배치결과를 다시 읽을 때  reader 설정하는 메소드
	 */
	@Override
	protected void pointReaderToOutput(ItemReader<CustomerCredit> reader) {
		JobParameters jobParameters = new JobParametersBuilder(super.getUniqueJobParameters()).addString("input.file.path", "file:target/test-outputs/multiResourceOutput.csv.*")
				.toJobParameters();
		StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobParameters);
		StepSynchronizationManager.close();
		StepSynchronizationManager.register(stepExecution);
	}

	/**
	 * 잡파라미터를 설정하기 위한 메소드
	 * @return jobParameters
	 */
	@Override
	protected JobParameters getUniqueJobParameters() {

		JobParametersBuilder builder = new JobParametersBuilder(super.getUniqueJobParameters());
		return builder.addString("input.file.path", "/egovframework/data/input/delimited*.csv").addString("output.file.path", "file:./target/test-outputs/multiResourceOutput.csv")
				.toJobParameters();
	}

}
