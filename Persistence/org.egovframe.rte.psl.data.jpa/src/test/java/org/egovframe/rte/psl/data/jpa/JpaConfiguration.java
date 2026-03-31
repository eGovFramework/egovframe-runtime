package org.egovframe.rte.psl.data.jpa;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan(basePackages = "org.egovframe.rte.psl.data.jpa")
@EnableJpaRepositories(basePackages = "org.egovframe.rte.psl.data.jpa")
public class JpaConfiguration {

    private final Environment env;

    public JpaConfiguration(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        var vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabasePlatform(env.getProperty("jpa.database", "org.hibernate.dialect.H2Dialect"));
        vendorAdapter.setGenerateDdl(Boolean.parseBoolean(env.getProperty("jpa.generateDdl", "true")));
        vendorAdapter.setShowSql(Boolean.parseBoolean(env.getProperty("jpa.showSql", "false")));

        var properties = new Properties();
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.hbm2ddl.auto", "create-drop");
        properties.put("hibernate.id.new_generator_mappings", "true");

        var emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("org.egovframe.rte.psl.data.jpa");
        emf.setJpaVendorAdapter(vendorAdapter);
        emf.setJpaProperties(properties);
        emf.setEntityManagerFactoryInterface(jakarta.persistence.EntityManagerFactory.class);
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }

}
