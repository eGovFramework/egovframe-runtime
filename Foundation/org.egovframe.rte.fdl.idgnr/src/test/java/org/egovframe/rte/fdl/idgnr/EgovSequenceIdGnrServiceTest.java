package org.egovframe.rte.fdl.idgnr;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTestConfig;
import org.egovframe.rte.fdl.idgnr.config.SequenceIdGenerationConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

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
@ContextConfiguration(classes = {IdgnrTestConfig.class, SequenceIdGenerationConfig.class})
public class EgovSequenceIdGnrServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EgovSequenceIdGnrServiceTest.class);

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "Ids-TestSequenceNonExistingSequenceName")
    private EgovIdGnrService idsTestSequenceNonExistingSequenceName;

    @Resource(name = "Ids-TestSequenceSimpleRequestIdsSize1")
    private EgovIdGnrService idsTestSequenceSimpleRequestIdsSize1;

    @Resource(name = "Ids-TestSequenceMaxByteIds")
    private EgovIdGnrService idsTestSequenceMaxByteIds;

    @Resource(name = "Ids-TestSequenceMaxBigDecimalIds")
    private EgovIdGnrService idsTestSequenceMaxBigDecimalIds;

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

    /**
     * 존재하지 않는 시퀀스 테이블에 의한 처리 시 에러나는 것을 확인하는 테스트
     */
    @Test
    public void testNonExistingSequenceName() {
        try {
            // 1. get next integer id
            idsTestSequenceNonExistingSequenceName.getNextIntegerId();
            fail("Should not have gotten an id");
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * Integer Type 아이디 생성 테스트
     *
     * @throws Exception fail to test
     */
    private static final int TEST_COUNT = 30;

    @Test
    public void testSimpleRequestIdsSize1() throws Exception {

        LOGGER.debug("testSequenceSimpleRequestIdsSize1");

        int testCount = TEST_COUNT;

        // 1. Initialize the counter in the database.
        initializeNextLongId("idstest", 1);

        // 2. get next integer id until 99
        for (int i = 1; i <= testCount; i++) {
            int id = idsTestSequenceSimpleRequestIdsSize1.getNextIntegerId();
            assertEquals(i, id);
        }

        // 3. get next Long id using query directly.
        assertEquals(testCount + 2, peekNextLongId("idstest"));
    }

    /**
     * 다음 Byte Id 읽어오기 테스트
     *
     * @throws Exception fail to test
     */
    @Test
    public void testSequenceMaxByteIds() throws Exception {

        int testCount = 30;
        // max = 127
        long max = Byte.MAX_VALUE;
        long initial = max - testCount;

        // 1. initialize the counter in the database.
        initializeNextLongId("idstest", initial);

        // 2. get next byte id until limitation of Byte
        for (int i = 0; i <= testCount; i++) {
            byte id = idsTestSequenceMaxByteIds.getNextByteId();
            assertEquals(i + initial, id);
        }

        // 3. in case it reached a max value data type
        // allows, get next byte id.
        try {
            byte id = idsTestSequenceMaxByteIds.getNextByteId();
            fail("Should not have gotten an id: " + id);
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * Short Id 읽어오기 테스트
     *
     * @throws Exception fail to test
     */
    @Test
    public void testMaxShortIds() throws Exception {

        int testCount = 30;
        // max is 32767
        long max = Short.MAX_VALUE;
        long initial = max - testCount;

        // 1. Initialize the counter in the database.
        initializeNextLongId("idstest", initial);

        // 2. get next short id until limitation of
        // Short
        for (int i = 0; i <= testCount; i++) {
            short id = idsTestSequenceMaxByteIds.getNextShortId();
            assertEquals(i + initial, id);
        }

        // 3. in case it reached a max value data type
        // allows, get next short
        // id.
        try {
            short id = idsTestSequenceMaxByteIds.getNextShortId();
            fail("Should not have gotten an id: " + id);
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * long Sequence 값 초기화
     *
     * @param sequenceName sequence name
     * @param nextId       next id
     */
    private void initializeNextLongId(String sequenceName, long nextId) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                try {
                    statement.executeUpdate("DROP SEQUENCE " + sequenceName);
                } catch (SQLException se) {
                    LOGGER.debug("Drop Sequence end.");
                }
                statement.executeUpdate("CREATE SEQUENCE " + sequenceName + " START WITH " + nextId);
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to initialize next_id. " + e);
        }
    }

    /**
     * BigDecimal Sequence 값 초기화
     *
     * @param sequenceName sequence name
     * @param nextId       next id
     */
    private void initializeNextBigDecimalId(String sequenceName, BigDecimal nextId) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                try {
                    statement.executeUpdate("DROP SEQUENCE " + sequenceName);
                } catch (SQLException se) {
                    LOGGER.debug("Drop Sequence end.");
                }
                statement.executeUpdate("CREATE SEQUENCE " + sequenceName + " START WITH " + nextId);
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to initialize next_id. " + e);
        }
    }

    /**
     * 쿼리를 통해서 long 유형 Id를 읽어와서 테스트에 검증용으로 사용
     *
     * @param sequenceName sequence name
     * @return next Long Id
     */
    private long peekNextLongId(String sequenceName) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("call NEXT VALUE FOR " + sequenceName + "");
                if (rs.next()) {
                    return rs.getLong(1) + 1;
                } else {
                    fail(sequenceName + " sequence doesn't exist.");
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

    /**
     * 쿼리를 통해서 BigDecimal유형 Id를 읽어와서 테스트에 검증용으로 사용
     *
     * @param sequenceName sequence name
     * @return next BigDecimal Id
     */
    private BigDecimal peekNextBigDecimalId(String sequenceName) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("call NEXT VALUE FOR " + sequenceName + "");
                if (rs.next()) {
                    return rs.getBigDecimal(1).add(new BigDecimal(1));
                } else {
                    fail(sequenceName + " sequence doesn't exist.");
                    return new BigDecimal(-1); // for compiler
                }
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to peek next_id. " + e);
            return new BigDecimal(-1); // for compiler
        }
    }

}
