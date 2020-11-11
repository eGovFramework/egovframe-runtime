/*
 * Copyright 2008-2009 MOPAS(Ministry of Public Administration and Security).
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
package egovframework.rte.fdl.cmmn.trace.manager;

import egovframework.rte.fdl.cmmn.trace.handler.TraceHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultTraceHandleManager클래스는 TraceHandlerService 인터페이스를 구현한 Manager 이다.
 *
 * <p><b>NOTE:</b> AbsTraceHandleManager 를 상속받고 TraceHandlerService 를 구현한 real TraceHandleManager 클래스이다.
 * 실제 수행 메소드들은 거의 AbsTraceHandleManager 에서 정의 되어 있으며, trace 메소드만 재정의하여 사용할 수 있다.
 * 별도의 특정 로직이 없다면 그냥 재정의 없이 사용가능하다. </p>
 *
 * @author Judd Cho (horanghi@gmail.com)
 * @since 2009.06.01
 * @version 1.0
 * @see
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.05.30  Judd Cho        최초 생성
 *   2015.01.31 Vincent Han		코드 품질 개선 
 *
 * </pre>
 */
public class DefaultTraceHandleManager extends AbstractTraceHandleManager implements TraceHandlerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTraceHandleManager.class);

	/**
	 * trace 메소드.
	 *
	 * @param clazz 클래스정보
	 * @param message 보여주고자하는 메세지
	 * @return boolean true|false
	 */
	@Override
	public boolean trace(Class<?> clazz, String message) {
		LOGGER.debug(" DefaultExceptionHandleManager.run() ");

		// 매칭조건이 false 인 경우
		if (!enableMatcher()) {
			return false;
		}

		for (String pattern : patterns) {
			LOGGER.debug("pattern = {}, thisPackageName = {}", pattern, getPackageName());
			LOGGER.debug("pm.match(pattern, getPackageName()) = {}", pm.match(pattern, getPackageName()));
			if (pm.match(pattern, getPackageName())) {
				for (TraceHandler eh : handlers) {
					eh.todo(clazz, message);
					LOGGER.debug("trace end?");
				}
				break;
			}
		}

		return true;
	}

}
