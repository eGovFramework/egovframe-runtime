package egovframework.rte.fdl.cmmn.aspectj;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AppConfig {
	@Bean
	public SampleService getSampleService() {
		return new SampleServiceImpl();
	}
}
