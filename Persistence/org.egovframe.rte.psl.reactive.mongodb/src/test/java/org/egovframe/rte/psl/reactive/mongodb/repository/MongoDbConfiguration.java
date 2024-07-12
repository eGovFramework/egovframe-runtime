package org.egovframe.rte.psl.reactive.mongodb.repository;

import org.egovframe.rte.psl.reactive.mongodb.connect.EgovMongoDbConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@Configuration
@EnableWebFlux
@ComponentScan(basePackages="org.egovframe.rte.psl.reactive.mongodb.repository")
@EnableMongoRepositories
public class MongoDbConfiguration {

    private String mongoDbName = "com";
    private String mongoDbUrl = "mongodb://com:com01@localhost:27017/com";

    @Bean(name="reactiveMongoDatabaseFactory")
    public ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory() {
        EgovMongoDbConnectionFactory egovMongoDbConnectionFactory = new EgovMongoDbConnectionFactory(this.mongoDbName, this.mongoDbUrl);
        return egovMongoDbConnectionFactory.reactiveMongoDatabaseFactory();
    }

}
