/*
 * Copyright 2002-2013 the original author or authors.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;


public class PropertySourceAnnotationTests {

	@Test
	public void withExplicitName() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigWithExplicitName.class);
		ctx.refresh();
		assertTrue("property source p1 was not added",
				ctx.getEnvironment().getPropertySources().contains("p1"));
		assertThat(ctx.getBean(TestBean.class).getName(), equalTo("p1TestBean"));

		// assert that the property source was added last to the set of sources
		String name;
		MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
		Iterator<org.springframework.core.env.PropertySource<?>> iterator = sources.iterator();
		do {
			name = iterator.next().getName();
		}
		while(iterator.hasNext());

		assertThat(name, is("p1"));

		ctx.close();
	}

	@Test
	public void orderingIsLifo() {
		{
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.register(ConfigWithImplicitName.class, P2Config.class);
			ctx.refresh();
			// p2 should 'win' as it was registered last
			assertThat(ctx.getBean(TestBean.class).getName(), equalTo("p2TestBean"));

			ctx.close();
		}

		{
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.register(P2Config.class, ConfigWithImplicitName.class);
			ctx.refresh();
			// p1 should 'win' as it was registered last
			assertThat(ctx.getBean(TestBean.class).getName(), equalTo("p1TestBean"));

			ctx.close();
		}
	}

	@Test
	public void withNameAndMultipleResourceLocations() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(ConfigWithNameAndMultipleResourceLocations.class);
		ctx.refresh();
		assertThat(ctx.getEnvironment().containsProperty("from.p1"), is(true));
		assertThat(ctx.getEnvironment().containsProperty("from.p2"), is(true));

		ctx.close();
	}

	@Configuration
	@PropertySource("classpath:properties/p1.properties")
	static class ConfigWithImplicitName {
		@Autowired
		Environment env;

		@Bean
		public TestBean testBean() {
			return new TestBean(env.getProperty("testbean.name"));
		}
	}

	@Configuration
	@PropertySource(name="p1", value="classpath:properties/p1.properties")
	static class ConfigWithExplicitName {
		@Autowired
		Environment env;

		@Bean
		public TestBean testBean() {
			return new TestBean(env.getProperty("testbean.name"));
		}
	}


	@Configuration
	@PropertySource("classpath:properties/p2.properties")
	static class P2Config {
	}

	@Configuration
	@PropertySource(
			name = "psName",
			value = {
					"classpath:properties/p1.properties",
					"classpath:properties/p2.properties"
			})
	static class ConfigWithNameAndMultipleResourceLocations {

		/**추가함***/
		@Autowired
		Environment env;

		@Bean
		public String TesDoubleProperties(){
			String temp = env.getProperty("testbean.name");
			return temp;
		}
		/***End***/
	}


	@Configuration
	@PropertySource(value = {})
	static class ConfigWithEmptyResourceLocations {
	}
}
