package org.egovframe.rte.fdl.idgnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * SequenceId Generation Service Test 클래스
 * @author 실행환경 개발팀 김태호
 * @since 2009.02.01
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2009.02.01  김태호          최초 생성
 *
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:/spring/context-common-hsql.xml", "classpath*:/spring/context-sequenceid.xml" })
public class EgovSequenceIdGnrServiceTest extends AbstractJUnit4SpringContextTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovSequenceIdGnrServiceTest.class);

	@Resource(name = "Ids-TestSequenceNonExistingSequenceName")
	private EgovIdGnrService idsTestSequenceNonExistingSequenceName;

	@Resource(name = "Ids-TestSequenceSimpleRequestIdsSize1")
	private EgovIdGnrService idsTestSequenceSimpleRequestIdsSize1;

	@Resource(name = "Ids-TestSequenceMaxByteIds")
	private EgovIdGnrService idsTestSequenceMaxByteIds;

	@Resource(name = "Ids-TestSequenceMaxBigDecimalIds")
	private EgovIdGnrService idsTestSequenceMaxBigDecimalIds;

	@Resource(name = "dataSource")
	private DataSource dataSource;

	@Resource(name = "schemaProperties")
	Properties schemaProperties;

	/**
	 * 시퀀스 테이블의 생성
	 * @throws Exception
	 *         fail to initialize
	 */
	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("testdata/" + schemaProperties.getProperty("seq_sample_create")));
	}

	@After
	public void onTearDown() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("testdata/" + schemaProperties.getProperty("seq_sample_drop")));
	}

	/**
	 * 존재하지 않는 시퀀스 테이블에 의한 처리 시 에러나는 것을 확인하는 테스트
	 */
	@Test
	public void testNonExistingSequenceName() throws Exception {

		try {
			// 1. get next integer id
			idsTestSequenceNonExistingSequenceName.getNextIntegerId();
			fail("Should not have gotten an id");
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * Integer Type 아이디 생성 테스트
	 * @throws Exception
	 *         fail to test
	 */
	@Test
	public void testSimpleRequestIdsSize1() throws Exception {

		LOGGER.debug("testSequenceSimpleRequestIdsSize1");

		int testCount = 100;

		// 1. Initialize the counter in the database.
		initializeNextLongId("idstest", 1);

		// 2. get next integer id until 99
		for (int i = 1; i <= testCount; i++) {
			int id = idsTestSequenceSimpleRequestIdsSize1.getNextIntegerId();
			assertEquals("The returned id was not what was expected.", i, id);
		}

		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount + 2, peekNextLongId("idstest"));
	}

	/**
	 * 다음 Byte Id 읽어오기 테스트
	 * @throws Exception
	 *         fail to test
	 */
	@Test
	public void testSequenceMaxByteIds() throws Exception {

		int testCount = 100;
		// max = 127
		long max = Byte.MAX_VALUE;
		long initial = max - testCount;

		// 1. initialize the counter in the database.
		initializeNextLongId("idstest", initial);

		// 2. get next byte id until limitation of Byte
		for (int i = 0; i <= testCount; i++) {
			byte id = idsTestSequenceMaxByteIds.getNextByteId();
			assertEquals("The returned id was not what was expected.", i + initial, id);
		}

		// 3. in case it reached a max value data type
		// allows, get next byte id.
		try {
			byte id = idsTestSequenceMaxByteIds.getNextByteId();
			fail("Should not have gotten an id: " + id);
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * Short Id 읽어오기 테스트
	 * @throws Exception
	 *         fail to test
	 */
	@Test
	public void testMaxShortIds() throws Exception {

		int testCount = 100;
		// max is 32767
		long max = Short.MAX_VALUE;
		long initial = max - testCount;

		// 1. Initialize the counter in the database.
		initializeNextLongId("idstest", initial);

		// 2. get next short id until limitation of
		// Short
		for (int i = 0; i <= testCount; i++) {
			short id = idsTestSequenceMaxByteIds.getNextShortId();
			assertEquals("The returned id was not what was expected.", i + initial, id);
		}

		// 3. in case it reached a max value data type
		// allows, get next short
		// id.
		try {
			short id = idsTestSequenceMaxByteIds.getNextShortId();
			fail("Should not have gotten an id: " + id);
		} catch (Exception e) {
			assertTrue(e instanceof FdlException);
		}
	}

	/**
	 * Integer Id 읽어오기 테스트(HSQL이외 DB용)
	 * @throws Exception
	 *         fail to test
	 */
//	@Test
//	public void testMaxIntegerIds() throws Exception {
//
//		int testCount = 100;
//		// max is 0x7fffffff
//		long max = Integer.MAX_VALUE;
//		long initial = max - testCount;
//
//		// 1. Initialize the counter in the database.
//		initializeNextLongId("idstest", initial);
//
//		// 2. get next integer id until limitation of
//		// Integer
//		for (int i = 0; i <= testCount; i++) {
//			int id = idsTestSequenceMaxByteIds.getNextIntegerId();
//			assertEquals("The returned id was not what was expected.", i + initial, id);
//		}
//
//		// 3. in case it reached a max value data type
//		// allows, get next
//		// integer id.
//		try {
//			int id = idsTestSequenceMaxByteIds.getNextIntegerId();
//			fail("Should not have gotten an id: " + id);
//		} catch (Exception e) {
//			assertTrue(e instanceof FdlException);
//		}
//	}

	/**
	 * long Id 읽어오기 테스트(HSQL이외 DB용)
	 * @throws Exception
	 *         fail to test
	 */
//	@Test
//	public void testMaxLongIds() throws Exception {
//
//		int testCount = 100;
//		// max is 0x7fffffffffffffffL
//		long max = Long.MAX_VALUE;
//		long initial = max - testCount;
//
//		// 1. Initialize the counter in the database.
//		initializeNextLongId("idstest", initial);
//		
//		// 2. get next long id until limitation of Long
//		for (int i = 0; i <= testCount; i++) {
//			long id = idsTestSequenceMaxByteIds.getNextLongId();
//			assertEquals("The returned id was not what was expected.", i + initial, id);
//		}
//		max = max+1;
//		// 3. in case it reached a max value data type
//		// allows, get next
//		// long id.
//		try {
//			long id = idsTestSequenceMaxByteIds.getNextLongId();
//			fail("Should not have gotten an id: " + id);
//		} catch (FdlException e) {
//			assertTrue(e instanceof FdlException);
//		}
//	}

	/**
	 * BigDecimal Id 읽어오기 테스트(HSQL이외 DB용)
	 * @throws Exception
	 *         fail to test
	 */
//	@Test
//	public void testBigDecimalIds() throws Exception {
//
//		int testCount = 100;
//
//		BigDecimal max = new BigDecimal(Long.MAX_VALUE);
//		BigDecimal initial = max.subtract(new BigDecimal(testCount));
//
//		initializeNextBigDecimalId("idstest", initial);
//
//		for (int i = 0; i <= testCount; i++) {
//			BigDecimal id = idsTestSequenceMaxBigDecimalIds.getNextBigDecimalId();
//
//			assertEquals("The returned id was not what was expected.", initial.add(new BigDecimal(i)), id);
//		}
//
//		assertEquals("The next_id column in the database did not have the expected value.", initial.add(new BigDecimal(testCount + 1)), peekNextBigDecimalId("idstest"));
//	}

	/**
	 * long Sequence 값 초기화
	 * @param sequenceName
	 *        sequence name
	 * @param nextId
	 *        next id
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
	 * @param sequenceName
	 *        sequence name
	 * @param nextId
	 *        next id
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
	 * @param sequenceName
	 *        sequence name
	 * @return next Long Id
	 */
	private long peekNextLongId(String sequenceName) {

		try {
			Connection conn = dataSource.getConnection();
			try {
				Statement statement = conn.createStatement();

				//ResultSet rs = statement.executeQuery("SELECT " + sequenceName + ".CURRVAL FROM DUAL");
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
	 * @param sequenceName
	 *        sequence name
	 * @return next BigDecimal Id
	 */
	private BigDecimal peekNextBigDecimalId(String sequenceName) {

		try {
			Connection conn = dataSource.getConnection();
			try {
				Statement statement = conn.createStatement();

				//ResultSet rs = statement.executeQuery("SELECT " + sequenceName + ".CURRVAL FROM DUAL");
				ResultSet rs = statement.executeQuery("call NEXT VALUE FOR " + sequenceName + "");

				if (rs.next()) {
					return rs.getBigDecimal(1).add(new BigDecimal(1));
				} else {
					fail(sequenceName + " sequence doesn't exist.");
					return new BigDecimal(-1); // for
												// compiler
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
