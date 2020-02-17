package egovframework.rte.fdl.property.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 
 * @author 이영지
 *
 */
//DB기반의 PropertySource의 경우, 코드상에서 property사용하는 경우
@Configuration
public class PropertySourceTestInCode {

	@Autowired
	Environment env;
	
	/**
	 * INSERT INTO PROPERTY (PKEY,PVALUE) VALUES ('pkey01','config01');
	 * INSERT INTO PROPERTY (PKEY,PVALUE) VALUES ('pkey02','config02');
	 */
	@Bean
	public String config01() {
		String config = env.getProperty("egov.test.sample01");
		System.out.println("config: " + config);
		return config;
	}
}
