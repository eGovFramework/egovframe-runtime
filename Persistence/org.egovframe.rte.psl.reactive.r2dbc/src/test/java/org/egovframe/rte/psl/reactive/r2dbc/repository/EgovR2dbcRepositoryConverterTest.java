package org.egovframe.rte.psl.reactive.r2dbc.repository;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.mapping.R2dbcMappingContext;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EgovR2dbcRepository} 의 Boot 매핑 부품 조립 생성자 검증.
 *
 * <p>커스텀 변환기를 가진 변환기로 조립하면 값 타입 필드의 저장과 조회 매핑에 그 변환기가 쓰인다.
 * 연결 팩토리만 받는 기존 생성자는 종전처럼 기본 변환기로 조립된다.</p>
 */
class EgovR2dbcRepositoryConverterTest {

    /** 커스텀 변환기가 있어야 저장되는 값 타입 */
    record Code(String value) {
    }

    @Table("coded_item")
    static class CodedItem {
        @Id
        private Long id;
        private Code code;

        CodedItem() {
        }

        CodedItem(Code code) {
            this.code = code;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Code getCode() {
            return code;
        }

        public void setCode(Code code) {
            this.code = code;
        }
    }

    @WritingConverter
    static class CodeWriter implements Converter<Code, String> {
        @Override
        public String convert(Code source) {
            return source.value();
        }
    }

    @ReadingConverter
    static class CodeReader implements Converter<String, Code> {
        @Override
        public Code convert(String source) {
            return new Code(source);
        }
    }

    private ConnectionFactory connectionFactory;
    private EgovR2dbcRepository<CodedItem> repository;

    @BeforeEach
    void setUp() {
        connectionFactory = ConnectionFactories.get("r2dbc:h2:mem:///convertertest;DB_CLOSE_DELAY=-1");
        DatabaseClient client = DatabaseClient.create(connectionFactory);
        client.sql("CREATE TABLE IF NOT EXISTS coded_item (id BIGINT AUTO_INCREMENT PRIMARY KEY, code VARCHAR(32))").then().block();
        client.sql("DELETE FROM coded_item").then().block();

        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        R2dbcCustomConversions conversions = R2dbcCustomConversions.of(dialect, List.of(new CodeWriter(), new CodeReader()));
        R2dbcMappingContext mappingContext = new R2dbcMappingContext();
        mappingContext.setSimpleTypeHolder(conversions.getSimpleTypeHolder());
        repository = new EgovR2dbcRepository<>(client, dialect, new MappingR2dbcConverter(mappingContext, conversions));
    }

    @Test
    void Boot_부품_생성자로_조립하면_커스텀_변환기가_저장과_조회에_쓰인다() {
        repository.insertData(new CodedItem(new Code("A-01"))).block();
        repository.insertData(new CodedItem(new Code("B-02"))).block();

        List<CodedItem> items = repository
                .selectAllData(Query.empty().sort(Sort.by("id")), CodedItem.class)
                .collectList().block();

        assertNotNull(items);
        assertEquals(2, items.size());
        assertEquals(List.of("A-01", "B-02"), items.stream().map(item -> item.getCode().value()).toList(),
                "조회 행이 커스텀 읽기 변환기로 값 타입이 된다");
        String stored = DatabaseClient.create(connectionFactory)
                .sql("SELECT code FROM coded_item ORDER BY id")
                .map(row -> row.get(0, String.class))
                .first().block();
        assertEquals("A-01", stored, "쓰기 변환기 규칙대로 문자열로 저장된다");
    }

    @Test
    void 기존_연결_팩토리_생성자는_종전처럼_기본_변환기로_조립된다() {
        EgovR2dbcRepository<CodedItem> legacy = new EgovR2dbcRepository<>(connectionFactory);
        // 종전 조립은 앱 커스텀 변환기를 모른다 — 값 타입을 중첩 엔티티로 보고 거부한다. Boot 부품 생성자가 필요한 이유다
        Throwable thrown = assertThrows(RuntimeException.class,
                () -> legacy.insertData(new CodedItem(new Code("C-03"))).block());
        Throwable root = thrown;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        assertTrue(root instanceof InvalidDataAccessApiUsageException, root.toString());
        assertNotNull(legacy.getDatabaseClient());
    }
}
