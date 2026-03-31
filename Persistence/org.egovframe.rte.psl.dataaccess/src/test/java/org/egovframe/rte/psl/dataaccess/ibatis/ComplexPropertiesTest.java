package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.vo.*;
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
import java.util.List;

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
public class ComplexPropertiesTest {

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
    public void testComplexPropertiesOneToOneResultMapSelect() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7369));

        // select
        EmpIncludesDeptVO resultVO = empDAO.selectEmpDeptComplexProperties("selectEmpIncludesDeptResultUsingResultMap", vo);

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
        assertEquals(new BigDecimal(20), resultVO.getDeptVO().getDeptNo());
        assertEquals("RESEARCH", resultVO.getDeptVO().getDeptName());
        assertEquals("DALLAS", resultVO.getDeptVO().getLoc());
    }

    @Rollback(false)
    @Test
    public void testComplexPropertiesOneToManyResultMapSelect() throws ParseException {
        DeptVO vo = new DeptVO();
        vo.setDeptNo(new BigDecimal(20));

        // select
        DeptIncludesEmpListVO resultVO = empDAO.selectDeptEmpListComplexProperties("selectDeptIncludesEmpListResultUsingResultMap", vo);

        // check
        assertNotNull(resultVO);
        assertEquals(new BigDecimal(20), resultVO.getDeptNo());
        assertEquals("RESEARCH", resultVO.getDeptName());
        assertEquals("DALLAS", resultVO.getLoc());
        assertFalse(resultVO.getEmpVOList().isEmpty());
        assertEquals(5, resultVO.getEmpVOList().size());
        assertEquals(new BigDecimal(7369), resultVO.getEmpVOList().get(0).getEmpNo());
        assertEquals("SMITH", resultVO.getEmpVOList().get(0).getEmpName());
        assertEquals("CLERK", resultVO.getEmpVOList().get(0).getJob());
        assertEquals(new BigDecimal(7902), resultVO.getEmpVOList().get(0).getMgr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1980-12-17"), resultVO.getEmpVOList().get(0).getHireDate());
        assertEquals(new BigDecimal(800), resultVO.getEmpVOList().get(0).getSal());
        assertEquals(new BigDecimal(0), resultVO.getEmpVOList().get(0).getComm());
        assertEquals(new BigDecimal(20), resultVO.getEmpVOList().get(0).getDeptNo());
        assertEquals(new BigDecimal(7566), resultVO.getEmpVOList().get(1).getEmpNo());
        assertEquals(new BigDecimal(7788), resultVO.getEmpVOList().get(2).getEmpNo());
        assertEquals(new BigDecimal(7876), resultVO.getEmpVOList().get(3).getEmpNo());
        assertEquals(new BigDecimal(7902), resultVO.getEmpVOList().get(4).getEmpNo());
    }

    @Rollback(false)
    @Test
    public void testComplexPropertiesOneToManyVOListResultMapSelect() {
        DeptVO vo = new DeptVO();
        vo.setDeptName("E");

        // select
        List<DeptIncludesEmpListVO> resultList = empDAO.selectDeptEmpListComplexPropertiesList("selectDeptIncludesEmpListResultListUsingResultMap", vo);

        // check
        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals(new BigDecimal(20), resultList.get(0).getDeptNo());
        assertEquals(new BigDecimal(30), resultList.get(1).getDeptNo());
        assertEquals(new BigDecimal(40), resultList.get(2).getDeptNo());
        assertEquals(5, resultList.get(0).getEmpVOList().size());
        assertEquals(6, resultList.get(1).getEmpVOList().size());
        assertEquals(1, resultList.get(2).getEmpVOList().size());
        assertNull(resultList.get(2).getEmpVOList().get(0).getEmpNo());
        assertEquals(new BigDecimal(7566), resultList.get(0).getEmpVOList().get(1).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(0).getEmpVOList().get(2).getEmpNo());
        assertEquals(new BigDecimal(7876), resultList.get(0).getEmpVOList().get(3).getEmpNo());
        assertEquals(new BigDecimal(7902), resultList.get(0).getEmpVOList().get(4).getEmpNo());
    }

    @Rollback(false)
    @Test
    public void testComplexPropertiesOneToManyVOListRepetitionSelect() {
        DeptVO vo = new DeptVO();
        vo.setDeptName("E");

        // select
        List<DeptIncludesEmpListVO> resultList = empDAO.selectDeptEmpListComplexPropertiesList("selectDeptIncludesEmpListResultListUsingRepetitionSelect", vo);

        // check
        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals(new BigDecimal(20), resultList.get(0).getDeptNo());
        assertEquals(new BigDecimal(30), resultList.get(1).getDeptNo());
        assertEquals(new BigDecimal(40), resultList.get(2).getDeptNo());
        assertEquals(5, resultList.get(0).getEmpVOList().size());
        assertEquals(6, resultList.get(1).getEmpVOList().size());
        assertEquals(0, resultList.get(2).getEmpVOList().size());
        assertEquals(new BigDecimal(7566), resultList.get(0).getEmpVOList().get(1).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(0).getEmpVOList().get(2).getEmpNo());
        assertEquals(new BigDecimal(7876), resultList.get(0).getEmpVOList().get(3).getEmpNo());
        assertEquals(new BigDecimal(7902), resultList.get(0).getEmpVOList().get(4).getEmpNo());
    }

    @Rollback(false)
    @Test
    public void testComplexPropertiesHierarcyRepetitionSelect() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7369));

        EmpIncludesMgrVO resultVO = empDAO.selectEmpMgrHierarchy("selectMgrHierarchy", vo);

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
        assertNotNull(resultVO.getMgrVO());
        assertEquals(new BigDecimal(7902), resultVO.getMgrVO().getEmpNo());
        assertEquals(new BigDecimal(7566), resultVO.getMgrVO().getMgrVO().getEmpNo());
        assertEquals(new BigDecimal(7839), resultVO.getMgrVO().getMgrVO().getMgrVO().getEmpNo());
        assertNull(resultVO.getMgrVO().getMgrVO().getMgrVO().getMgrVO());
    }

}
