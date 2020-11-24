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
package org.egovframe.rte.psl.dataaccess.mapper;

import org.mybatis.spring.mapper.MapperScannerConfigurer;

/**
 * Mapper를 등록하기 위한 configurer로 Mapper annotation을 대상으로 scan한다.
 * <p>
 * 추가적으로 sqlSessionFactoryBeanName에 대하여 "sqlSession"을 사용한다. 
 * </p><p>
 * 설정 예:
 * </p>
 *
 * <pre class="code">
 * {@code
 *   <bean class="org.egovframe.rte.psl.dataaccess.mapper.MapperConfigurer">
 *       <property name="basePackage" value="org.egovframe.rte.**.mapper" />
 *   
 * }
 * </pre>
 *
 * @author Vincent Han
 * @since 2.6
 * <pre>
 * </pre>
 */
public class MapperConfigurer extends MapperScannerConfigurer {

	/**
	 * 기본 정보(anntationClass, sqlSessionFactoryBeanName)으로 설정한다.
	 */
	public MapperConfigurer() {
		super.setAnnotationClass(Mapper.class);
		super.setSqlSessionFactoryBeanName("sqlSession");
	}
}
