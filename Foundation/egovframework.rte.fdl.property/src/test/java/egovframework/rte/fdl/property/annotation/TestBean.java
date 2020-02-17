/*
 * Copyright 2002-2012 the original author or authors.
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

package egovframework.rte.fdl.property.annotation;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ObjectUtils;

/**
 * Simple test bean used for testing bean factories, the AOP framework etc.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 15 April 2001
 */
public class TestBean {


	private String country;

	private BeanFactory beanFactory;

	private boolean postProcessed;

	private String name;

	private String sex;

	private int age;

	private boolean destroyed;


	public TestBean() {
	}

	public TestBean(String name) {
		this.name = name;
	}
	public TestBean(String name, int age) {
		this.name = name;
		this.age = age;
	}

	public void setPostProcessed(boolean postProcessed) {
		this.postProcessed = postProcessed;
	}

	public boolean isPostProcessed() {
		return postProcessed;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
		if (this.name == null) {
			this.name = sex;
		}
	}



	public void destroy() {
		this.destroyed = true;
	}

	public boolean wasDestroyed() {
		return destroyed;
	}


	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || !(other instanceof TestBean)) {
			return false;
		}
		TestBean tb2 = (TestBean) other;
		return (ObjectUtils.nullSafeEquals(this.name, tb2.name) && this.age == tb2.age);
	}

	public int hashCode() {
		return this.age;
	}

	public String toString() {
		return this.name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
