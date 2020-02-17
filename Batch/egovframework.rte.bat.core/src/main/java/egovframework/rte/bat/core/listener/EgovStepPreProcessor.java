/*
 * Copyright 2014 MOSPA(Ministry of Security and Public Administration).
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
package egovframework.rte.bat.core.listener;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepListenerSupport;

/**
 * Step 단계 이전에 호출되는 메소드를 갖고 있는 클래스
 * @author 배치실행개발팀
 * @since 2012.06.27
 * @version 1.0
 * @see
 */

public class EgovStepPreProcessor<T, S> extends StepListenerSupport<T, S> {

	/**
	 * Step 수행 이전에 호출되는 부분
	 * org.springframework.batch.core.StepExecutionListener#beforeStep(org.
	 * springframework.batch.core.StepExecution)
	*/
	public void beforeStep(StepExecution stepExecution) {

	}

}
