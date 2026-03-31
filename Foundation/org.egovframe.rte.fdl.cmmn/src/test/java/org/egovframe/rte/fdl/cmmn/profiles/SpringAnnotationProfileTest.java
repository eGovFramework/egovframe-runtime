package org.egovframe.rte.fdl.cmmn.profiles;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@ActiveProfiles("annotationProfile")
public class SpringAnnotationProfileTest {

    @Resource(name = "annotationDataSource")
    private DataSource dataSource;

    @Test
    public void testAnnotationProfile() {
        assertNotNull(dataSource);
    }

    @Configuration
    @Profile("annotationProfile")
    static class EmbeddedDataSource {
        @Bean
        public DataSource annotationDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .addScript("classpath:/META-INF/testdata/testdb.sql")
                    .build();
        }
    }

}
