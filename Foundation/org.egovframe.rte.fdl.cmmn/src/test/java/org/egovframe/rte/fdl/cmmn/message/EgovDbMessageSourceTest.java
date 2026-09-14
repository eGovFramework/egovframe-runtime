package org.egovframe.rte.fdl.cmmn.message;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DB 기반 MessageSource 를 HSQLDB 인메모리 테이블로 검증한다.
 */
public class EgovDbMessageSourceTest {

    private static JDBCDataSource dataSource;

    @BeforeAll
    public static void setUpDatabase() throws Exception {
        dataSource = new JDBCDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:egovmsg");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE EGOV_MESSAGE_SOURCE IF EXISTS");
            statement.execute("CREATE TABLE EGOV_MESSAGE_SOURCE (CODE VARCHAR(100) NOT NULL, LANG VARCHAR(10), MESSAGE VARCHAR(2000) NOT NULL)");
            statement.execute("INSERT INTO EGOV_MESSAGE_SOURCE VALUES ('greeting', 'ko', '안녕하세요 {0}')");
            statement.execute("INSERT INTO EGOV_MESSAGE_SOURCE VALUES ('greeting', 'en', 'Hello {0}')");
            statement.execute("INSERT INTO EGOV_MESSAGE_SOURCE VALUES ('only.default', NULL, '기본 메시지')");
            statement.execute("INSERT INTO EGOV_MESSAGE_SOURCE VALUES ('blank.lang', '', '빈 언어도 기본값')");
            statement.execute("DROP TABLE CUSTOM_MSG IF EXISTS");
            statement.execute("CREATE TABLE CUSTOM_MSG (MSG_CD VARCHAR(100), LOCALE_CD VARCHAR(10), MSG_TXT VARCHAR(2000))");
            statement.execute("INSERT INTO CUSTOM_MSG VALUES ('custom.code', 'ko', '사용자 쿼리 메시지')");
        }
    }

    private EgovDbMessageSource newMessageSource() {
        EgovDbMessageSource messageSource = new EgovDbMessageSource();
        messageSource.setDataSource(dataSource);
        messageSource.afterPropertiesSet();
        return messageSource;
    }

    @Test
    public void testResolvesMessagePerLanguageWithArguments() {
        EgovDbMessageSource messageSource = newMessageSource();

        assertEquals("안녕하세요 eGov", messageSource.getMessage("greeting", new Object[] {"eGov"}, null, Locale.KOREAN));
        assertEquals("Hello eGov", messageSource.getMessage("greeting", new Object[] {"eGov"}, null, Locale.ENGLISH));
        assertEquals("안녕하세요 eGov", messageSource.getMessage("greeting", new Object[] {"eGov"}, null, Locale.KOREA),
                "국가가 붙은 로케일도 언어 코드로 조회한다");
    }

    @Test
    public void testFallsBackToDefaultLanguageRow() {
        EgovDbMessageSource messageSource = newMessageSource();

        assertEquals("기본 메시지", messageSource.getMessage("only.default", null, null, Locale.FRENCH));
        assertEquals("빈 언어도 기본값", messageSource.getMessage("blank.lang", null, null, Locale.JAPANESE));
    }

    @Test
    public void testAggregatesWithParentMessageSource() {
        StaticMessageSource parent = new StaticMessageSource();
        parent.addMessage("parent.only", Locale.KOREAN, "파일 메시지");
        EgovDbMessageSource messageSource = newMessageSource();
        messageSource.setParentMessageSource(parent);

        assertEquals("파일 메시지", messageSource.getMessage("parent.only", null, null, Locale.KOREAN),
                "DB 에 없는 코드는 부모 MessageSource 로 폴백한다");
        assertEquals("안녕하세요 eGov", messageSource.getMessage("greeting", new Object[] {"eGov"}, null, Locale.KOREAN),
                "DB 에 있는 코드는 DB 값이 우선한다");
        assertNull(messageSource.getMessage("no.such.code", null, null, Locale.KOREAN));
    }

    @Test
    public void testRefreshReloadsMessagesWithoutRestart() throws Exception {
        EgovDbMessageSource messageSource = newMessageSource();
        assertNull(messageSource.getMessage("added.later", null, null, Locale.KOREAN));

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO EGOV_MESSAGE_SOURCE VALUES ('added.later', 'ko', '나중에 추가된 메시지')");
        }
        assertNull(messageSource.getMessage("added.later", null, null, Locale.KOREAN), "refresh 전에는 반영되지 않는다");

        messageSource.refresh();
        assertEquals("나중에 추가된 메시지", messageSource.getMessage("added.later", null, null, Locale.KOREAN));

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM EGOV_MESSAGE_SOURCE WHERE CODE = 'added.later'");
        }
        messageSource.refresh();
        assertNull(messageSource.getMessage("added.later", null, null, Locale.KOREAN));
    }

    @Test
    public void testCustomQueryFollowsColumnOrderContract() {
        EgovDbMessageSource messageSource = new EgovDbMessageSource();
        messageSource.setDataSource(dataSource);
        messageSource.setQuery("SELECT MSG_CD, LOCALE_CD, MSG_TXT FROM CUSTOM_MSG");
        messageSource.afterPropertiesSet();

        assertEquals("사용자 쿼리 메시지", messageSource.getMessage("custom.code", null, null, Locale.KOREAN));
        assertNull(messageSource.getMessage("greeting", null, null, Locale.KOREAN), "다른 테이블은 읽지 않는다");
    }

    @Test
    public void testMissingDataSourceIsReportedClearly() {
        EgovDbMessageSource messageSource = new EgovDbMessageSource();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, messageSource::afterPropertiesSet);
        assertTrue(e.getMessage().contains("dataSource"), e.getMessage());
        assertThrows(IllegalStateException.class, messageSource::refresh, "초기화 전 refresh 는 명확한 예외여야 한다");
    }
}
