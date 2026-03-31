/*
 * Copyright 2008-2024 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.rte.fdl.crypto.config;

import org.egovframe.rte.fdl.crypto.EgovPasswordEncoder;
import org.egovframe.rte.fdl.crypto.impl.EgovARIACryptoServiceImpl;
import org.egovframe.rte.fdl.crypto.impl.EgovDigestServiceImpl;
import org.egovframe.rte.fdl.crypto.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.rte.fdl.crypto.impl.EgovGeneralCryptoServiceImpl;
import org.egovframe.rte.fdl.property.impl.EgovPropertyServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * egov-crypto Java Configuration 클래스
 *
 * <p>Desc.: crypto-config.xml을 대체하는 Java Configuration</p>
 *
 * @author 유지보수
 * @version 6.0
 * @since 2025.06.01
 */
@Configuration
public class EgovCryptoConfiguration {

    @Value("${Globals.CryptoConfigPath:}")
    private String cryptoConfigPath;

    @Autowired
    private ApplicationContext applicationContext;

    private EgovCryptoConfig cryptoConfig;

    /**
     * 설정 파일 리더 Bean. Globals.CryptoConfigPath는 서비스의 globals.properties에서 읽으며,
     * ApplicationContext를 ResourceLoader로 전달하여 서비스 classpath/리소스 기준으로 파일을 찾는다.
     */
    @Bean
    public EgovCryptoConfigReader egovCryptoConfigReader(
            @Value("${Globals.CryptoConfigPath:}") String cryptoConfigPath) {
        return new EgovCryptoConfigReader(cryptoConfigPath, applicationContext);
    }

    @Bean
    public EgovCryptoConfig egovCryptoConfig(
            @Value("${Globals.CryptoConfigPath:}") String cryptoConfigPath) {
        if (cryptoConfig == null) {
            EgovCryptoConfigReader reader = new EgovCryptoConfigReader(cryptoConfigPath, applicationContext);
            cryptoConfig = reader.readConfig();
        }
        return cryptoConfig;
    }

    @Bean(name = "egovEnvPasswordEncoderService")
    public EgovPasswordEncoder egovPasswordEncoder(EgovCryptoConfig cryptoConfig) {
        EgovPasswordEncoder egovPasswordEncoder = new EgovPasswordEncoder();
        egovPasswordEncoder.setAlgorithm(cryptoConfig.getAlgorithm());
        egovPasswordEncoder.setHashedPassword(cryptoConfig.getAlgorithmKeyHash());
        return egovPasswordEncoder;
    }

    @Bean(name = "egovARIACryptoService")
    public EgovARIACryptoServiceImpl egovARIACryptoService(EgovCryptoConfig cryptoConfig) {
        EgovARIACryptoServiceImpl egovARIACryptoService = new EgovARIACryptoServiceImpl();
        egovARIACryptoService.setPasswordEncoder(egovPasswordEncoder(cryptoConfig));
        egovARIACryptoService.setBlockSize(cryptoConfig.getCryptoBlockSize());
        return egovARIACryptoService;
    }

    @Bean(name = "egovEnvCryptoService")
    public EgovEnvCryptoServiceImpl egovEnvCryptoService(EgovCryptoConfig cryptoConfig) {
        String algorithmKey = cryptoConfig.getAlgorithmKey();
        if ("egovframe".equals(algorithmKey)) {
            System.err.println("[EgovFramework Fatal ERROR] Since a fatal security threat may occur, " +
                    "the Crypto service default algorithm Key=\"egovframe\" must be changed to another keyword. " +
                    "For more details see https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte5.0:fdl:crypto");
        }

        EgovEnvCryptoServiceImpl egovEnvCryptoService = new EgovEnvCryptoServiceImpl();
        egovEnvCryptoService.setPasswordEncoder(egovPasswordEncoder(cryptoConfig));
        egovEnvCryptoService.setCryptoService(egovARIACryptoService(cryptoConfig));
        egovEnvCryptoService.setCryptoConfigurer(egovEnvCryptoConfigurerService(cryptoConfig));
        egovEnvCryptoService.setCrypto(cryptoConfig.isCrypto());
        egovEnvCryptoService.setCryptoAlgorithm(cryptoConfig.getAlgorithm());
        egovEnvCryptoService.setCryptoAlgorithmKey(algorithmKey);
        egovEnvCryptoService.setCryptoAlgorithmKeyHash(cryptoConfig.getAlgorithmKeyHash());
        egovEnvCryptoService.setCryptoBlockSize(cryptoConfig.getCryptoBlockSize());

        return egovEnvCryptoService;
    }

    @Bean(name = "egovGeneralCryptoService")
    public EgovGeneralCryptoServiceImpl egovGeneralCryptoService(EgovCryptoConfig cryptoConfig) {
        EgovGeneralCryptoServiceImpl egovGeneralCryptoService = new EgovGeneralCryptoServiceImpl();
        egovGeneralCryptoService.setPasswordEncoder(egovPasswordEncoder(cryptoConfig));
        egovGeneralCryptoService.setAlgorithm(cryptoConfig.getAlgorithm());
        egovGeneralCryptoService.setBlockSize(cryptoConfig.getCryptoBlockSize());
        return egovGeneralCryptoService;
    }

    @Bean(name = "egovDigestService")
    public EgovDigestServiceImpl egovDigestService(EgovCryptoConfig cryptoConfig) {
        EgovDigestServiceImpl egovDigestService = new EgovDigestServiceImpl();
        egovDigestService.setAlgorithm(cryptoConfig.getAlgorithm());
        egovDigestService.setPlainDigest(cryptoConfig.isPlainDigest());
        return egovDigestService;
    }

    @Bean
    public ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource() {
        ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
        reloadableResourceBundleMessageSource.setDefaultEncoding(String.valueOf(StandardCharsets.UTF_8));
        reloadableResourceBundleMessageSource.setBasename("classpath:messages");
        return reloadableResourceBundleMessageSource;
    }

    private EgovPropertyServiceImpl egovEnvCryptoConfigurerService(EgovCryptoConfig cryptoConfig) {
        EgovPropertyServiceImpl service = new EgovPropertyServiceImpl();
        Set<Map<String, String>> extFileName = Set.of(
                Map.of("encoding", "UTF-8", "filename", cryptoConfig.getCryptoPropertyLocation())
        );
        service.setExtFileName(extFileName);
        return service;
    }

}
