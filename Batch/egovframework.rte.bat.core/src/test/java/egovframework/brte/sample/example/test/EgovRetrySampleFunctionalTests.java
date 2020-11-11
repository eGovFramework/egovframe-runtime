/*
 * Copyright 2006-2007 the original author or authors.
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

package egovframework.brte.sample.example.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import egovframework.brte.sample.common.domain.trade.GeneratingTradeItemReader;
import egovframework.brte.sample.example.support.EgovRetrySampleItemWriter;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Data를 처리하다 에러시 Retry 시도 횟수 설정만큼 재시도 하는 과정을 테스트 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @version 1.0
 * @see
 * 
 * <pre>
 * == 개정이력(Modification Information) ==
 *   
 *   수정일               수정자               수정내용
 *  -------      --------     ---------------------------
 *  2012. 07.30  배치실행개발팀    최초 생성
 *
 * </pre>
*/

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/egovframework/batch/simple-job-launcher-context.xml", "/egovframework/batch/job-runner-context.xml",
		"/egovframework/batch/jobs/retrySample.xml" })
public class EgovRetrySampleFunctionalTests {

	//ItemReader
	@Autowired
	private GeneratingTradeItemReader itemGenerator;

	//ItemWriter
	@Autowired
	private EgovRetrySampleItemWriter<?> itemProcessor;

	//배치작업을  test하기 위한 JobLauncherTestUtils
	@Autowired
	@Qualifier("jobLauncherTestUtils")
	private JobLauncherTestUtils jobLauncherTestUtils;

	/**
	 * 배치작업 테스트
	 */
	@Test
	public void testLaunchJob() throws Exception {
		jobLauncherTestUtils.launchJob();
		//items processed = items read + 2 exceptions
		assertEquals(itemGenerator.getLimit() + 2, itemProcessor.getCounter());
	}

}
