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

package egovframework.brte.sample.example.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;

/**
 * 로그틀을 제공하는 클래스
 * 
 * @author 배치실행개발팀
 * @since 2012. 07.30
 * @see <pre>
 * << 개정이력(Modification Information) >>  
 * 수정일               수정자               수정내용 
 * ------      --------     --------------------------- 
 * 2012. 07.30  배치실행개발팀    최초 생성
 * </pre>
 */
public class EgovLogAdvice {

	private static Log log = LogFactory.getLog(EgovLogAdvice.class);

	/**
	 * 기본 로그틀 제공
	 */
	public void doBasicLogging(JoinPoint pjp) throws Throwable {
		Object[] args = pjp.getArgs();
		StringBuffer output = new StringBuffer();

		output.append(pjp.getTarget().getClass().getName()).append(": ");
		output.append(pjp.toShortString()).append(": ");

		for (Object arg : args) {
			output.append(arg).append(" ");
		}

		log.info("Basic: " + output.toString());
	}

	/**
	 *  processed: item 로그틀 제공 
	 */
	public void doStronglyTypedLogging(Object item) {
		log.info("Processed: " + item);
	}

}
