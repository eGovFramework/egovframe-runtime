package org.egovframe.rte.fdl.cmmn.profiles;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
@ActiveProfiles("annotationProfile")
public class SpringAnnotationProfileTest {

	@Configuration
	@Profile("annotationProfile")
	static class EmbeddedDataSource {
		@Bean
		public DataSource annotationDataSource() {
			return new EmbeddedDatabaseBuilder()
			.setType(EmbeddedDatabaseType.HSQL)
			.addScript("classpath:/spring/schema.sql")
			.addScript("classpath:/spring/test-data.sql")
			.build();
		}
	}

	@Resource(name="annotationDataSource")
	private DataSource dataSource;

	@Test
	public void testAnnotationProfile() {
		assertNotNull(dataSource instanceof EmbeddedDatabaseBuilder);
	}
}
