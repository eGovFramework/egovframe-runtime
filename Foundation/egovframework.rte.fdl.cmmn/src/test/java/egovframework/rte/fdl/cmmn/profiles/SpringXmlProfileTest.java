package egovframework.rte.fdl.cmmn.profiles;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/spring/context-profiles.xml" })
public class SpringXmlProfileTest {
	
	@Resource(name="xmlDataSource")
	private DataSource dataSource;

	@Test
	public void testXmlConf() {
		Assert.notNull(dataSource);
	}	

	@Test
	public void testXmlProfile() {
		GenericXmlApplicationContext context = new GenericXmlApplicationContext();
		context.getEnvironment().setActiveProfiles("poc");
		context.load("classpath:/spring/context-profiles.xml");
		context.refresh();
		
		Assert.notNull(context.getBean("dataSource"));
		
		context.close();
	}
}
