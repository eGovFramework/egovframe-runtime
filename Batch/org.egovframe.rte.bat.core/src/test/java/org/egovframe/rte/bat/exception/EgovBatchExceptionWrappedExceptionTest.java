package org.egovframe.rte.bat.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * EgovBatchException 의 원인 예외 보관 여부를 검증하는 JUnit Test 클래스
 *
 * <pre>
 * == 개정이력(Modification Information) ==
 *
 *   수정일        수정자           수정내용
 *  -------      -------------  ----------------------
 *   2026.08.03                 최초 생성
 * </pre>
 */
public class EgovBatchExceptionWrappedExceptionTest {

    private static final String MESSAGE_KEY = "EGOVBATCH000001";
    private static final String EXPECTED_MESSAGE = "배치실행 중 업무 관련 에러가 발생 하였습니다.";

    private EmbeddedDatabase dataSource;

    @BeforeEach
    public void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("classpath:META-INF/testdata/testdb.sql")
                .build();
    }

    @AfterEach
    public void tearDown() {
        if (dataSource != null) {
            dataSource.shutdown();
        }
    }

    @Test
    @DisplayName("원인 예외를 받는 생성자는 전달된 예외를 그대로 보관한다")
    public void wrappedExceptionIsRetained() {
        Exception cause = new IllegalStateException("배치 대상 데이터가 없습니다.");

        EgovBatchException exception = new EgovBatchException(dataSource, MESSAGE_KEY, cause);

        assertNotNull(exception.getWrappedException(), "전달한 원인 예외가 보관되어야 한다.");
        assertSame(cause, exception.getWrappedException(), "전달한 예외 객체와 동일해야 한다.");
    }

    @Test
    @DisplayName("원인 예외를 받지 않는 생성자는 원인 예외를 보관하지 않는다")
    public void wrappedExceptionIsNullWhenNotSupplied() {
        EgovBatchException exception = new EgovBatchException(dataSource, MESSAGE_KEY);

        assertEquals(null, exception.getWrappedException(), "원인 예외를 전달하지 않으면 비어 있어야 한다.");
    }

    @Test
    @DisplayName("원인 예외를 보관하더라도 메시지키로 조회한 메시지는 그대로 유지된다")
    public void messageIsResolvedFromMessageKey() {
        Exception cause = new IllegalStateException("배치 대상 데이터가 없습니다.");

        EgovBatchException exception = new EgovBatchException(dataSource, MESSAGE_KEY, cause);

        assertEquals(EXPECTED_MESSAGE, exception.getMessage(), "메시지키로 조회한 메시지가 유지되어야 한다.");
    }
}
