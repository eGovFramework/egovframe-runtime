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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * EgovResourceVariable 클래스
 * 표준프레임워크 베치에서 singleton 방식으로 배치 Resource 변수 사용
 * (Map 형태로 Object를 담을 수 있음)
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
 * </pre>
 */
public class EgovResourceVariable {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovResourceVariable.class);

	private Properties pros;

	private Map<String, Object> map = null;

	public EgovResourceVariable(){
		LOGGER.debug("EgovResourceVariable constructor run. ");
		this.map = new HashMap<String, Object>();
	}

	public Properties getPros() {
		synchronized(this.map){
			return pros;
		}
	}

	public void setPros(Properties pros) {
		LOGGER.debug("EgovResourceVariable setPros run. ");
		String key = "";
		synchronized(this.map){
			map.clear();
			this.pros = pros;
			Enumeration<Object> keys = this.pros.keys();
			while (keys.hasMoreElements()) {
				key = (String) keys.nextElement();
				this.map.put(key, pros.getProperty(key));
			}
		}
	}

	public Map<String, Object> getVariableMap() {
		synchronized(this.map){
			return this.map;
		}
	}

	public Object getVariable(String key) {
		synchronized(this.map){
			return this.map.get(key);
		}
	}

	public void setVariable(String key, Object value) {
		synchronized(this.map){
			this.map.put(key, value);
		}
	}

	public String getVariableString(String key) {
		synchronized(this.map){
			return (String)this.map.get(key);
		}
	}

	public void setVariableString(String key, String value) {
		synchronized(this.map){
			this.map.put(key, value);
		}
	}

	public void setClear() {
		synchronized(this.map){
			this.map = new HashMap<String, Object>();
		}
	}

}
