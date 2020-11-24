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
package org.egovframe.rte.bat.core.item.file.mapping;

import java.util.List;
import org.egovframe.rte.bat.core.reflection.EgovReflectionSupport;
import org.springframework.batch.item.file.transform.IncorrectTokenCountException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * EgovObjectMapper 클래스
 *
 * @author 실행환경 개발팀 이도형
 * @since 2012.07.20
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2012.07.20	이도형				최초 생성
 * </pre>
*/
public class EgovObjectMapper<T> implements InitializingBean {

	// VO type 지정
	private Class<? extends T> type;

	// names(필드들)지정
	private String[] names;

	// Reflection 관련 클래스
	private EgovReflectionSupport<?> egovReflectionSupport;

	/**
	 * VO type을 설정한다.
	 * @param type
	 */
	public void setType(Class<? extends T> type) {
		this.type = type;	
	}

	/**
	 * names를 설정한다.
	 * @param names
	 */
	public void setNames(String[] names) {
		this.names = new String[names.length];
		for (int i = 0; i < names.length; i++) {
			this.names[i] = names[i];
		}
		Assert.notNull(names, "Names must be non-null");
	}

	/**
	 * VO를 만들고, Token을 세팅한다.
	 * @param tokens: LineTokenizer에서 line을 자른 token
	 * @return T: VO
	 */
	@SuppressWarnings("unchecked")
	public T mapObject(List<String> tokens) {
		int tokenSize = tokens.size();

		// 지정된 names(필드들)의 수와  실제 만들어 낸 Token 갯수가 다르면 예외를 던진다.
		if (names.length != tokenSize) {
			throw new IncorrectTokenCountException(names.length, tokenSize);
		}

		return (T) egovReflectionSupport.generateObject((Class<?>) type, tokens, names);
	}

	public void afterPropertiesSet() {
		Assert.notNull(type, "The type must be set");
		Assert.notNull(names, "The names must be set");

		egovReflectionSupport = new EgovReflectionSupport<T>();
		egovReflectionSupport.generateSetterMethodMap(type, names);
		egovReflectionSupport.getFieldType(type, names);
	}	

}
