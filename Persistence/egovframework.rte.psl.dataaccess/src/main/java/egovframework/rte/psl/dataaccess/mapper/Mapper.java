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
package egovframework.rte.psl.dataaccess.mapper;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Mapper 인터페이스에 대한 marker Annotation(Single-value)으로 MyBatis 적용 방식 중 annotation을 사용한 방식에 대한 기준을 위해 사용된다.
 * <p>
 * Service에 injection을 위해 Component annotation을 사용하였다.  
 * </p>
 * @author Vincent Han
 * @since 2.6
 *
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Mapper {
	/**
	 * The value may indicate a suggestion for a logical mapper name, to be turned into a Spring bean in case of an autodetected component.
	 * 
	 * @return the suggested mapper name, if any
	 */
	String value() default "";
}
