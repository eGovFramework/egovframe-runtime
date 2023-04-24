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
package org.egovframe.brte.sample.example.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.egovframe.rte.bat.core.listener.EgovJobPostProcessor;
import org.springframework.batch.core.JobExecution;

/**
 * 잡 단계 이후에 호출되는 리스너 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012.06.27
 * @version 1.0
 * @see <pre>
 *      개정이력(Modification Information)
 *   
 *   수정일      수정자           수정내용
 *  ------- -------- ---------------------------
 *  2012.06.27  배치실행개발팀     최초 생성
 * </pre>
 */
public class EgovSampleJobPostProcessor extends EgovJobPostProcessor {

	protected Log log = LogFactory.getLog(this.getClass());

	/**
	 * Job 수행 이후에 호출되는 부분
	 */
	public void afterJob(JobExecution jobExecution) {

		log.info(">>>>>>>> afterJob :::  Finish " + jobExecution.getJobInstance().getJobName());
	}

}
