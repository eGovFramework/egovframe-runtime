package org.egovframe.rte.psl.data.mongodb;

import org.egovframe.rte.psl.data.mongodb.connect.EgovMongodbConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ComponentScan(basePackages = "org.egovframe.rte.psl.data.mongodb")
@EnableMongoRepositories
public class MongoDbConfiguration {

    private String mongoDbName = "com";
    private String mongoDbUrl = "mongodb://com:com01@localhost:27017/com";

    @Bean(name = "mongoDatabaseFactory")
    public MongoDatabaseFactory mongoDatabaseFactory() {
        EgovMongodbConnectionFactory egovMongodbConnectionFactory = new EgovMongodbConnectionFactory(this.mongoDbName, this.mongoDbUrl);
        return egovMongodbConnectionFactory.mongoDbFactory();
    }

}
