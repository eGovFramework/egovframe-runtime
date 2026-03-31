package org.egovframe.rte.fdl.idgnr.config;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TableIdGenerationConfig {

    private final DataSource dataSource;

    public TableIdGenerationConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Table 기반 ID 생성기
     */
    @Bean(name = "strategy")
    public EgovIdGnrStrategyImpl strategy() {
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl();
        strategy.setPrefix("TEST-");
        strategy.setCipers(5);
        strategy.setFillChar('*');
        return strategy;
    }

    @Bean(name = "basicService", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl basicService() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("id");
        return generator;
    }

    @Bean(name = "Ids-TestSimpleRequestIdsSize1", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestSimpleRequestIdsSize1() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(1);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestSimpleRequestIdsSize10", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestSimpleRequestIdsSize10() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestSimpleRequestIdsSize100", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestSimpleRequestIdsSize100() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(100);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestBigDecimalRequestIdsSize10", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestBigDecimalRequestIdsSize10() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setUseBigDecimals(true);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestMaxByteIds", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestMaxByteIds() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestMaxShortIds", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestMaxShortIds() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestMaxIntegerIds", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestMaxIntegerIds() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestMaxLongIds", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestMaxLongIds() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(10);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestNotDefinedTableInfo", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestNotDefinedTableInfo() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(1);
        return generator;
    }

    @Bean(name = "Ids-TestWithGenerationStrategy", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestWithGenerationStrategy() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setStrategy(strategy());
        generator.setBlockSize(1);
        generator.setTable("idttest");
        generator.setTableName("test");
        return generator;
    }

    @Bean(name = "Ids-TestNonExistingTableName", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestNonExistingTableName() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(1);
        generator.setTable("idttest");
        generator.setTableName("does_not_exist");
        return generator;
    }

    @Bean(name = "Ids-TestWithColumnName", destroyMethod = "destroy")
    public EgovTableIdGnrServiceImpl idsTestWithColumnName() {
        EgovTableIdGnrServiceImpl generator = new EgovTableIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setBlockSize(1);
        generator.setTable("idttest");
        generator.setTableName("test");
        generator.setTableNameFieldName("table_name");
        generator.setNextIdFieldName("next_id");
        return generator;
    }

}
