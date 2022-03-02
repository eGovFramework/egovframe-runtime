package org.egovframe.rte.psl.dataaccess;

import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Before;

public class TestBase {

	protected boolean isHsql = false;
	protected boolean isOracle = false;
	protected boolean isMysql = false;
	protected boolean isTibero = false;
	protected boolean isAltibase = false;

	protected static String usingDBMS = "hsql";

	@Resource(name = "jdbcProperties")
	protected Properties jdbcProperties;

	@Resource(name = "dataSource")
	protected DataSource dataSource;

	@Before
	public void setDBMSType() {
		usingDBMS = jdbcProperties.getProperty("usingDBMS");
		isHsql = "hsql".equals(usingDBMS);
		isOracle = "oracle".equals(usingDBMS);
		isMysql = "mysql".equals(usingDBMS);
		isTibero = "tibero".equals(usingDBMS);
		isAltibase = "altibase".equals(usingDBMS);
	}

}
