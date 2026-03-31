package org.egovframe.rte.fdl.crypto.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.egovframe.rte.fdl.crypto.EgovEnvCryptoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Test Crypto DataSource Configuration
 * <p>
 * Globals.CryptoConfigPath 기반으로 설정된 crypto 빈 이후에,
 * EgovEnvCryptoService가 제공하는 복호화된 DB 연결 정보로 DataSource를 생성한다.
 * </p>
 *
 * @author 유지보수
 * @version 5.0
 * @since 2025.06.01
 */
@Configuration
public class EgovCryptoTestDatasource {

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(EgovEnvCryptoService egovEnvCryptoService) throws Exception {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(egovEnvCryptoService.getDriverClassName());
        ds.setUrl(egovEnvCryptoService.getUrl());
        ds.setUsername(egovEnvCryptoService.getUsername());
        ds.setPassword(egovEnvCryptoService.getPassword() != null ? egovEnvCryptoService.getPassword() : "");
        ds.setDefaultAutoCommit(true);
        ds.setPoolPreparedStatements(true);

        try (Connection conn = ds.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("META-INF/testdata/testdb.sql"));
        } catch (Exception e) {
            throw new IllegalStateException("Test database initialization failed", e);
        }

        return ds;
    }
}
