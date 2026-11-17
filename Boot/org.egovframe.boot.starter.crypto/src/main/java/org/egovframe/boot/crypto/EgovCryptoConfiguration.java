/*
 * Copyright 2009-2026 MOIS(Ministry of the Interior and Safety).
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
package org.egovframe.boot.crypto;

import org.egovframe.boot.crypto.service.impl.EgovARIACryptoServiceImpl;
import org.egovframe.boot.crypto.service.impl.EgovDigestServiceImpl;
import org.egovframe.boot.crypto.service.impl.EgovEnvCryptoServiceImpl;
import org.egovframe.boot.crypto.service.impl.EgovGeneralCryptoServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.StringUtils;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 암호화 서비스 자동 구성.
 * <p>@Bean 간 의존은 메서드 파라미터로 주입받는다(proxyBeanMethods = false).</p>
 *
 * @author 유지보수
 * @version 5.1
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일       수정자    수정내용
 * ------------------------------------------------------------
 * 2025.06.01   유지보수  최초 생성
 * 2026.10.23   유지보수  @AutoConfiguration 전환, @Bean 메서드 파라미터 주입, DB 접속 정보 복호화 설정, 메시지 basename 수정
 * </pre>
 * @since 2025.06.01
 */
@AutoConfiguration
@EnableConfigurationProperties(EgovCryptoProperties.class)
public class EgovCryptoConfiguration {

    private final EgovCryptoProperties properties;

    public EgovCryptoConfiguration(EgovCryptoProperties properties) {
        this.properties = properties;
    }

    @Bean(name = "egovEnvPasswordEncoderService")
    public EgovPasswordEncoder egovPasswordEncoder() {
        EgovPasswordEncoder egovPasswordEncoder = new EgovPasswordEncoder();
        egovPasswordEncoder.setAlgorithm(properties.getAlgorithm());
        egovPasswordEncoder.setHashedPassword(properties.getAlgorithmKeyHash());
        return egovPasswordEncoder;
    }

    @Bean(name = "egovARIACryptoService")
    public EgovARIACryptoServiceImpl egovARIACryptoService(
            @Qualifier("egovEnvPasswordEncoderService") EgovPasswordEncoder egovPasswordEncoder) {
        EgovARIACryptoServiceImpl egovARIACryptoService = new EgovARIACryptoServiceImpl();
        egovARIACryptoService.setPasswordEncoder(egovPasswordEncoder);
        egovARIACryptoService.setBlockSize(properties.getCryptoBlockSize());
        return egovARIACryptoService;
    }

    @Bean(name = "egovEnvCryptoService")
    public EgovEnvCryptoServiceImpl egovEnvCryptoService(
            @Qualifier("egovEnvPasswordEncoderService") EgovPasswordEncoder egovPasswordEncoder,
            @Qualifier("egovARIACryptoService") EgovARIACryptoServiceImpl egovARIACryptoService,
            Environment environment) {
        String algorithmKey = properties.getAlgorithmKey();
        if ("egovframe".equals(algorithmKey)) {
            System.err.println("[EgovFramework Fatal ERROR] Since a fatal security threat may occur, " +
                    "the Crypto service default algorithm Key=\"egovframe\" must be changed to another keyword. " +
                    "For more details see https://www.egovframe.go.kr/wiki/doku.php?id=egovframework:rte5.0:fdl:crypto");
        }

        EgovEnvCryptoServiceImpl egovEnvCryptoService = new EgovEnvCryptoServiceImpl();
        egovEnvCryptoService.setEgovPasswordEncoder(egovPasswordEncoder);
        egovEnvCryptoService.setCryptoService(egovARIACryptoService);
        egovEnvCryptoService.setCryptoAlgorithm(properties.getAlgorithm());
        egovEnvCryptoService.setCyptoAlgorithmKey(properties.getAlgorithmKey());
        egovEnvCryptoService.setCyptoAlgorithmKeyHash(properties.getAlgorithmKeyHash());
        egovEnvCryptoService.setCryptoBlockSize(properties.getCryptoBlockSize());
        egovEnvCryptoService.setCrypto(properties.isCrypto());
        egovEnvCryptoService.setPropertyResolver(databasePropertyResolver(environment));
        egovEnvCryptoService.init();
        return egovEnvCryptoService;
    }

    @Bean(name = "egovGeneralCryptoService")
    public EgovGeneralCryptoServiceImpl egovGeneralCryptoService(
            @Qualifier("egovEnvPasswordEncoderService") EgovPasswordEncoder egovPasswordEncoder) {
        EgovGeneralCryptoServiceImpl egovGeneralCryptoService = new EgovGeneralCryptoServiceImpl();
        egovGeneralCryptoService.setPasswordEncoder(egovPasswordEncoder);
        return egovGeneralCryptoService;
    }

    @Bean(name = "egovDigestService")
    public EgovDigestServiceImpl egovDigestService() {
        EgovDigestServiceImpl egovDigestService = new EgovDigestServiceImpl();
        egovDigestService.setAlgorithm(properties.getAlgorithm());
        egovDigestService.setPlainDigest(properties.isPlainDigest());
        return egovDigestService;
    }

    @Bean
    public ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource() {
        ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
        reloadableResourceBundleMessageSource.setDefaultEncoding(String.valueOf(StandardCharsets.UTF_8));
        // 애플리케이션의 message(s).properties 와 충돌하지 않도록 messages/ 아래 별도 이름 사용
        reloadableResourceBundleMessageSource.setBasename("classpath:messages/message-crypto");
        return reloadableResourceBundleMessageSource;
    }

    /** DB 접속 항목 조회용 PropertyResolver. cryptoPropertyLocation 파일(UTF-8) 우선, 없는 키는 Environment. */
    private PropertyResolver databasePropertyResolver(Environment environment) {
        String location = properties.getCryptoPropertyLocation();
        if (!StringUtils.hasText(location)) {
            return environment;
        }
        Resource resource = new DefaultResourceLoader().getResource(location.trim());
        Properties fileProperties;
        try {
            fileProperties = PropertiesLoaderUtils.loadProperties(new EncodedResource(resource, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read org.egovframe.crypto.cryptoPropertyLocation : " + location, e);
        }
        MutablePropertySources sources = new MutablePropertySources();
        sources.addFirst(new PropertiesPropertySource("egovCryptoPropertyLocation", fileProperties));
        if (environment instanceof ConfigurableEnvironment configurable) {
            configurable.getPropertySources().forEach(sources::addLast);
        }
        return new PropertySourcesPropertyResolver(sources);
    }

}
