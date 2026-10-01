package org.egovframe.rte.fdl.idgnr;

import org.egovframe.rte.fdl.idgnr.impl.EgovTableIdGnrServiceImpl;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 모듈 동봉 채번 테이블 DDL 리소스({@code egov/idgnr/schema-*.sql}) 검증 — 스크립트로 만든 기본 이름의 테이블에 대해
 * {@link EgovTableIdGnrServiceImpl} 기본 구성이 그대로 채번한다.
 *
 * <pre>
 * 개정이력(Modification Information)
 *
 * 수정일		수정자				수정내용
 * ----------------------------------------------
 * 2026.09.30	실행환경 개발팀		최초 생성
 * </pre>
 */
class EgovIdgnrSchemaResourceTest {

    private static DataSource hsqldb(String name, String schemaResource) throws Exception {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:" + name);
        dataSource.setUser("sa");
        dataSource.setPassword("");
        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource(schemaResource));
        }
        return dataSource;
    }

    /** 채번 구현이 {@code messageSource} 빈을 컨텍스트에서 꺼내므로 모듈 번들만 가진 최소 컨텍스트를 만든다 */
    private static GenericApplicationContext contextWithModuleBundle() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.registerBean("messageSource", MessageSource.class, () -> {
            ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
            source.setBasename("classpath:/org/egovframe/rte/fdl/idgnr/messages/idgnr");
            source.setDefaultEncoding("UTF-8");
            return source;
        });
        context.refresh();
        return context;
    }

    private static EgovTableIdGnrServiceImpl defaultTableService(DataSource dataSource, GenericApplicationContext context, String tableName) throws Exception {
        EgovTableIdGnrServiceImpl service = new EgovTableIdGnrServiceImpl();
        service.setDataSource(dataSource);
        service.setTableName(tableName);      // table=ids, table_name/next_id 컬럼은 구현 기본값 그대로 — 스크립트와 일치해야 한다
        service.setBlockSize(2);
        service.setApplicationContext(context);
        service.afterPropertiesSet();
        return service;
    }

    @Test
    void HSQLDB_스크립트로_만든_기본_테이블에서_기본_구성이_연속_채번한다() throws Exception {
        DataSource dataSource = hsqldb("idgnr_schema_hsqldb", "egov/idgnr/schema-hsqldb.sql");
        GenericApplicationContext context = contextWithModuleBundle();
        EgovTableIdGnrServiceImpl service = defaultTableService(dataSource, context, "SCHEMA_TEST");
        try {
            long first = service.getNextLongId();
            long second = service.getNextLongId();
            long third = service.getNextLongId();   // 블록(2) 경계를 넘어 재할당
            assertEquals(first + 1, second);
            assertEquals(second + 1, third);

            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            Integer rows = jdbc.queryForObject("SELECT COUNT(*) FROM ids WHERE table_name = ?", Integer.class, "SCHEMA_TEST");
            assertEquals(1, rows, "첫 채번이 행을 만들고 이후에는 같은 행을 갱신해야 한다");
            Long nextId = jdbc.queryForObject("SELECT next_id FROM ids WHERE table_name = ?", Long.class, "SCHEMA_TEST");
            assertTrue(nextId > third, "next_id 는 발급된 마지막 ID 보다 커야 한다(블록 선점)");
        } finally {
            service.destroy();
            context.close();
        }
    }

    @Test
    void ANSI_스크립트도_HSQLDB_에서_그대로_실행되어_채번한다() throws Exception {
        DataSource dataSource = hsqldb("idgnr_schema_ansi", "egov/idgnr/schema-ansi.sql");
        GenericApplicationContext context = contextWithModuleBundle();
        EgovTableIdGnrServiceImpl service = defaultTableService(dataSource, context, "ANSI_TEST");
        try {
            long first = service.getNextLongId();
            assertEquals(first + 1, service.getNextLongId());
        } finally {
            service.destroy();
            context.close();
        }
    }
}
