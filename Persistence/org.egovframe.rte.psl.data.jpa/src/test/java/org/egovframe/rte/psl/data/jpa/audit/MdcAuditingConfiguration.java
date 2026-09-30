package org.egovframe.rte.psl.data.jpa.audit;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * MDC 감사자 테스트 구성 — {@code @EnableJpaAuditing} + {@link EgovMdcAuditorAware} 빈. 기존 {@code JpaConfiguration} 의 컴포넌트 스캔에
 * 잡히지 않도록 {@code @Configuration} 을 붙이지 않는다(lite 구성 — {@code @ContextConfiguration} 이 직접 등록). 임베디드 DB 는 고유 이름을 써
 * 다른 컨텍스트의 {@code create-drop} 과 격리한다.
 */
@EnableJpaRepositories(basePackages = "org.egovframe.rte.psl.data.jpa.crud")
@EnableJpaAuditing
public class MdcAuditingConfiguration {

	@Bean
	public AuditorAware<String> egovAuditorAware() {
		return new EgovMdcAuditorAware();
	}

	@Bean
	public DataSource dataSource() {
		return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.HSQL).generateUniqueName(true).build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setDatabasePlatform("org.hibernate.dialect.HSQLDialect");
		vendorAdapter.setGenerateDdl(true);

		Properties properties = new Properties();
		properties.put("hibernate.hbm2ddl.auto", "create-drop");

		LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
		emf.setDataSource(dataSource);
		emf.setPackagesToScan("org.egovframe.rte.psl.data.jpa.crud");
		emf.setJpaVendorAdapter(vendorAdapter);
		emf.setJpaProperties(properties);
		// JpaConfiguration 과 같은 이유 — Hibernate SessionFactory 와 JPA 3.2 SchemaManager 의 시그니처 충돌 회피
		emf.setEntityManagerFactoryInterface(EntityManagerFactory.class);
		return emf;
	}

	@Bean
	public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
