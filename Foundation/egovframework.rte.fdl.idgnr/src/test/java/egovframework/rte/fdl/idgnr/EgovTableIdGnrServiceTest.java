package egovframework.rte.fdl.idgnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import egovframework.rte.fdl.cmmn.exception.FdlException;
import egovframework.rte.fdl.idgnr.impl.strategy.EgovIdGnrStrategyImpl;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

/**
 * TableId Generation Service Test 클래스
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01	김태호	최초 생성
 *   2013.03.25	한성곤	필드명 속성 처리, JdbcTemplate 방식으로 변경, 초기 id 값 등록(자동 insert 처리), 반복처리 제외
 *
 * </pre>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/spring/context-common.xml", "classpath*:/spring/context-tableid.xml" })
public class EgovTableIdGnrServiceTest {

	@Resource(name = "dataSource")
	private DataSource dataSource;

	@Resource(name = "schemaProperties")
	Properties schemaProperties;

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

	/**
	 * Test Case 시작
	 *
	 * @throws Exception fail to initialize
	 */
	@Before
	public void onSetUp() throws Exception {

		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("testdata/" + schemaProperties.getProperty("tab_sample_create")), true);

	}

	@After
	public void onTearDown() throws Exception {

		JdbcTestUtils.executeSqlScript(new JdbcTemplate(dataSource), new ClassPathResource("testdata/" + schemaProperties.getProperty("tab_sample_drop")), true);

	}

	@Test
	public void testBasicService() throws Exception {
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

	/**
	 * Int, long Type의 ID 제공 서비스 테스트 Block Size 1
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testSimpleRequestIdsSize1() throws Exception {

		int testCount = 100;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", 1);

		// 2. get next integer id until 99
		for (int i = 1; i <= testCount; i++) {
			int id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
			assertEquals("The returned id was not what was expected.", i, id);
		}

		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount + 1, peekNextLongId("test"));
	}

	/**
	 * Int,long Type의 ID 제공 서비스 테스트 Block Size 10
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testSimpleRequestIdsSize10() throws Exception {

		int testCount = 100;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", 1);

		// 2. get next integer id until 99
		for (int i = 1; i <= testCount; i++) {
			int id = idsTestSimpleRequestIdsSize10.getNextIntegerId();
			assertEquals("The returned id was not what was expected.", i, id);
		}

		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount + 1, peekNextLongId("test"));
	}

	/**
	 * Int,long Type의 ID 제공 서비스 테스트 Block Size 100
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testSimpleRequestIdsSize100() throws Exception {

		int testCount = 100;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", 1);

		// 2. get next integer id until 99
		for (int i = 1; i <= testCount; i++) {
			int id = idsTestSimpleRequestIdsSize100.getNextIntegerId();
			assertEquals("The returned id was not what was expected.", i, id);
		}
		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount + 1, peekNextLongId("test"));
	}

	/**
	 * BigDecimal Id 제공 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testBigDecimalRequestIdsSize10() throws Exception {
		if (isBigDecimalImplemented()) {

			int testCount = 100;
			BigDecimal initial = new BigDecimal(Long.MAX_VALUE + "00");

			// 1. Initialize the counter in the database.
			initializeNextBigDecimalId("test", initial);

			// 2. get next bigdecimal id
			for (int i = 0; i < testCount; i++) {
				BigDecimal id = idsTestBigDecimalRequestIdsSize10.getNextBigDecimalId();
				assertEquals("The returned id was not what was expected.", initial.add(new BigDecimal(i)), id);
			}

			// 3. get next integer id using query directly.
			assertEquals("The next_id column in the database did not have the expected value.", initial.add(new BigDecimal(testCount)), peekNextBigDecimalId("test"));

		} else {
			fail("Test failed because BigDecimals are not implemented in current driver.");
		}
	}

	/**
	 * Byte Id 제공
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testMaxByteIds() throws Exception {

		int testCount = 100;
		// max = 127
		long max = Byte.MAX_VALUE;
		long initial = max - testCount;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", initial);

		// 2. get next byte id until limitation of Byte
		for (int i = 0; i <= testCount; i++) {
			byte id = idsTestMaxByteIds.getNextByteId();
			assertEquals("The returned id was not what was expected.", i + initial, id);
		}

		// 3. in case it reached a max value data type allows, get next byte id.
		try {
			byte id = idsTestMaxByteIds.getNextByteId();
			fail("Should not have gotten an id: " + id);
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * Short Id 제공 서비스 테스트 & Max Value 체크 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testMaxShortIds() throws Exception {

		int testCount = 100;
		// max is 32767
		long max = Short.MAX_VALUE;
		long initial = max - testCount;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", initial);

		// 2. get next short id until limitation of Short
		for (int i = 0; i <= testCount; i++) {
			short id = idsTestMaxShortIds.getNextShortId();
			assertEquals("The returned id was not what was expected.", i + initial, id);
		}

		// 3. in case it reached a max value data type allows, get next short id.
		try {
			short id = idsTestMaxShortIds.getNextShortId();
			fail("Should not have gotten an id: " + id);
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * int Id 제공 서비스 테스트 & Max Value 체크 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testMaxIntegerIds() throws Exception {

		int testCount = 100;
		// max is 0x7fffffff
		long max = Integer.MAX_VALUE;
		long initial = max - testCount;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", initial);

		// 2. get next integer id until limitation of Integer
		for (int i = 0; i <= testCount; i++) {
			int id = idsTestMaxIntegerIds.getNextIntegerId();
			assertEquals("The returned id was not what was expected.", i + initial, id);
		}

		// 3. in case it reached a max value data type allows, get next integer id.
		try {
			int id = idsTestMaxIntegerIds.getNextIntegerId();
			fail("Should not have gotten an id: " + id);
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * long Id 제공 서비스 테스트 & Max Value 체크 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testMaxLongIds() throws Exception {

		int testCount = 100;
		// max is 0x7fffffffffffffffL
		long max = Long.MAX_VALUE;
		long initial = max - testCount;

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", initial);

		// 2. get next long id until limitation of Long
		for (int i = 0; i <= testCount; i++) {
			long id = idsTestMaxLongIds.getNextLongId();
			assertEquals("The returned id was not what was expected.", i + initial, id);
		}

		// 3. in case it reached a max value data type allows, get next long id.
		try {
			long id = idsTestMaxLongIds.getNextLongId();
			fail("Should not have gotten an id: " + id);
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * BigDecimal ID 제공 서비스 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testGetBigDecimalfromNonBigDecimal() throws Exception {
		if (isBigDecimalImplemented()) {

			int testCount = 100;
			BigDecimal initial = new BigDecimal("0");

			// 1. Initialize the counter in the database.
			initializeNextBigDecimalId("test", initial);

			// 2. get next bigdecimal id
			for (int i = 0; i < testCount; i++) {
				BigDecimal id = idsTestSimpleRequestIdsSize100.getNextBigDecimalId();
				assertEquals("The returned id was not what was expected.", initial.add(new BigDecimal(i)), id);
			}

			// 3. get next integer id using query directly.
			assertEquals("The next_id column in the database did not have the expected value.", initial.add(new BigDecimal(testCount)), peekNextBigDecimalId("test"));
		} else {
			fail("Test failed because BigDecimals are not implemented in current driver.");
		}
	}

	/**
	 * String ID 제공 서비스
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testGetStringIdFromLongId() throws Exception {

		int testCount = 100;

		initializeNextLongId("test", 1);

		for (int i = 1; i <= testCount; i++) {
			String id = idsTestSimpleRequestIdsSize10.getNextStringId();
			assertEquals("The returned id was not what was expected.", new Integer(i).toString(), id);
		}

		assertEquals("The next_id column in the database did not have the expected value.", testCount + 1, peekNextLongId("test"));
	}

	/**
	 * 테이블정보 없을 시 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testNotDefinedTableInfo() throws Exception {

		initializeNextLongId("ids", "id", 1);

		String id = idsTestNotDefinedTableInfo.getNextStringId();
		assertEquals("The returned id was not what was expected.", "1", id);

	}

	/**
	 * 정책정보를 포함하는 아이디 생성 서비스 테스트
	 *
	 * @throws Exception ID 생성 실패시
	 */
	@Test
	public void testIdGenStrategy() throws Exception {

		initializeNextLongId("test", 1);

		// prefix : TEST-, cipers : 5, fillChar :*)
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
	 *
	 * @throws Exception
	 */
	@Test
	public void testNonExistingTableName() throws Exception {
		String id = idsTestNonExistingTableName.getNextStringId();

		assertEquals("The returned id was not what was expected.", "0", id);
	}

	@Test
	public void testWithColumnName() throws Exception {
		initializeNextLongId("test", 10);

		String id = idsTestWithColumnName.getNextStringId();

		assertEquals("The returned id was not what was expected.", "10", id);
	}

	/**
	 * 초기값 세팅
	 * @return boolean 초기값 세팅 성공여부
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
	 * @param tableName
	 *        table name
	 * @param nextId
	 *        next id
	 */
	private void initializeNextBigDecimalId(String tableName, BigDecimal nextId) {
		try {

			Connection conn = dataSource.getConnection();
			try {
				Statement statement = conn.createStatement();

				// Need to quote the BigDecimal as it is larger than normal numbers can be.
				// Was causing problems with MySQL
				statement.executeUpdate("INSERT INTO idttest (table_name, next_id) VALUES ('" + tableName + "', '" + nextId.toString() + "')");
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			fail("Unable to initialize next_id. " + e);
		}
	}

	/**
	 * 초기값 세팅
	 *
	 * @param tableName table name
	 * @param nextId next id
	 */
	private void initializeNextLongId(String tableName, long nextId) {
		try {

			Connection conn = dataSource.getConnection();
			try {
				Statement statement = conn.createStatement();

				statement.executeUpdate("INSERT INTO idttest (table_name, next_id) VALUES ('" + tableName + "', " + nextId + ")");
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			fail("Unable to initialize next_id. " + e);
		}
	}

	/**
	 * 초기값 세팅 (물리테이블명 지정)
	 *
	 * @param tableName table name
	 * @param nextId next id
	 */
	private void initializeNextLongId(String table, String tableName, long nextId) {
		try {

			Connection conn = dataSource.getConnection();
			try {
				Statement statement = conn.createStatement();

				// IDS 테이블의 경우 이미 존재하고 있음을 가정..
				statement.executeUpdate("UPDATE " + table + " SET  next_id = " + nextId + " WHERE table_name = '" + tableName + "'");
			} finally {
				conn.close();
			}
		} catch (Exception e) {

			fail("Unable to initialize next_id. " + e);
		}
	}

	/**
	 * Query를 통해 BigDecimal 타입의 ID 직접 읽기
	 *
	 * @param tableName table name
	 * @return next BigDecimal Id
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
	 *
	 * @param tableName table name
	 * @return next Long Id
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
