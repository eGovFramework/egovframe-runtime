package org.egovframe.rte.psl.reactive.cassandra.repository;

import org.egovframe.rte.psl.reactive.cassandra.connect.EgovCassandraConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.ReactiveSession;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@ComponentScan(basePackages="org.egovframe.rte.psl.reactive.cassandra.repository")
@EnableCassandraRepositories
public class CassandraConfiguration {

    private String dataCenterName = "datacenter1";
    private String keyspaceName = "com";
    private String contactPoints = "localhost";
    private int port = 9042;
    private String username = "com";
    private String password = "com01";

    @Bean(name="reactiveSession")
    public ReactiveSession reactiveSession() {
        EgovCassandraConfiguration egovCassandraConfiguration = new EgovCassandraConfiguration();
        egovCassandraConfiguration.setDataCenterName(this.dataCenterName);
        egovCassandraConfiguration.setKeyspaceName(this.keyspaceName);
        egovCassandraConfiguration.setContactPoint(this.contactPoints);
        egovCassandraConfiguration.setPort(this.port);
        egovCassandraConfiguration.setUsername(this.username);
        egovCassandraConfiguration.setPassword(this.password);
        return egovCassandraConfiguration.reactiveSession();
    }

}
