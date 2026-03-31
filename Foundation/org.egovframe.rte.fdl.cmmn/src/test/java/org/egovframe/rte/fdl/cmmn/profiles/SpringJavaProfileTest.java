package org.egovframe.rte.fdl.cmmn.profiles;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.config.ProfileDataSourceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProfileDataSourceConfig.class)
public class SpringJavaProfileTest {

    @Resource(name = "commonDataSource")
    private DataSource dataSource;

    @Test
    public void testXmlConf() {
        assertNotNull(dataSource, "DataSource should not be null");
    }

    @Test
    public void testJavaProfile() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("poc");
        context.register(ProfileDataSourceConfig.class); // Java 기반 @Configuration 클래스 등록
        context.refresh();

        Assert.notNull(context.getBean("dataSource"), "dataSource bean should not be null");

        context.close();
    }

}
