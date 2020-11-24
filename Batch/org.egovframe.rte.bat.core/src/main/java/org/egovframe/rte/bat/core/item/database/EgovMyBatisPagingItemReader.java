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
package org.egovframe.rte.bat.core.item.database;

import static org.springframework.util.ClassUtils.getShortName;
import java.util.HashMap;
import java.util.Map;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.egovframe.rte.bat.support.EgovJobVariableListener;
import org.egovframe.rte.bat.support.EgovResourceVariable;
import org.egovframe.rte.bat.support.EgovStepVariableListener;

/**
 * EgovMyBatisPagingItemReader 클래스
 * 표준프레임워크 베치에서 MyBatisPagingItemReader 클래스를 확장하여
 * EgovResourceVariable,EgovJobVariableListener,EgovStepVariableListener 클래스를 이용한 변수 공유 기능 제공
 *      
 * @author 장동한
 * @since 2017.09.06
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2017.09.06	장동한				최초 생성
 * 2018.01.15	장동한				EgovJobVariable, EgovStepVariable 적용
 * </pre>
 */
public class EgovMyBatisPagingItemReader<T> extends MyBatisPagingItemReader<T> {

	private Map<String, Object> map = new HashMap<String, Object>();

	/** egovframework EgovResourceVariable */
	private EgovResourceVariable resourceVariable = null;

	/** egovframework EgovJobVariableListener */
	private EgovJobVariableListener jobVariable = null;

	/** egovframework EgovStepVariableListener */
	private EgovStepVariableListener stepVariable = null;

	public EgovResourceVariable getResourceVariable() {
		return resourceVariable;
	}

	public void setResourceVariable(EgovResourceVariable resourceVariable) {
		this.resourceVariable = resourceVariable;
		processVariable();
	}

	public EgovJobVariableListener getJobVariable() {
		return jobVariable;
	}

	public void setJobVariable(EgovJobVariableListener jobVariable) {
		this.jobVariable = jobVariable;
		processVariable();
	}

	public EgovStepVariableListener getStepVariable() {
		return stepVariable;
	}

	public void setStepVariable(EgovStepVariableListener stepVariable) {
		this.stepVariable = stepVariable;
		processVariable();
	}

	public void processVariable() {
		map.clear();

		if(resourceVariable != null)
	    	map.putAll(resourceVariable.getVariableMap());
	    if(jobVariable != null)
	    	map.putAll(jobVariable.getVariableMap());
	    if(stepVariable != null)
	    	map.putAll(stepVariable.getVariableMap());

	    setParameterValues(this.map);
	}

	public EgovMyBatisPagingItemReader() {
		setName(getShortName(EgovMyBatisPagingItemReader.class));
	}

}
