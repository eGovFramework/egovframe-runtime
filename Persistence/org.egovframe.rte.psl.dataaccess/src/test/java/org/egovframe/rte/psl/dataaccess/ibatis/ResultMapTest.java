package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.vo.EmpDeptSimpleCompositeVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpExtendsDeptVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
public class ResultMapTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "empDAO")
    private EmpDAO empDAO;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Rollback(false)
    @Test
    public void testResultMapSelect() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7369));

        EmpVO resultVO = empDAO.selectEmp("selectEmpUsingResultMap", vo);

        // check
        assertNotNull(resultVO);
        assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
        assertEquals("SMITH", resultVO.getEmpName());
        assertEquals("CLERK", resultVO.getJob());
        assertEquals(new BigDecimal(7902), resultVO.getMgr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
        assertEquals(new BigDecimal(800), resultVO.getSal());
        assertEquals(new BigDecimal(0), resultVO.getComm());
        assertEquals(new BigDecimal(20), resultVO.getDeptNo());
    }

    @Rollback(false)
    @Test
    public void testExtendsResultMapSelect() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7369));

        EmpExtendsDeptVO resultVO = empDAO.selectEmpExtendsDept("selectEmpExtendsDeptUsingResultMap", vo);

        // check
        assertNotNull(resultVO);
        assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
        assertEquals("SMITH", resultVO.getEmpName());
        assertEquals("CLERK", resultVO.getJob());
        assertEquals(new BigDecimal(7902), resultVO.getMgr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
        assertEquals(new BigDecimal(800), resultVO.getSal());
        assertEquals(new BigDecimal(20), resultVO.getDeptNo());
        assertEquals("RESEARCH", resultVO.getDeptName());
        assertEquals("DALLAS", resultVO.getLoc());
    }

    @Rollback(false)
    @Test
    public void testSimpleCompositeResultMapSelect() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7369));

        // select
        EmpDeptSimpleCompositeVO resultVO = empDAO.selectEmpDeptSimpleComposite("selectEmpDeptSimpleCompositeUsingResultMap", vo);

        // check
        assertNotNull(resultVO);
        assertEquals(new BigDecimal(7369), resultVO.getEmpNo());
        assertEquals("SMITH", resultVO.getEmpName());
        assertEquals("CLERK", resultVO.getJob());
        assertEquals(new BigDecimal(7902), resultVO.getMgr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1980-12-17"), resultVO.getHireDate());
        assertEquals(new BigDecimal(800), resultVO.getSal());
        assertEquals(new BigDecimal(0), resultVO.getComm());
        assertEquals(new BigDecimal(20), resultVO.getDeptNo());
        assertEquals("RESEARCH", resultVO.getDeptName());
        assertEquals("DALLAS", resultVO.getLoc());
    }

}
