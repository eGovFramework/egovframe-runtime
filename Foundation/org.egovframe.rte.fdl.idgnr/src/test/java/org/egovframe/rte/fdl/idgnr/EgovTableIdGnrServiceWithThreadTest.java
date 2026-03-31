package org.egovframe.rte.fdl.idgnr;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTestConfig;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTransactionTestConfig;
import org.egovframe.rte.fdl.idgnr.config.TableIdGenerationConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * SequenceId Generation Service Test 클래스
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2009.02.01  김태호          최초 생성
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IdgnrTestConfig.class, TableIdGenerationConfig.class, IdgnrTransactionTestConfig.class})
public class EgovTableIdGnrServiceWithThreadTest {

    /** 테스트 반복 횟수 (CI/빌드 시간 단축) */
    private static final int TEST_COUNT = 20;
    /** 동시 스레드 수 */
    private static final int TEST_THREAD = 4;
    private int[] ids;
    @Resource(name = "dataSource")
    private DataSource dataSource;
    @Resource(name = "Ids-TestSimpleRequestIdsSize1")
    private EgovIdGnrService idsTestSimpleRequestIdsSize1;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @AfterEach
    public void onTearDown() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Test
    @Timeout(30)
    public void testIdsSize1InThread() throws InterruptedException {
        // next_id=0 이면 반환 ID는 0, 1, 2, ... (totalIds-1)
        initializeNextLongId("test", 0);

        int totalIds = TEST_COUNT * TEST_THREAD;
        ids = new int[totalIds];

        Thread[] t = new Thread[TEST_THREAD];
        for (int i = 0; i < TEST_THREAD; i++) {
            t[i] = new Thread(() -> {
                for (int j = 0; j < TEST_COUNT; j++) {
                    try {
                        int id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
                        synchronized (ids) {
                            ids[id] = ids[id] + 1;
                        }
                    } catch (FdlException e) {
                        throw new AssertionError("Unexpected FdlException", e);
                    }
                }
            });
            t[i].start();
        }
        for (int i = 0; i < TEST_THREAD; i++) {
            t[i].join();
        }

        for (int i = 0; i < totalIds; i++) {
            assertEquals(1, ids[i], "ID " + i + " should be allocated exactly once");
        }
        assertEquals(totalIds, peekNextLongId("test"));
    }

    @Test
    @Timeout(30)
    @Transactional
    public void testIdsSize1InThreadWithTransaction() throws InterruptedException {
        initializeNextLongId("test", 0);
        int totalIds = TEST_COUNT * TEST_THREAD;
        ids = new int[totalIds];

        Thread[] t = new Thread[TEST_THREAD];
        for (int i = 0; i < TEST_THREAD; i++) {
            t[i] = new Thread(() -> {
                for (int j = 0; j < TEST_COUNT; j++) {
                    try {
                        int id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
                        synchronized (ids) {
                            ids[id] = ids[id] + 1;
                        }
                    } catch (FdlException e) {
                        throw new AssertionError("Unexpected FdlException", e);
                    }
                }
            });
            t[i].start();
        }
        for (int i = 0; i < TEST_THREAD; i++) {
            t[i].join();
        }

        for (int i = 0; i < totalIds; i++) {
            assertEquals(1, ids[i], "ID " + i + " should be allocated exactly once");
        }
        assertEquals(totalIds, peekNextLongId("test"));
    }

    @Test
    @Timeout(30)
    public void testIdsSize1InThreadWithSeperatedTransaction() throws InterruptedException {
        initializeNextLongId("test", 0);
        int totalIds = TEST_COUNT * TEST_THREAD;
        ids = new int[totalIds];

        Thread[] t = new Thread[TEST_THREAD];
        for (int i = 0; i < TEST_THREAD; i++) {
            t[i] = new Thread(() -> {
                for (int j = 0; j < TEST_COUNT; j++) {
                    runTransaction();
                }
            });
            t[i].start();
        }
        for (int i = 0; i < TEST_THREAD; i++) {
            t[i].join();
        }

        for (int i = 0; i < totalIds; i++) {
            assertEquals(1, ids[i], "ID " + i + " should be allocated exactly once");
        }
        assertEquals(totalIds, peekNextLongId("test"));
    }

    @Transactional
    public void runTransaction() {
        try {
            int id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
            synchronized (ids) {
                ids[id] = ids[id] + 1;
            }
        } catch (FdlException e) {
            throw new AssertionError("Unexpected FdlException", e);
        }
    }

    /**
     * 초기값 세팅
     */
    private void initializeNextLongId(String tableName, long nextId) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                statement.executeUpdate("INSERT INTO idttest (table_name, next_id) VALUES ('" + tableName + "', " + nextId + ")");
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to initialize next_id. " + e);
        }
    }

    /**
     * Query를 통해 long 타입의 ID 직접 읽기
     */
    private long peekNextLongId(String tableName) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("SELECT next_id FROM idttest " + "WHERE table_name = '" + tableName + "'");
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    fail(tableName + " row not in ids table.");
                    return -1; // for compiler
                }
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to peek next_id. " + e);
            return -1; // for compiler
        }
    }

}
