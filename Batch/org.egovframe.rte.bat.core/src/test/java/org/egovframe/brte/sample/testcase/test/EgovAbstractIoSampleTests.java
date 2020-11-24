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
package org.egovframe.brte.sample.testcase.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.egovframe.brte.sample.common.domain.trade.CustomerCredit;
import org.egovframe.brte.sample.common.domain.trade.CustomerCreditIncreaseProcessor;

import org.junit.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * IoSample 실행을 위한 기본 클래스
 *
 * @author 배치실행개발팀
 * @since 2012. 06.27
 * @version 1.0
 * @see
 */
@ContextConfiguration(locations={"/org/egovframe/batch/simple-job-launcher-context.xml"
		, "/org/egovframe/batch/job-runner-context.xml"})
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class })
public abstract class EgovAbstractIoSampleTests {

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	@Qualifier("jobLauncherTestUtils")
	private JobLauncherTestUtils jobLauncherTestUtils;

	//배치작업의  reader
	@Autowired
	private ItemReader<CustomerCredit> reader;

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testUpdateCredit() throws Exception {

		open(reader);
		List<CustomerCredit> inputs = getCredits(reader);
		close(reader);
		JobExecution jobExecution = jobLauncherTestUtils.launchJob(getUniqueJobParameters());

		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		pointReaderToOutput(reader);
		open(reader);
		List<CustomerCredit> outputs = getCredits(reader);
		close(reader);

		assertEquals(inputs.size(), outputs.size());
		int itemCount = inputs.size();
		assertTrue(itemCount > 0);

		for (int i = 0; i < itemCount; i++) {
			assertEquals(inputs.get(i).getCredit().add(CustomerCreditIncreaseProcessor.FIXED_AMOUNT).intValue(), outputs.get(i).getCredit().intValue());
		}

	}

	/**
	 * 잡파라미터를 얻기 위한 메소드
	 * @return jobParameters
	 */
	protected JobParameters getUniqueJobParameters() {
		return jobLauncherTestUtils.getUniqueJobParameters();
	}

	/**
	 * file-to-file 배치작업일 경우만 필요한 설정으로  배치결과를 다시 읽을 때  reader 설정하는 메소드
	 */
	protected abstract void pointReaderToOutput(ItemReader<CustomerCredit> reader);

	/**
	 * 배치작업의 결과값을 list로 만드는 메소드
	 * @param reader
	 * @return List<CustomerCredit>
	 * @throws Exception
	 */
	private List<CustomerCredit> getCredits(ItemReader<CustomerCredit> reader) throws Exception {
		CustomerCredit credit;
		List<CustomerCredit> result = new ArrayList<CustomerCredit>();
		while ((credit = reader.read()) != null) {
			result.add(credit);
		}
		return result;

	}

	/**
	 * reader를 open하는 메소드
	 * @param reader
	 */
	private void open(ItemReader<?> reader) {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).open(new ExecutionContext());
		}
	}

	/**
	 * reader를 close하는 메소드
	 * @param reader
	 */
	private void close(ItemReader<?> reader) {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).close();
		}
	}

	/**
	 * stepExecution를 얻는 메소드
	 * @return StepExecution
	 */
	protected StepExecution getStepExecution() {
		return MetaDataInstanceFactory.createStepExecution(getUniqueJobParameters());
	}

}
