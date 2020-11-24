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
package org.egovframe.rte.bat.support;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;

/**
 * EgovStepVariableListener 클래스
 * 표준프레임워크 베치에서 Step Listener 환경에서 Step Scope 범위에서 변수 사용
 * (stepExecutionContext에 데이터 저장)
 *
 * @author 장동한
 * @since 2017.12.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2017.12.01	장동한				최초 생성
 * 2018.01.15	장동한				getVariableMap, getVariableString 적용
 * </pre>
 */
public class EgovStepVariableListener implements StepExecutionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovStepVariableListener.class);

	private Map<String, Object> map = new HashMap<String, Object>();

	private Properties pros;

	public Properties getPros() {
		return pros;
	}

	public void setPros(Properties pros) {
		this.pros = pros;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		LOGGER.debug("EgovStepVariableListener afterJob run. ");
		Enumeration<Object> keys = this.pros.keys();
		ExecutionContext executionContext = stepExecution.getExecutionContext();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			executionContext.put(key, pros.getProperty(key));
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		LOGGER.debug("EgovStepVariableListener afterStep run. ");
		return null;
	}

	public String getVariableString(String key) {
		return pros.getProperty(key);
	}

	public Map<String, Object> getVariableMap() {
		map.clear();
		Enumeration<?> propertyNames = pros.propertyNames();
		String key = "";
		while (propertyNames.hasMoreElements()) {
			key = (String)propertyNames.nextElement();
			map.put(key, pros.getProperty(key));
		}
		return map;
	}

}
