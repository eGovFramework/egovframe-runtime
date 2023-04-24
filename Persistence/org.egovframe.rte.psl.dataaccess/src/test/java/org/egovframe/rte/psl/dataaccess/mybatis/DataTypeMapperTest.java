package org.egovframe.rte.psl.dataaccess.mybatis;

import org.egovframe.rte.psl.dataaccess.TestBase;
import org.egovframe.rte.psl.dataaccess.dao.DataTypeTestMapper;
import org.egovframe.rte.psl.dataaccess.vo.TypeTestVO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.Assert.*;

/**
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 *   2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath*:META-INF/spring/context-*.xml" })
@Transactional
public class DataTypeMapperTest extends TestBase {

	@Resource(name = "dataTypeTestMapper")
	DataTypeTestMapper dataTypeTestMapper;

	@Before
	public void onSetUp() throws Exception {

		// 외부에 sql file 로부터 DB 초기화 (TypeTest 기존 테이블
		// 삭제/생성)
		ScriptUtils.executeSqlScript(dataSource.getConnection(), new ClassPathResource("META-INF/testdata/sample_schema_ddl_typetest_" + usingDBMS + ".sql"));
	}

	public TypeTestVO makeVO() throws Exception {
		TypeTestVO vo = new TypeTestVO();
		vo.setId(1);
		vo.setBigdecimalType(new BigDecimal("99999999999999999.99"));
		vo.setBooleanType(true);
		vo.setByteType((byte) 127);
		// VO 에서 String 으로 선언했음. char 로 하고자 하는 경우
		// TypeHandler 작성 필요
		vo.setCharType("A");
		// Oracle 10g 에서 double precision 타입은
		// Double.MAX_VALUE 를 수용하지 못함.
		// oracle jdbc driver 에서 Double.MAX_VALUE 를
		// 전달하면 Overflow Exception trying to bind
		// 1.7976931348623157E308 에러 발생
		// vo.setDoubleType(Double.MAX_VALUE);
		vo.setDoubleType(isHsql ? Double.MAX_VALUE : 1.7976931348623157d);
		// mysql 5.0 에서 테스트 시 Float.MAX_VALUE 를 입력할 수
		// 없음
		vo.setFloatType(isMysql ? (float) 3.40282 : Float.MAX_VALUE);
		vo.setIntType(Integer.MAX_VALUE);
		vo.setLongType(Long.MAX_VALUE);
		vo.setShortType(Short.MAX_VALUE);
		vo.setStringType("abcd가나다라あいうえおｶｷｸｹｺ");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
		vo.setDateType(sdf.parse("2009-02-18"));
		long currentTime = new java.util.Date().getTime();
		vo.setSqlDateType(new java.sql.Date(currentTime));
		vo.setSqlTimeType(new java.sql.Time(currentTime));
		vo.setSqlTimestampType(new java.sql.Timestamp(currentTime));
		vo.setCalendarType(Calendar.getInstance());

		return vo;
	}

	public void checkResult(TypeTestVO vo, TypeTestVO resultVO) {
		assertNotNull(resultVO);
		assertEquals(vo.getId(), resultVO.getId());
		assertEquals(vo.getBigdecimalType(), resultVO.getBigdecimalType());
		assertEquals(vo.getByteType(), resultVO.getByteType());
		// mysql 인 경우 timestamp 칼럼에 null 을 입력하면 현재 시각으로
		// insert 됨에 유의
		if (vo.getCalendarType() == null && isMysql) {
			assertNotNull(resultVO.getCalendarType());
			// mysql 인 경우 java 의 timestamp 에 비해 3자리 정밀도
			// 떨어짐
		} else if (vo.getCalendarType() != null && isMysql) {
			String orgSeconds = Long.toString(vo.getCalendarType().getTime().getTime());
			String mysqlSeconds = Long.toString(resultVO.getCalendarType().getTime().getTime());
			assertEquals(orgSeconds.substring(0, orgSeconds.length() - 3), mysqlSeconds.substring(0, mysqlSeconds.length() - 3));
		} else {
			assertEquals(vo.getCalendarType(), resultVO.getCalendarType());
		}
		assertEquals(vo.getCharType(), resultVO.getCharType());
		assertEquals(vo.getDateType(), resultVO.getDateType());
		// double 에 대한 delta 를 1e-15 로 주었음.
		assertEquals(vo.getDoubleType(), resultVO.getDoubleType(), isMysql ? 1e-14 : 1e-15);
		// float 에 대한 delta 를 1e-7 로 주었음.
		assertEquals(vo.getFloatType(), resultVO.getFloatType(), 1e-7);
		assertEquals(vo.getIntType(), resultVO.getIntType());
		assertEquals(vo.getLongType(), resultVO.getLongType());
		assertEquals(vo.getShortType(), resultVO.getShortType());
		// java.sql.Date 의 경우 Date 만 비교
		if (vo.getSqlDateType() != null) {
			assertEquals(vo.getSqlDateType().toString(), resultVO.getSqlDateType().toString());
		}

		// mysql 인 경우 timestamp 칼럼에 null 을 입력하면 현재 시각으로
		// insert 됨에 유의
		if (vo.getSqlTimestampType() == null && isMysql) {
			assertNotNull(resultVO.getSqlTimestampType());
		} else if (vo.getCalendarType() != null && isMysql) {
			String orgSeconds = Long.toString(vo.getSqlTimestampType().getTime());
			String mysqlSeconds = Long.toString(resultVO.getSqlTimestampType().getTime());
			assertEquals(orgSeconds.substring(0, orgSeconds.length() - 3), mysqlSeconds.substring(0, mysqlSeconds.length() - 3));
		} else {
			assertEquals(vo.getSqlTimestampType(), resultVO.getSqlTimestampType());
		}
		// java.sql.Time 의 경우 Time 만 비교
		if ((isHsql || isOracle || isTibero || isMysql) && vo.getSqlTimeType() != null) {
			assertEquals(vo.getSqlTimeType().toString(), resultVO.getSqlTimeType().toString());
		} else {
			assertEquals(vo.getSqlTimeType(), resultVO.getSqlTimeType());
		}
		assertEquals(vo.getStringType(), resultVO.getStringType());
		assertEquals(vo.isBooleanType(), resultVO.isBooleanType());

	}

	@Rollback(false)
	@Test
	public void testDataTypeTest() throws Exception {
		// 값을 세팅하지 않고 insert 해 봄 - id 는 int 의 초기값에 따라 0
		// 임
		TypeTestVO vo = new TypeTestVO();

		// insert
		dataTypeTestMapper.insertTypeTest("insertTypeTest", vo);

		// select
		TypeTestVO resultVO = dataTypeTestMapper.selectTypeTest("selectTypeTest", vo);

		// check
		checkResult(vo, resultVO);

		try {
			// duplication 테스트
			dataTypeTestMapper.insertTypeTest("insertTypeTest", vo);

			fail("키 값 duplicate 에러가 발생해야 합니다.");
		} catch (Exception e) {
			assertNotNull(e);
			assertTrue(e instanceof DataIntegrityViolationException);
			assertTrue(e.getCause() instanceof SQLException);
		}

		// DataType 테스트 데이터 입력 및 재조회
		vo = makeVO();

		// insert
		dataTypeTestMapper.insertTypeTest("insertTypeTest", vo);

		// select
		resultVO = dataTypeTestMapper.selectTypeTest("selectTypeTest", vo);

		// check
		checkResult(vo, resultVO);

	}

}
