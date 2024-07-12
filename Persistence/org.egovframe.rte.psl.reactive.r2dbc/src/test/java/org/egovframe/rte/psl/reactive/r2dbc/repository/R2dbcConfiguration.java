package org.egovframe.rte.psl.reactive.r2dbc.repository;

import io.r2dbc.spi.ConnectionFactory;
import org.egovframe.rte.psl.reactive.r2dbc.connect.EgovR2dbcConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@ComponentScan(basePackages="org.egovframe.rte.psl.reactive.r2dbc.repository")
@EnableR2dbcRepositories
public class R2dbcConfiguration {

    private String r2dbcUrl = "r2dbc:h2:mem:///sampledb?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    @Bean(name="connectionFactory")
    public ConnectionFactory connectionFactory() {
        EgovR2dbcConnectionFactory egovR2dbcConnectionFactory = new EgovR2dbcConnectionFactory(this.r2dbcUrl);
        return egovR2dbcConnectionFactory.connectionFactory();
    }

    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("script/r2dbc-h2.sql")));
        return initializer;
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

}
