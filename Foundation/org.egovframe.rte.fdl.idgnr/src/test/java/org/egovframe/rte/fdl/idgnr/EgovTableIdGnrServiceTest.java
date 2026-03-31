package org.egovframe.rte.fdl.idgnr;

import jakarta.annotation.Resource;
import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.egovframe.rte.fdl.idgnr.config.IdgnrTestConfig;
import org.egovframe.rte.fdl.idgnr.config.TableIdGenerationConfig;
import org.egovframe.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * TableId Generation Service Test 클래스
 * <p>
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2009.02.01	김태호	최초 생성
 * 2013.03.25	한성곤	필드명 속성 처리, JdbcTemplate 방식으로 변경, 초기 id 값 등록(자동 insert 처리), 반복처리 제외
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IdgnrTestConfig.class, TableIdGenerationConfig.class})
public class EgovTableIdGnrServiceTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "basicService")
    private EgovIdGnrService basicService;

    @Resource(name = "Ids-TestSimpleRequestIdsSize1")
    private EgovIdGnrService idsTestSimpleRequestIdsSize1;

    @Resource(name = "Ids-TestSimpleRequestIdsSize10")
    private EgovIdGnrService idsTestSimpleRequestIdsSize10;

    @Resource(name = "Ids-TestSimpleRequestIdsSize100")
    private EgovIdGnrService idsTestSimpleRequestIdsSize100;

    @Resource(name = "Ids-TestBigDecimalRequestIdsSize10")
    private EgovIdGnrService idsTestBigDecimalRequestIdsSize10;

    @Resource(name = "Ids-TestMaxByteIds")
    private EgovIdGnrService idsTestMaxByteIds;

    @Resource(name = "Ids-TestMaxShortIds")
    private EgovIdGnrService idsTestMaxShortIds;

    @Resource(name = "Ids-TestMaxIntegerIds")
    private EgovIdGnrService idsTestMaxIntegerIds;

    @Resource(name = "Ids-TestMaxLongIds")
    private EgovIdGnrService idsTestMaxLongIds;

    @Resource(name = "Ids-TestNotDefinedTableInfo")
    private EgovIdGnrService idsTestNotDefinedTableInfo;

    @Resource(name = "Ids-TestWithGenerationStrategy")
    private EgovIdGnrService idsTestWithGenerationStrategy;

    @Resource(name = "Ids-TestNonExistingTableName")
    private EgovIdGnrService idsTestNonExistingTableName;

    @Resource(name = "Ids-TestWithColumnName")
    private EgovIdGnrService idsTestWithColumnName;

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
    public void testBasicService() throws FdlException {
        // int
        assertNotNull(basicService.getNextIntegerId());
        // short
        assertNotNull(basicService.getNextShortId());
        // byte
        assertNotNull(basicService.getNextByteId());
        // long
        assertNotNull(basicService.getNextLongId());
        // BigDecimal
        assertNotNull(basicService.getNextBigDecimalId());
        // String
        assertNotNull(basicService.getNextStringId());
    }

    /** 테스트 반복 횟수 (빌드 시간 단축) */
    private static final int TEST_COUNT = 30;

    /**
     * Int, long Type의 ID 제공 서비스 테스트 Block Size 1
     */
    @Test
    public void testSimpleRequestIdsSize1() throws FdlException {
        int testCount = TEST_COUNT;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", 1);

        // 2. get next integer id until 99
        for (int i = 1; i <= testCount; i++) {
            int id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
            assertEquals(i, id);
        }

        // 3. get next Long id using query directly.
        assertEquals(testCount + 1, peekNextLongId("test"));
    }

    /**
     * Int, long Type의 ID 제공 서비스 테스트 Block Size 10
     */
    @Test
    public void testSimpleRequestIdsSize10() throws FdlException {
        int testCount = TEST_COUNT;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", 1);

        // 2. get next integer id until 99
        for (int i = 1; i <= testCount; i++) {
            int id = idsTestSimpleRequestIdsSize10.getNextIntegerId();
            assertEquals(i, id);
        }

        // 3. get next Long id using query directly.
        assertEquals(testCount + 1, peekNextLongId("test"));
    }

    /**
     * Int, long Type의 ID 제공 서비스 테스트 Block Size 100
     * - 100개 단위로 블록 할당하므로 30회 호출 후 DB next_id는 1+100=101
     */
    @Test
    public void testSimpleRequestIdsSize100() throws FdlException {
        int testCount = TEST_COUNT;
        int blockSize = 100;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", 1);

        // 2. get next integer id
        for (int i = 1; i <= testCount; i++) {
            int id = idsTestSimpleRequestIdsSize100.getNextIntegerId();
            assertEquals(i, id);
        }

        // 3. block size 100이므로 1회 할당 시 next_id가 1+100=101로 갱신됨
        assertEquals(1 + blockSize, peekNextLongId("test"));
    }

    /**
     * BigDecimal Id 제공 테스트
     */
    @Test
    public void testBigDecimalRequestIdsSize10() throws FdlException {
        if (isBigDecimalImplemented()) {
            int testCount = TEST_COUNT;
            BigDecimal initial = new BigDecimal(Long.MAX_VALUE + "00");

            // 1. Initialize the counter in the database.
            initializeNextBigDecimalId("test", initial);

            // 2. get next bigdecimal id
            for (int i = 0; i < testCount; i++) {
                BigDecimal id = idsTestBigDecimalRequestIdsSize10.getNextBigDecimalId();
                assertEquals(initial.add(new BigDecimal(i)), id);
            }

            // 3. get next integer id using query directly.
            assertEquals(initial.add(new BigDecimal(testCount)), peekNextBigDecimalId("test"));
        } else {
            fail("Test failed because BigDecimals are not implemented in current driver.");
        }
    }

    /**
     * Byte Id 제공
     */
    @Test
    public void testMaxByteIds() throws FdlException {
        int testCount = 30;
        // max = 127
        long max = Byte.MAX_VALUE;
        long initial = max - testCount;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", initial);

        // 2. get next byte id until limitation of Byte
        for (int i = 0; i <= testCount; i++) {
            byte id = idsTestMaxByteIds.getNextByteId();
            assertEquals(i + initial, id);
        }

        // 3. in case it reached a max value data type allows, get next byte id.
        try {
            byte id = idsTestMaxByteIds.getNextByteId();
            fail("Should not have gotten an id: " + id);
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * Short Id 제공 서비스 테스트 & Max Value 체크 테스트
     */
    @Test
    public void testMaxShortIds() throws FdlException {
        int testCount = 30;
        // max is 32767
        long max = Short.MAX_VALUE;
        long initial = max - testCount;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", initial);

        // 2. get next short id until limitation of Short
        for (int i = 0; i <= testCount; i++) {
            short id = idsTestMaxShortIds.getNextShortId();
            assertEquals(i + initial, id);
        }

        // 3. in case it reached a max value data type allows, get next short id.
        try {
            short id = idsTestMaxShortIds.getNextShortId();
            fail("Should not have gotten an id: " + id);
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * int Id 제공 서비스 테스트 & Max Value 체크 테스트
     */
    @Test
    public void testMaxIntegerIds() throws FdlException {
        int testCount = 30;
        // max is 0x7fffffff
        long max = Integer.MAX_VALUE;
        long initial = max - testCount;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", initial);

        // 2. get next integer id until limitation of Integer
        for (int i = 0; i <= testCount; i++) {
            int id = idsTestMaxIntegerIds.getNextIntegerId();
            assertEquals(i + initial, id);
        }

        // 3. in case it reached a max value data type allows, get next integer id.
        try {
            int id = idsTestMaxIntegerIds.getNextIntegerId();
            fail("Should not have gotten an id: " + id);
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * long Id 제공 서비스 테스트 & Max Value 체크 테스트
     */
    @Test
    public void testMaxLongIds() throws FdlException {
        int testCount = 30;
        // max is 0x7fffffffffffffffL
        long max = Long.MAX_VALUE;
        long initial = max - testCount;

        // 1. Initialize the counter in the database.
        initializeNextLongId("test", initial);

        // 2. get next long id until limitation of Long
        for (int i = 0; i <= testCount; i++) {
            long id = idsTestMaxLongIds.getNextLongId();
            assertEquals(i + initial, id);
        }

        // 3. in case it reached a max value data type allows, get next long id.
        try {
            long id = idsTestMaxLongIds.getNextLongId();
            fail("Should not have gotten an id: " + id);
        } catch (Exception e) {
            assertInstanceOf(FdlException.class, e);
        }
    }

    /**
     * BigDecimal ID 제공 서비스 테스트 (Non-BigDecimal 설정 서비스에서 BigDecimal ID 발급)
     * - block size 1인 서비스를 사용해 DB next_id가 1씩 증가하는지 검증
     */
    @Test
    public void testGetBigDecimalfromNonBigDecimal() throws FdlException {
        if (isBigDecimalImplemented()) {
            int testCount = TEST_COUNT;
            BigDecimal initial = new BigDecimal("0");

            // 1. Initialize the counter in the database.
            initializeNextBigDecimalId("test", initial);

            // 2. get next bigdecimal id (block size 1 사용하여 1건씩 할당, 다른 테스트와 상태 공유 방지)
            for (int i = 0; i < testCount; i++) {
                BigDecimal id = idsTestSimpleRequestIdsSize1.getNextBigDecimalId();
                assertEquals(initial.add(new BigDecimal(i)), id);
            }

            // 3. get next id using query directly.
            assertEquals(initial.add(new BigDecimal(testCount)), peekNextBigDecimalId("test"));
        } else {
            fail("Test failed because BigDecimals are not implemented in current driver.");
        }
    }

    /**
     * String ID 제공 서비스
     */
    @Test
    public void testGetStringIdFromLongId() throws FdlException {
        int testCount = TEST_COUNT;
        initializeNextLongId("test", 1);
        for (int i = 1; i <= testCount; i++) {
            String id = idsTestSimpleRequestIdsSize10.getNextStringId();
            assertEquals(i, Integer.parseInt(id));
        }
        assertEquals(testCount + 1, peekNextLongId("test"));
    }

    /**
     * 테이블정보 없을 시 테스트
     */
    @Test
    public void testNotDefinedTableInfo() throws FdlException {
        initializeNextLongId("ids", "id", 1);
        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl("SMPL-", 5, '#');
        String id = idsTestNotDefinedTableInfo.getNextStringId(strategy);
        assertEquals("SMPL-####1", id);
    }

    /**
     * 정책정보를 포함하는 아이디 생성 서비스 테스트
     */
    @Test
    public void testIdGenStrategy() throws FdlException {
        initializeNextLongId("test", 1);

        for (int i = 0; i < 5; i++) {
            assertEquals("TEST-****" + (i + 1), idsTestWithGenerationStrategy.getNextStringId());
        }

        initializeNextLongId("ids", "id", 0);

        String id = idsTestNotDefinedTableInfo.getNextStringId();
        assertEquals("0", id);

        id = idsTestNotDefinedTableInfo.getNextStringId("strategy");
        assertEquals("TEST-****1", id);

        EgovIdGnrStrategyImpl strategy = new EgovIdGnrStrategyImpl("SMPL-", 5, '#');
        id = idsTestNotDefinedTableInfo.getNextStringId(strategy);
        assertEquals("SMPL-####2", id);
    }

    /**
     * 테이블 정보가 없는 경우 테스트
     */
    @Test
    public void testNonExistingTableName() throws FdlException {
        String id = idsTestNonExistingTableName.getNextStringId();
        assertEquals("0", id);
    }

    @Test
    public void testWithColumnName() throws FdlException {
        initializeNextLongId("test", 10);
        String id = idsTestWithColumnName.getNextStringId();
        assertEquals("10", id);
    }

    /**
     * 초기값 세팅
     */
    private boolean isBigDecimalImplemented() {
        String tableName = "foorbar_table";

        initializeNextLongId(tableName, 1);

        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("SELECT next_id FROM idttest " + "WHERE table_name = '" + tableName + "'");
                if (rs.next()) {
                    rs.getBigDecimal(1);
                } else {
                    fail(tableName + " row not in ids table.");
                    return false; // for compiler
                }
            } finally {
                conn.close();
            }
            return true;
        } catch (Exception e) {
            if (e.toString().toLowerCase().indexOf("implemented") > 0) {
                return false;
            }
            fail("Unable to test for BigDecimal support. " + e);
            return false; // for compiler
        }
    }

    /**
     * 초기값 세팅
     */
    private void initializeNextBigDecimalId(String tableName, BigDecimal nextId) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                statement.executeUpdate("INSERT INTO idttest (table_name, next_id) VALUES ('" + tableName + "', '" + nextId.toString() + "')");
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to initialize next_id. " + e);
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
     * 초기값 세팅 (물리테이블명 지정)
     */
    private void initializeNextLongId(String table, String tableName, long nextId) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                statement.executeUpdate("UPDATE " + table + " SET  next_id = " + nextId + " WHERE table_name = '" + tableName + "'");
                conn.commit();
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to initialize next_id. " + e);
        }
    }

    /**
     * Query를 통해 BigDecimal 타입의 ID 직접 읽기
     */
    private BigDecimal peekNextBigDecimalId(String tableName) {
        try {
            Connection conn = dataSource.getConnection();
            try {
                Statement statement = conn.createStatement();
                ResultSet rs = statement.executeQuery("SELECT next_id FROM idttest " + "WHERE table_name = '" + tableName + "'");
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                } else {
                    fail(tableName + " row not in ids table.");
                    return null; // for compiler
                }
            } finally {
                conn.close();
            }
        } catch (Exception e) {
            fail("Unable to peek next_id. " + e);
            return null; // for compiler
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
