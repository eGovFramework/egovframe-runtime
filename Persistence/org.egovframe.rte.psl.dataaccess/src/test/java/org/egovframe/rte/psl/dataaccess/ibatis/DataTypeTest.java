package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.TypeTestDAO;
import org.egovframe.rte.psl.dataaccess.vo.TypeTestVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  SimpleJdbcTestUtils -> JdbcTestUtils 변경
 * 2014.01.22 권윤정  SimpleJdbcTemplate -> JdbcTemplate 변경
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DataAccessTestConfig.class)
@Transactional
public class DataTypeTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "typeTestDAO")
    private TypeTestDAO typeTestDAO;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public TypeTestVO makeVO() throws ParseException {
        TypeTestVO vo = new TypeTestVO();
        vo.setId(1);
        vo.setBigdecimalType(new BigDecimal("99999999999999999.99"));
        vo.setBooleanType(true);
        vo.setByteType((byte) 127);
        vo.setCharType("A");
        vo.setDoubleType(Double.MAX_VALUE);
        vo.setFloatType(Float.MAX_VALUE);
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
        assertEquals(vo.getCalendarType(), resultVO.getCalendarType());
        assertEquals(vo.getCharType(), resultVO.getCharType());
        assertEquals(vo.getDateType(), resultVO.getDateType());
        assertEquals(vo.getDoubleType(), resultVO.getDoubleType(), 1e-15);
        assertEquals(vo.getFloatType(), resultVO.getFloatType(), 1e-7);
        assertEquals(vo.getIntType(), resultVO.getIntType());
        assertEquals(vo.getLongType(), resultVO.getLongType());
        assertEquals(vo.getShortType(), resultVO.getShortType());
        if (vo.getSqlDateType() != null) {
            assertEquals(vo.getSqlDateType().toString(), resultVO.getSqlDateType().toString());
        }
        assertEquals(vo.getSqlTimestampType(), resultVO.getSqlTimestampType());
        if (vo.getSqlTimeType() != null) {
            assertEquals(vo.getSqlTimeType().toString(), resultVO.getSqlTimeType().toString());
        } else {
            assertEquals(vo.getSqlTimeType(), resultVO.getSqlTimeType());
        }
        assertEquals(vo.getStringType(), resultVO.getStringType());
        assertEquals(vo.isBooleanType(), resultVO.isBooleanType());
    }

    @Rollback(false)
    @Test
    public void testDataTypeTest() throws ParseException {
        TypeTestVO vo = new TypeTestVO();

        // insert
        typeTestDAO.insertTypeTest("insertTypeTest", vo);

        // select
        TypeTestVO resultVO = typeTestDAO.selectTypeTest("selectTypeTest", vo);

        // check
        checkResult(vo, resultVO);

        try {
            typeTestDAO.insertTypeTest("insertTypeTest", vo);
            fail("키 값 duplicate 에러가 발생해야 합니다.");
        } catch (Exception e) {
            assertNotNull(e);
            assertInstanceOf(DataIntegrityViolationException.class, e);
            assertInstanceOf(SQLException.class, e.getCause());
        }

        // DataType 테스트 데이터 입력 및 재조회
        vo = makeVO();

        // insert
        typeTestDAO.insertTypeTest("insertTypeTest", vo);

        // select
        resultVO = typeTestDAO.selectTypeTest("selectTypeTest", vo);

        // check
        checkResult(vo, resultVO);
    }

}
