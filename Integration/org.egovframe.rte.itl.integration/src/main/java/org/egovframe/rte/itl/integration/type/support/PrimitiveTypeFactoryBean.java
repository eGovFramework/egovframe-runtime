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
package org.egovframe.rte.itl.integration.type.support;

import org.egovframe.rte.itl.integration.type.NoSuchTypeException;
import org.egovframe.rte.itl.integration.type.PrimitiveType;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.StringUtils;

/**
 * 연계 서비스의 표준 메시지의 PrimitiveType을 지원하는 FactoryBean 클래스
 * <p>
 * <b>NOTE:</b> 전자정부 연계 서비스의 표준 메시지의 PrimitiveType을 Spring Context 설정을 통해
 * reference할 수 있도록 지원하는 FactoryBean Class이다.
 * </p>
 * 
 * @author 실행환경 개발팀 심상호
 * @since 2009.06.01
 * @version 1.0
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2009.06.01	심상호				최초 생성
 * </pre>
 */
public class PrimitiveTypeFactoryBean extends AbstractFactoryBean<Object> implements BeanNameAware {

	/** bean name */
	protected String beanName;

	/** type id */
	protected String id;

	/**
	 * Default Constructor
	 */
	public PrimitiveTypeFactoryBean() {
		super();
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (id == null) {
			id = beanName;
		}
		if (StringUtils.hasText(id) == false) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	protected Object createInstance() throws Exception {
		PrimitiveType type = PrimitiveType.getPrimitiveType(id);
		if (type == null) {
			throw new NoSuchTypeException();
		}
		return type;
	}

	@Override
	public Class<?> getObjectType() {
		return PrimitiveType.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
