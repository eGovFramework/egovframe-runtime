package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
import org.egovframe.rte.psl.dataaccess.dao.JobHistDAO;
import org.egovframe.rte.psl.dataaccess.vo.EmpIncludesEmpListVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;
import org.egovframe.rte.psl.dataaccess.vo.JobHistVO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
@SuppressWarnings("deprecation")
public class ReuseSQLTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "jobHistDAO")
    private JobHistDAO jobHistDAO;

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
    public void testIncludeWithDynamicStatement() throws ParseException {
        JobHistVO vo = new JobHistVO();
        vo.setEmpNo(new BigDecimal(7788));

        // select
        List<JobHistVO> resultList = jobHistDAO.selectJobHistList("selectJobHistListUsingIncludeA", vo);

        // check
        assertNotNull(resultList);
        assertEquals(3, resultList.size());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1987-04-19"), resultList.get(0).getStartDate());
        assertEquals(sdf.parse("1988-04-13"), resultList.get(1).getStartDate());
        assertEquals(sdf.parse("1990-05-05"), resultList.get(2).getStartDate());

        // 입력 파라메터 객체의 property 에 따른 Dynamic 테스트
        vo.setEmpNo(null);

        // select
        resultList = jobHistDAO.selectJobHistList("selectJobHistListUsingDynamicElement", vo);

        // check
        assertNotNull(resultList);
        assertEquals(17, resultList.size());
    }

    @Rollback(false)
    @Test
    public void testIncludeWithDynamicIterate() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7521));

        // select
        EmpIncludesEmpListVO resultVO = empDAO.selectEmpIncludesEmpList("selectEmpIncludesSameMgrMoreSalaryEmpList", vo);

        // check
        assertNotNull(resultVO);
        assertEquals(new BigDecimal(7521), resultVO.getEmpNo());
        assertEquals("WARD", resultVO.getEmpName());
        assertInstanceOf(List.class, resultVO.getEmpList());
        assertEquals(3, resultVO.getEmpList().size());
        assertEquals(new BigDecimal(7499), resultVO.getEmpList().get(0).getEmpNo());
        assertEquals(new BigDecimal(1600), resultVO.getEmpList().get(0).getSal());
        assertEquals(new BigDecimal(7844), resultVO.getEmpList().get(1).getEmpNo());
        assertEquals(new BigDecimal(1500), resultVO.getEmpList().get(1).getSal());
        assertEquals(new BigDecimal(7654), resultVO.getEmpList().get(2).getEmpNo());
        assertEquals(new BigDecimal(1250), resultVO.getEmpList().get(2).getSal());

        // select
        List<JobHistVO> resultList = jobHistDAO.getSqlMapClientTemplate().queryForList("selectJobHistListUsingIncludeB", resultVO);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals(new BigDecimal(7499), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7654), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7844), resultList.get(2).getEmpNo());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1981-02-20"), resultList.get(0).getStartDate());
        assertEquals(sdf.parse("1981-09-28"), resultList.get(1).getStartDate());
        assertEquals(sdf.parse("1981-09-08"), resultList.get(2).getStartDate());
    }

    @Rollback(false)
    @Test
    public void testIncludeWithDynamicNestedIterate() throws ParseException {
        Map<String, Object> map = new HashMap<>();
        List<Object> condition = new ArrayList<>();
        Map<String, Object> columnMap1 = new HashMap<>();
        columnMap1.put("columnName", "DEPT_NO");
        columnMap1.put("columnOperation", "=");
        columnMap1.put("columnValue", new BigDecimal(30));
        condition.add(columnMap1);

        Map<String, Object> columnMap2 = new HashMap<>();
        columnMap2.put("columnName", "SAL");
        columnMap2.put("columnOperation", "<");
        columnMap2.put("columnValue", new BigDecimal(3000));
        condition.add(columnMap2);

        Map<String, Object> columnMap3 = new HashMap<>();
        columnMap3.put("columnName", "JOB");
        columnMap3.put("columnOperation", "in");
        List<Object> jobList = new ArrayList<Object>();
        jobList.add("CLERK");
        jobList.add("SALESMAN");
        columnMap3.put("columnValue", jobList);
        columnMap3.put("nested", "true");
        condition.add(columnMap3);
        map.put("condition", condition);

        // select
        List<JobHistVO> resultList = jobHistDAO.getSqlMapClientTemplate().queryForList("selectJobHistListUsingIncludeC", map);

        // check
        assertNotNull(resultList);
        assertEquals(5, resultList.size());
        assertEquals(new BigDecimal(7499), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7521), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7654), resultList.get(2).getEmpNo());
        assertEquals(new BigDecimal(7844), resultList.get(3).getEmpNo());
        assertEquals(new BigDecimal(7900), resultList.get(4).getEmpNo());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1981-02-20"), resultList.get(0).getStartDate());
        assertEquals(sdf.parse("1981-02-22"), resultList.get(1).getStartDate());
        assertEquals(sdf.parse("1981-09-28"), resultList.get(2).getStartDate());
        assertEquals(sdf.parse("1981-09-08"), resultList.get(3).getStartDate());
        assertEquals(sdf.parse("1983-01-15"), resultList.get(4).getStartDate());
    }

}
