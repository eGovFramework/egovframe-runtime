package org.egovframe.rte.fdl.security.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.access.method.DelegatingMethodSecurityMetadataSource;
import org.springframework.security.config.BeanIds;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * 테스트 시 globals.properties의 Globals.SecurityConfigPath 값을 읽어
 * 해당 경로의 프로퍼티로 EgovSecurityConfig 빈을 생성한다.
 * (globals.properties → SecurityConfigPath → 해당 경로 설정 파일 로드)
 * EgovSecurityConfiguration의 egovSecurityConfig 대신 이 빈이 사용되도록 @Primary 적용.
 * <p>
 * 테스트에서 BeanIds.AUTHENTICATION_MANAGER, FilterChainProxy, delegatingMethodSecurityMetadataSource
 * 이름으로 조회할 수 있도록 테스트 전용 빈/별칭만 등록한다. (실제 서비스 설정에는 영향 없음)
 * </p>
 *
 * @author 유지보수
 * @version 5.0
 * @since 2025.06.01
 */
@Configuration
public class EgovSecurityTestConfig {

    private static final String GLOBALS_PROPERTIES = "egovframework/egovProps/globals.properties";

    @Bean
    @Primary
    public EgovSecurityConfig egovSecurityConfig(ApplicationContext applicationContext) throws Exception {
        Properties globals = PropertiesLoaderUtils.loadProperties(new ClassPathResource(GLOBALS_PROPERTIES));
        String securityConfigPath = globals.getProperty("Globals.SecurityConfigPath");
        if (securityConfigPath == null || securityConfigPath.isEmpty()) {
            securityConfigPath = "classpath:egovframework/conf/egov-security-config.properties";
        } else if (!securityConfigPath.startsWith("classpath:") && !securityConfigPath.startsWith("file:")) {
            securityConfigPath = "classpath:" + securityConfigPath.trim();
        } else {
            securityConfigPath = securityConfigPath.trim();
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = applicationContext.getClassLoader();
        }
        ResourceLoader resourceLoader = new DefaultResourceLoader(cl);
        EgovSecurityConfigReader reader = new EgovSecurityConfigReader(securityConfigPath, resourceLoader);
        return reader.readConfig();
    }

    /**
     * 테스트에서 BeanIds.AUTHENTICATION_MANAGER 이름으로 조회할 수 있도록
     * authenticationManager 빈에 대한 별칭만 등록. (동일 타입 빈 중복 방지)
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor testSecurityBeanAliasRegistrar() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
                registry.registerAlias("authenticationManager", BeanIds.AUTHENTICATION_MANAGER);
            }

            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            }
        };
    }

    /**
     * 테스트에서 BeanIds.SPRING_SECURITY_FILTER_CHAIN / BeanIds.FILTER_CHAIN_PROXY 이름으로 조회할 수 있도록 등록.
     */
    @Bean(name = { BeanIds.SPRING_SECURITY_FILTER_CHAIN, BeanIds.FILTER_CHAIN_PROXY })
    public FilterChainProxy springSecurityFilterChain(SecurityFilterChain securityFilterChain) {
        return new FilterChainProxy(List.of(securityFilterChain));
    }

    /**
     * 테스트에서 "delegatingMethodSecurityMetadataSource" 이름으로 조회할 수 있도록 등록.
     * 메서드 시큐리티 미사용 시 빈 목록으로 생성.
     */
    @Bean(name = "delegatingMethodSecurityMetadataSource")
    public DelegatingMethodSecurityMetadataSource delegatingMethodSecurityMetadataSource() {
        return new DelegatingMethodSecurityMetadataSource(Collections.emptyList());
    }
}
