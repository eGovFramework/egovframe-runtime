/*
 * Copyright 2009-2014 MOSPA(Ministry of Security and Public Administration).

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
package egovframework.rte.fdl.xml;

import java.io.Serializable;

/**
 * Object Wrap 구조체 Class
 * @author 개발프레임웍크 실행환경 개발팀 김종호
 * @since 2009.03.18
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자              수정내용
 *  ---------   ---------   -------------------------------
 * 2009.03.18    김종호        최초생성
 *
 * </pre>
 */
public class SharedObject implements Serializable {
	/**
	 * serialVersion UID
	 */
	private static final long serialVersionUID = 6064051424003971508L;
	
	/** Object 조회 Key **/
	private String key = null;
	/** 저장 Object **/
	private Object value = null;

	/**
	 * SharedObject 생성자
	 * 
	 * @param key - Object key
	 * @param value - 저장 object
	 */
	public SharedObject(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * 키 리턴
	 * 
	 * @return 키
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Object 리턴
	 * 
	 * @return Object
	 */
	public Object getValue() {
		return value;
	}
}
