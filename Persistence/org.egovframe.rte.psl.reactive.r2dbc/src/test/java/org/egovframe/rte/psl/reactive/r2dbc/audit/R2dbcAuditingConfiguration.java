package org.egovframe.rte.psl.reactive.r2dbc.audit;

import io.r2dbc.spi.ConnectionFactory;
import org.egovframe.rte.psl.reactive.r2dbc.connect.EgovR2dbcConnectionFactory;
import org.egovframe.rte.psl.reactive.r2dbc.repository.EgovR2dbcRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/**
 * R2DBC 감사 테스트 구성 — {@code @EnableR2dbcAuditing} + Context 감사자 + 실행환경 리포지토리 빈(컨텍스트 관리 → 엔티티 콜백 적용).
 * {@link AbstractR2dbcConfiguration} 이 감사 등록기가 참조하는 {@code r2dbcMappingContext} 빈을 제공한다.
 */
@Configuration
@EnableR2dbcAuditing
public class R2dbcAuditingConfiguration extends AbstractR2dbcConfiguration {

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        return new EgovR2dbcConnectionFactory("r2dbc:h2:mem:///auditdb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE").connectionFactory();
    }

    @Bean
    public ConnectionFactoryInitializer auditingInitializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("META-INF/script/r2dbc-h2-auditing.sql")));
        return initializer;
    }

    @Bean
    public EgovContextReactiveAuditorAware reactiveAuditorAware() {
        return new EgovContextReactiveAuditorAware();
    }

    @Bean
    public EgovR2dbcRepository<AuditedSample> auditedSampleRepository(ConnectionFactory connectionFactory) {
        return new EgovR2dbcRepository<>(connectionFactory);
    }
}
