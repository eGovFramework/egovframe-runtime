package org.egovframe.rte.fdl.idgnr.config;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.impl.EgovUUIdGnrServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.NoSuchAlgorithmException;

@Configuration
public class UUIdGenerationConfig {

    /**
     * UUID 기반 ID 생성기
     */
    @Bean(name = "UUIdGenerationService")
    public EgovUUIdGnrServiceImpl uuIdGenerationService() throws FdlException, NoSuchAlgorithmException {
        EgovUUIdGnrServiceImpl generator = new EgovUUIdGnrServiceImpl();
        generator.setAddress("00:00:F0:79:19:5B");
        return generator;
    }

    @Bean(name = "UUIdGenerationServiceWithoutAddress")
    public EgovUUIdGnrServiceImpl uuIdGenerationServiceWithoutAddress() {
        return new EgovUUIdGnrServiceImpl();
    }

    @Bean(name = "UUIdGenerationServiceWithIP")
    public EgovUUIdGnrServiceImpl uuIdGenerationServiceWithIP() throws FdlException, NoSuchAlgorithmException {
        EgovUUIdGnrServiceImpl generator = new EgovUUIdGnrServiceImpl();
        generator.setAddress("100.128.120.107");
        return generator;
    }

}
