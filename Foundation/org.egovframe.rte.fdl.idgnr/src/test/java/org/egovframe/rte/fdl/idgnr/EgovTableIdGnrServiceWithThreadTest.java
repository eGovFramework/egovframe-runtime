package org.egovframe.rte.fdl.idgnr;

import org.egovframe.rte.fdl.cmmn.exception.FdlException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath*:/spring/context-common.xml", "classpath*:/spring/context-transaction.xml", "classpath*:/spring/context-tableid.xml"})
public class EgovTableIdGnrServiceWithThreadTest {

	int testCount = 100;
	int testThread = 10;
	int[] ids = null;

	@Resource(name = "dataSource")
	private DataSource dataSource;

	@Resource(name = "schemaProperties")
	Properties schemaProperties;

	@Resource(name = "Ids-TestSimpleRequestIdsSize1")
	private EgovIdGnrService idsTestSimpleRequestIdsSize1;

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovTableIdGnrServiceWithThreadTest.class);

	/**
	 * Test Case 시작
	 *
	 * @throws Exception fail to initialize
	 */
	@Before
	public void onSetUp() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("testdata/" + schemaProperties.getProperty("tab_sample_create")));
	}

	@After
	public void onTearDown() throws Exception {
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("testdata/" + schemaProperties.getProperty("tab_sample_drop")));
	}

	@Test
	public void testIdsSize1InThread() throws Exception {

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", 0);

		ids = new int[testCount * testThread];

		Thread[] t = new Thread[testThread];

		for (int i = 0; i < testThread; i++) {
			t[i] = new Thread(new Runnable() {

				public void run() {
					for (int i = 1; i <= testCount; i++) {
						int id;
						try {
							id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
							LOGGER.info("Generated ID : {} (ThreadID = {})", id, Thread.currentThread().getId());
							ids[id] = ids[id] + 1;
						} catch (FdlException e) {
							e.printStackTrace();
						}
					}
				}
			});

			t[i].start();
		}

		for (int i = 0; i < testThread; i++) {

			try {
				t[i].join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < ids.length; i++) {
			assertTrue("Some id is duplicated : " + i + " => " + ids[i], ids[i] == 1);
		}

		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount * testThread, peekNextLongId("test"));
	}

	@Test
	@Transactional
	public void testIdsSize1InThreadWithTransaction() throws Exception {

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", 0);

		ids = new int[testCount * testThread];

		Thread[] t = new Thread[testThread];

		for (int i = 0; i < testThread; i++) {
			t[i] = new Thread(new Runnable() {

				public void run() {
					for (int i = 1; i <= testCount; i++) {
						int id;
						try {
							id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
							LOGGER.info("Generated ID : {} (ThreadID = {})", id, Thread.currentThread().getId());
							ids[id] = ids[id] + 1;
						} catch (FdlException e) {
							e.printStackTrace();
						}
					}
				}
			});

			t[i].start();
		}

		for (int i = 0; i < testThread; i++) {

			try {
				t[i].join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < ids.length; i++) {
			assertTrue("Some id is duplicated : " + i + " => " + ids[i], ids[i] == 1);
		}

		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount * testThread, peekNextLongId("test"));
	}

	@Test
	public void testIdsSize1InThreadWithSeperatedTransaction() throws Exception {

		// 1. Initialize the counter in the database.
		initializeNextLongId("test", 0);

		ids = new int[testCount * testThread];

		Thread[] t = new Thread[testThread];

		for (int i = 0; i < testThread; i++) {
			t[i] = new Thread(new Runnable() {

				public void run() {
					for (int i = 1; i <= testCount; i++) {
						transaction();
					}
				}
			});

			t[i].start();
		}

		for (int i = 0; i < testThread; i++) {

			try {
				t[i].join();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < ids.length; i++) {
			assertTrue("Some id is duplicated : " + i + " => " + ids[i], ids[i] == 1);
		}

		// 3. get next Long id using query directly.
		assertEquals("The next_id column in the database did not have the expected value.", testCount * testThread, peekNextLongId("test"));
	}

	@Transactional
	public void transaction() {
		int id;
		try {
			id = idsTestSimpleRequestIdsSize1.getNextIntegerId();
			LOGGER.info("Generated ID : {} (ThreadID = {})", id, Thread.currentThread().getId());
			ids[id] = ids[id] + 1;
		} catch (FdlException e) {
			e.printStackTrace();
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
