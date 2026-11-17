/*
 * Copyright 2008-2019 MOIS(Ministry of the Interior and Safety).
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableConfigurationProperties(EgovCryptoProperties.class)
public class EgovCryptoConfiguration {

    private final EgovCryptoProperties properties;

    public EgovCryptoConfiguration(EgovCryptoProperties properties) {
        this.properties = properties;
    }

    @Bean(name="egovEnvPasswordEncoderService")
    public EgovPasswordEncoder egovPasswordEncoder() {
        EgovPasswordEncoder egovPasswordEncoder = new EgovPasswordEncoder();
        egovPasswordEncoder.setAlgorithm(properties.getAlgorithm());
        egovPasswordEncoder.setHashedPassword(properties.getAlgorithmKeyHash());
        return egovPasswordEncoder;
    }

    @Bean(name="egovEnvARIACryptoService")
    public EgovARIACryptoServiceImpl egovARIACryptoService() {
        EgovARIACryptoServiceImpl egovARIACryptoService = new EgovARIACryptoServiceImpl();
        egovARIACryptoService.setPasswordEncoder(egovPasswordEncoder());
        egovARIACryptoService.setBlockSize(properties.getCryptoBlockSize());
        return egovARIACryptoService;
    }

    @Bean(name="egovEnvCryptoService")
    public EgovEnvCryptoServiceImpl egovEnvCryptoService() {
        EgovEnvCryptoServiceImpl egovEnvCryptoService = new EgovEnvCryptoServiceImpl();
        egovEnvCryptoService.setEgovPasswordEncoder(egovPasswordEncoder());
        egovEnvCryptoService.setCryptoService(egovARIACryptoService());
        egovEnvCryptoService.setCrypto(properties.isCrypto());
        egovEnvCryptoService.setCryptoDBDriver(properties.getCryptoDBDriver());
        egovEnvCryptoService.setCryptoDBUrl(properties.getCryptoDBUrl());
        egovEnvCryptoService.setCryptoDBUsername(properties.getCryptoDBUsername());
        egovEnvCryptoService.setCryptoDBPassword(properties.getCryptoDBPassword());
        egovEnvCryptoService.setCryptoAlgorithm(properties.getAlgorithm());
        egovEnvCryptoService.setCyptoAlgorithmKey(properties.getAlgorithmKey());
        egovEnvCryptoService.setCyptoAlgorithmKeyHash(properties.getAlgorithmKeyHash());
        egovEnvCryptoService.setCryptoBlockSize(properties.getCryptoBlockSize());
        return egovEnvCryptoService;
    }

    @Bean(name="egovGeneralCryptoService")
    public EgovGeneralCryptoServiceImpl egovGeneralCryptoService() {
        EgovGeneralCryptoServiceImpl egovGeneralCryptoService =  new EgovGeneralCryptoServiceImpl();
        egovGeneralCryptoService.setPasswordEncoder(egovPasswordEncoder());
        return egovGeneralCryptoService;
    }

    @Bean(name="egovDigestService")
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
        reloadableResourceBundleMessageSource.setBasename("classpath:messages");
        return reloadableResourceBundleMessageSource;
    }

}
