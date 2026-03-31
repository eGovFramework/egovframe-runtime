package org.egovframe.rte.fdl.crypto.config;

import org.egovframe.rte.fdl.crypto.EgovCryptoService;
import org.egovframe.rte.fdl.crypto.EgovEnvCryptoService;
import org.egovframe.rte.fdl.crypto.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.rte.fdl.property.EgovPropertyService;
import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Test Crypto Java Configuration 클래스
 * <p>
 * 테스트 시 globals.properties를 직접 읽어 Globals.CryptoConfigPath 값을 사용하고,
 * 해당 경로의 프로퍼티로 EgovCryptoConfig 빈을 생성한다.
 * (globals.properties → CryptoConfigPath → 해당 경로 설정 파일 로드)
 * EgovCryptoConfiguration의 egovCryptoConfig 대신 이 빈이 사용되도록 @Primary 적용.
 * </p>
 * <p>
 * - egovPropertyConfigurer: 암호화된 DB 정보가 있는 프로퍼티 파일(cryptoPropertyLocation) 로드
 * - EgovEnvCryptoService 초기화(init) 후 데이터소스 등에서 복호화된 값 사용
 * </p>
 *
 * @author 유지보수
 * @version 5.0
 * @since 2025.06.01
 */
@Configuration
@Import(EgovCryptoConfiguration.class)
public class EgovCryptoTestConfig {

    private static final String GLOBALS_PROPERTIES = "egovframework/egovProps/globals.properties";

    @Bean
    @Primary
    public EgovCryptoConfig egovCryptoConfig(ApplicationContext applicationContext) throws Exception {
        Properties globals = PropertiesLoaderUtils.loadProperties(new ClassPathResource(GLOBALS_PROPERTIES));
        String cryptoConfigPath = globals.getProperty("Globals.CryptoConfigPath");
        if (cryptoConfigPath == null || cryptoConfigPath.isEmpty()) {
            cryptoConfigPath = "classpath:egovframework/conf/egov-crypto-config.properties";
        } else if (!cryptoConfigPath.startsWith("classpath:") && !cryptoConfigPath.startsWith("file:")) {
            cryptoConfigPath = "classpath:" + cryptoConfigPath.trim();
        } else {
            cryptoConfigPath = cryptoConfigPath.trim();
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = applicationContext.getClassLoader();
        }
        ResourceLoader resourceLoader = new DefaultResourceLoader(cl);
        EgovCryptoConfigReader reader = new EgovCryptoConfigReader(cryptoConfigPath, resourceLoader);
        return reader.readConfig();
    }

    /**
     * 암호화된 DB 연결 정보가 저장된 프로퍼티를 로드하는 서비스 (테스트에서 egovPropertyConfigurer 이름으로 주입).
     * Spring 빈으로 등록되어 afterPropertiesSet()으로 프로퍼티 파일이 로드된다.
     */
    @Bean(name = "egovPropertyConfigurer")
    public EgovPropertyService egovPropertyConfigurer(EgovCryptoConfig cryptoConfig) {
        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        Set<Map<String, String>> extFileName = Set.of(
                Map.of("encoding", "UTF-8", "filename", cryptoConfig.getCryptoPropertyLocation())
        );
        service.setExtFileName(extFileName);
        return service;
    }

    /**
     * EgovCryptoConfiguration이 주입하는 PropertyService는 빈이 아니라 afterPropertiesSet()이 호출되지 않아
     * getConfiguration()이 null이 된다. 따라서 초기화 시점에 egovPropertyConfigurer를 주입한 뒤 init() 호출.
     */
    @Bean
    public EgovEnvCryptoServiceInitializer egovEnvCryptoServiceInitializer(
            EgovEnvCryptoServiceImpl egovEnvCryptoService,
            @Qualifier("egovPropertyConfigurer") EgovPropertyService egovPropertyConfigurer) {
        return new EgovEnvCryptoServiceInitializer(egovEnvCryptoService, egovPropertyConfigurer);
    }

    @Bean(name = "cryptoConfig")
    public EgovCryptoConfig cryptoConfig(EgovCryptoConfig egovCryptoConfig) {
        return egovCryptoConfig;
    }

    @Bean(name = "egov.envCryptoService")
    public EgovEnvCryptoService egovEnvCryptoServiceAlias(EgovEnvCryptoServiceImpl egovEnvCryptoService) {
        return egovEnvCryptoService;
    }

    @Bean(name = "egov.ariaCryptoService")
    public EgovCryptoService egovARIACryptoServiceAlias(
            org.egovframe.rte.fdl.crypto.impl.EgovARIACryptoServiceImpl egovARIACryptoService) {
        return egovARIACryptoService;
    }

    @Bean(name = "egov.digestService")
    public org.egovframe.rte.fdl.crypto.EgovDigestService egovDigestServiceAlias(
            org.egovframe.rte.fdl.crypto.impl.EgovDigestServiceImpl egovDigestService) {
        return egovDigestService;
    }

    /**
     * Jasypt PBE는 SecretKeyFactory 알고리즘명(PBEWithSHA1AndDESede 등)이 필요하나,
     * 설정 파일의 algorithm=SHA-256은 MessageDigest용이라 GeneralCryptoService에서 오류가 난다.
     * 테스트에서만 PBE 알고리즘으로 오버라이드한다.
     */
    @Bean(name = "generalCryptoService")
    @Primary
    public org.egovframe.rte.fdl.crypto.impl.EgovGeneralCryptoServiceImpl generalCryptoServiceForTest(
            org.egovframe.rte.fdl.crypto.impl.EgovGeneralCryptoServiceImpl egovGeneralCryptoService) {
        egovGeneralCryptoService.setAlgorithm("PBEWithSHA1AndDESede");
        return egovGeneralCryptoService;
    }

    @Bean(name = "password")
    public String password(EgovCryptoConfig cryptoConfig) {
        return cryptoConfig.getAlgorithmKey();
    }

    private static final class EgovEnvCryptoServiceInitializer implements InitializingBean {
        private final EgovEnvCryptoServiceImpl service;
        private final EgovPropertyService propertyConfigurer;

        EgovEnvCryptoServiceInitializer(EgovEnvCryptoServiceImpl service, EgovPropertyService propertyConfigurer) {
            this.service = service;
            this.propertyConfigurer = propertyConfigurer;
        }

        @Override
        public void afterPropertiesSet() {
            service.setCryptoConfigurer(propertyConfigurer);
            service.init();
        }
    }
}
