package org.egovframe.rte.itl.integration.config;

import org.egovframe.rte.itl.integration.metadata.dao.hibernate.*;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class HibernateContextConfig {

    @Bean(name = "dataSource")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean(name = "sessionFactory")
    public LocalSessionFactoryBean sessionFactory() {
        Properties properties = new Properties();
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.id.new_generator_mappings", "true");
        properties.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");

        LocalSessionFactoryBean factoryBean = new LocalSessionFactoryBean();
        factoryBean.setDataSource(dataSource());
        factoryBean.setPackagesToScan("org.egovframe.rte.itl.integration");
        factoryBean.setMappingResources(
                "/org/egovframe/rte/itl/integration/metadata/dao/hibernate/OrganizationDefinition.hbm.xml",
                "/org/egovframe/rte/itl/integration/metadata/dao/hibernate/SystemDefinition.hbm.xml",
                "/org/egovframe/rte/itl/integration/metadata/dao/hibernate/ServiceDefinition.hbm.xml",
                "/org/egovframe/rte/itl/integration/metadata/dao/hibernate/RecordTypeDefinition.hbm.xml",
                "/org/egovframe/rte/itl/integration/metadata/dao/hibernate/IntegrationDefinition.hbm.xml"
        );
        factoryBean.setHibernateProperties(properties);

        return factoryBean;
    }

    @Bean(name = "transactionManager")
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

    @Bean(name = "hibernateIntegrationDefinitionDao")
    public HibernateIntegrationDefinitionDao hibernateIntegrationDefinitionDao() {
        return new HibernateIntegrationDefinitionDao();
    }

    @Bean(name = "hibernateOrganizationDefinitionDao")
    public HibernateOrganizationDefinitionDao hibernateOrganizationDefinitionDao() {
        return new HibernateOrganizationDefinitionDao();
    }

    @Bean(name = "hibernateRecordTypeDefinitionDao")
    public HibernateRecordTypeDefinitionDao hibernateRecordTypeDefinitionDao() {
        return new HibernateRecordTypeDefinitionDao();
    }

    @Bean(name = "hibernateServiceDefinitionDao")
    public HibernateServiceDefinitionDao hibernateServiceDefinitionDao() {
        return new HibernateServiceDefinitionDao();
    }

    @Bean(name = "hibernateSystemDefinitionDao")
    public HibernateSystemDefinitionDao hibernateSystemDefinitionDao() {
        return new HibernateSystemDefinitionDao();
    }

}
