/*
 * Copyright 2012-2014 MOSPA(Ministry of Security and Public Administration).
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
package org.egovframe.rte.bat.core.event;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * 이벤트 알림을 위해 invoke 메소드를 파라미터 타입별로 정의한 인터페이스
 * 
 * @author 배치실행개발팀
 * @since 2012.06.27
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.06.27	배치실행개발팀		최초 생성
 * </pre>
 */
public interface EventNoticeTrigger {

	/**
	 * Function Call 위한 메서드 : 정해진 파라미터가 없고 사용자가 원하는 로직을 구현할 수 있다
	 */
	void invoke();

	/**
	 * Function Call 위한 메서드 : StepExecution 를 파라미터로 받음
	 */
	void invoke(StepExecution stepExecution);

	/**
	 * Function Call 위한 메서드 : JobExecution 를 파라미터로 받음
	 */
	void invoke(JobExecution jobExecution);

	/**
	 * Function Call 위한 메서드 : Execption 을 파라미터로 받음
	 */
	void invoke(Exception exception);

}
