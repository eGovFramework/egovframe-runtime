package org.egovframe.rte.fdl.string;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author sjyoon
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/context-*.xml" })
public class EgovObjectUtilTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EgovObjectUtilTest.class);

	@Before
    public void onSetUp() throws Exception {

		LOGGER.debug("###### EgovObjectUtilTest.onSetUp START ######");

		LOGGER.debug("###### EgovObjectUtilTest.onSetUp END ######");

    }

    @After
    public void onTearDown() throws Exception {

    	LOGGER.debug("###### EgovObjectUtilTest.onTearDown START ######");

    	LOGGER.debug("###### EgovObjectUtilTest.onTearDown END ######");
    }

	/**
	 * [Flow #-1] Positive Case : check which string is empty or not
	 */
	@Test
	public void testIsNull() throws Exception {

		assertFalse(EgovObjectUtil.isNull(new Object()));

		assertTrue(EgovObjectUtil.isNull(null));
	}

	/**
	 * [Flow #-2 Positive Case : check which Obejct is empty
	 * @throws Exception
	 */
	@Test
	public void testIsEmpty() throws Exception{
		ArrayList<String> list = new ArrayList<>();
		list.add("12124");

		assertFalse(EgovObjectUtil.isEmpty(new Object()));
		assertTrue(EgovObjectUtil.isEmpty(null));
		assertFalse(EgovObjectUtil.isEmpty(list));
	}

	/**
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testLoadClass() throws Exception {

		String className = "org.egovframe.rte.fdl.string.EgovStringUtil";
		String wrongClassName = "org.egovframe.rte.fdl.string.EgovStringUtil1";

		Class<?> clazz = EgovObjectUtil.loadClass(className);
		assertNotNull(clazz);

		clazz = null;
		assertNull(clazz);

		Class<Exception> exceptionClass = null;

		try {
			clazz = EgovObjectUtil.loadClass(wrongClassName);
		} catch (Exception e) {
			LOGGER.error("### Exception : {}", e.toString());
			exceptionClass = (Class<Exception>) e.getClass();
		} finally {
			assertEquals(ClassNotFoundException.class, exceptionClass);
			assertNotSame(InstantiationException.class, exceptionClass);
			assertNotSame(IllegalAccessException.class, exceptionClass);
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void testInstantiate() throws Exception {

		String className = "java.lang.String";
		String wrongClassName = "java.lang.String1";
		//String staticClassName = "org.egovframe.rte.fdl.string.EgovStringUtil";

		Object object = EgovObjectUtil.instantiate(className);
		assertNotNull(object);

		String string = (String) object;
		string = "eGovFramework";
		assertEquals("Framework", string.substring(4));

		object = null;
		assertNull(object);

		// wrongClass : ClassNotFoundException
		Class<Exception> exceptionClass = null;

		try {
			object = EgovObjectUtil.instantiate(wrongClassName);
		} catch (Exception e) {
			LOGGER.error("### Exception : {}", e.toString());
			exceptionClass = (Class<Exception>) e.getClass();
		} finally {
			assertEquals(ClassNotFoundException.class, exceptionClass);
			assertNotSame(InstantiationException.class, exceptionClass);
			assertNotSame(IllegalAccessException.class, exceptionClass);
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testInstantiateParamConstructor() throws Exception {
		String className = "java.lang.StringBuffer";
		String[] types = new String[]{"java.lang.String"};
		Object[] values = new Object[]{"전자정부 공통서비스"};

		StringBuffer sb = (StringBuffer)EgovObjectUtil.instantiate(className, types, values);

		sb.append(" 및 개발프레임워크 구축 사업");

		assertEquals("전자정부 공통서비스 및 개발프레임워크 구축 사업", sb.toString());
		LOGGER.debug(sb.toString());
	}
}
