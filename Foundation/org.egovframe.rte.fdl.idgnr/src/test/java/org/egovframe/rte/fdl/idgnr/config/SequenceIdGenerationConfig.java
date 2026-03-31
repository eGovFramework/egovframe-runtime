package org.egovframe.rte.fdl.idgnr.config;

import org.egovframe.rte.fdl.idgnr.impl.EgovSequenceIdGnrServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SequenceIdGenerationConfig {

    private final DataSource dataSource;

    public SequenceIdGenerationConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Sequence 기반 ID 생성기
     */
    @Bean(name = "Ids-TestSequenceNonExistingSequenceName", destroyMethod = "destroy")
    public EgovSequenceIdGnrServiceImpl idsTestSequenceNonExistingSequenceName() {
        EgovSequenceIdGnrServiceImpl generator = new EgovSequenceIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setQuery("SELECT nonexisting-sequence.NEXTVAL FROM DUAL");
        return generator;
    }

    @Bean(name = "Ids-TestSequenceSimpleRequestIdsSize1", destroyMethod = "destroy")
    public EgovSequenceIdGnrServiceImpl idsTestSequenceSimpleRequestIdsSize1() {
        EgovSequenceIdGnrServiceImpl generator = new EgovSequenceIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setQuery("call NEXT VALUE FOR idstest");
        return generator;
    }

    @Bean(name = "Ids-TestSequenceMaxByteIds", destroyMethod = "destroy")
    public EgovSequenceIdGnrServiceImpl idsTestSequenceMaxByteIds() {
        EgovSequenceIdGnrServiceImpl generator = new EgovSequenceIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setQuery("call NEXT VALUE FOR idstest");
        return generator;
    }

    @Bean(name = "Ids-TestSequenceMaxBigDecimalIds", destroyMethod = "destroy")
    public EgovSequenceIdGnrServiceImpl idsTestSequenceMaxBigDecimalIds() {
        EgovSequenceIdGnrServiceImpl generator = new EgovSequenceIdGnrServiceImpl();
        generator.setDataSource(dataSource);
        generator.setUseBigDecimals(true);
        generator.setQuery("call NEXT VALUE FOR idstest");
        return generator;
    }

}
