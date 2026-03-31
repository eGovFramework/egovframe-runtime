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
public class DynamicSQLTest {

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
    public void testDynamicStatement() throws ParseException {
        JobHistVO vo = new JobHistVO();
        vo.setEmpNo(new BigDecimal(7788));

        // select
        List<JobHistVO> resultList = jobHistDAO.selectJobHistList("selectJobHistListUsingDynamicElement", vo);

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
    public void testDynamicUnary() {
        Map<String, Object> map = new HashMap<>();
        map.put("testEmptyString", "");

        List<Object> list = new ArrayList<>();
        map.put("testEmptyCollection", list);

        // isNull 테스트
        map.put("testNull", null);

        // isPropertyAvailable 테스트 - cf.) property 의 값을 null 로 설정하더라도 해당 property 는 Available 한것에 유의!
        map.put("testProperty", null);

        // select
        Map<String, Object> resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicUnary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("empty String", resultMap.get("isEmptyString"));
        assertEquals("empty Collection", resultMap.get("isEmptyCollection"));
        assertEquals("null", resultMap.get("isNull"));
        assertEquals("testProperty Available", resultMap.get("testPropertyAvailable"));

        map.put("testEmptyString", null);

        List<Object> nullList = null;
        map.put("testEmptyCollection", nullList);

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicUnary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("empty String", resultMap.get("isEmptyString"));
        assertEquals("empty Collection", resultMap.get("isEmptyCollection"));

        map.clear();
        map.put("testEmptyString", "aa");

        list.clear();
        list.add("aa");

        map.put("testEmptyCollection", list);
        map.put("testNull", new BigDecimal(0));

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicUnary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("not empty String", resultMap.get("isEmptyString"));
        assertEquals("not empty Collection", resultMap.get("isEmptyCollection"));
        assertEquals("not null", resultMap.get("isNull"));
        assertEquals("testProperty Not Available", resultMap.get("testPropertyAvailable"));
    }

    @Rollback(false)
    @Test
    public void testDynamicBinary() {
        String castTypeScale = "numeric(2)";

        Map<String, Object> map = new HashMap<>();
        map.put("testString", "test");
        map.put("testNumeric", new BigDecimal(10));
        map.put("castTypeScale", castTypeScale);

        // select
        Map<String, Object> resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicBinary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("test", resultMap.get("testString"));
        assertEquals("test : equals", resultMap.get("isEqual"));
        assertEquals(new BigDecimal(10), resultMap.get("testNumeric"));
        assertEquals("10 : equals", resultMap.get("isEqualNumeric"));
        assertEquals("10 <= 10", resultMap.get("isGreaterEqual"));
        assertFalse(resultMap.containsKey("isGreaterThan"));
        assertEquals("10 >= 10", resultMap.get("isLessEqual"));
        assertFalse(resultMap.containsKey("isLessThan"));

        map.clear();
        map.put("testString", "not test");
        map.put("testNumeric", new BigDecimal(11));
        map.put("castTypeScale", castTypeScale);

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicBinary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("not test", resultMap.get("testString"));
        assertEquals("test : not equals", resultMap.get("isEqual"));
        assertEquals(new BigDecimal(11), resultMap.get("testNumeric"));
        assertEquals("10 : not equals", resultMap.get("isEqualNumeric"));
        assertEquals("10 <= 11", resultMap.get("isGreaterEqual"));
        assertEquals("10 < 11", resultMap.get("isGreaterThan"));
        assertFalse(resultMap.containsKey("isLessEqual"));
        assertFalse(resultMap.containsKey("isLessThan"));

        map.clear();
        map.put("testString", null);
        map.put("testNumeric", new BigDecimal(9));
        map.put("castTypeScale", castTypeScale);

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicBinary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("", resultMap.get("testString"));
        assertEquals("test : not equals", resultMap.get("isEqual"));
        assertEquals(new BigDecimal(9), resultMap.get("testNumeric"));
        assertEquals("10 : not equals", resultMap.get("isEqualNumeric"));
        assertFalse(resultMap.containsKey("isGreaterEqual"));
        assertFalse(resultMap.containsKey("isGreaterThan"));
        assertEquals("10 >= 9", resultMap.get("isLessEqual"));
        assertEquals("10 > 9", resultMap.get("isLessThan"));

        map.clear();
        map.put("testString", "test");
        map.put("testOtherString", "test");

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicBinary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("test : equals", resultMap.get("isEqual"));
        assertFalse(resultMap.containsKey("isGreaterEqual"));
        assertFalse(resultMap.containsKey("isGreaterThan"));
        assertFalse(resultMap.containsKey("isLessEqual"));
        assertFalse(resultMap.containsKey("isLessThan"));
        assertEquals("test", resultMap.get("testOtherString"));
        assertEquals("test : testOtherString equals testString", resultMap.get("comparePropertyEqual"));
        assertEquals("'test' >= 'test'", resultMap.get("comparePropertyGreaterEqual"));
        assertFalse(resultMap.containsKey("comparePropertyGreaterThan"));
        assertEquals("'test' <= 'test'", resultMap.get("comparePropertyLessEqual"));
        assertFalse(resultMap.containsKey("comparePropertyLessThan"));

        map.clear();
        map.put("testString", "test");
        map.put("testOtherString", "sample");

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicBinary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("test : equals", resultMap.get("isEqual"));
        assertFalse(resultMap.containsKey("isGreaterEqual"));
        assertFalse(resultMap.containsKey("isGreaterThan"));
        assertFalse(resultMap.containsKey("isLessEqual"));
        assertFalse(resultMap.containsKey("isLessThan"));
        assertEquals("sample", resultMap.get("testOtherString"));
        assertEquals("test : testOtherString not equals testString", resultMap.get("comparePropertyEqual"));
        assertFalse(resultMap.containsKey("comparePropertyGreaterEqual"));
        assertFalse(resultMap.containsKey("comparePropertyGreaterThan"));
        assertEquals("'sample' <= 'test'", resultMap.get("comparePropertyLessEqual"));
        assertEquals("'sample' < 'test'", resultMap.get("comparePropertyLessThan"));

        map.clear();
        map.put("testString", "test");
        map.put("testOtherString", "testa");

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicBinary", map);

        // check
        assertNotNull(resultMap);
        assertEquals("test : equals", resultMap.get("isEqual"));
        assertFalse(resultMap.containsKey("isGreaterEqual"));
        assertFalse(resultMap.containsKey("isGreaterThan"));
        assertFalse(resultMap.containsKey("isLessEqual"));
        assertFalse(resultMap.containsKey("isLessThan"));
        assertEquals("testa", resultMap.get("testOtherString"));
        assertEquals("test : testOtherString not equals testString", resultMap.get("comparePropertyEqual"));
        assertEquals("'testa' >= 'test'", resultMap.get("comparePropertyGreaterEqual"));
        assertEquals("'testa' > 'test'", resultMap.get("comparePropertyGreaterThan"));
        assertFalse(resultMap.containsKey("comparePropertyLessEqual"));
        assertFalse(resultMap.containsKey("comparePropertyLessThan"));
    }

    @Rollback(false)
    @Test
    public void testDynamicParameterPresent() {
        Map<String, Object> map = new HashMap<>();

        // select
        Map<String, Object> resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicParameterPresent", map);

        // check
        assertNotNull(resultMap);
        assertEquals("parameter object exist", resultMap.get("isParameterPresent"));

        map = null;

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicParameterPresent", map);

        // check
        assertNotNull(resultMap);
        assertEquals("parameter object not exist", resultMap.get("isParameterPresent"));
    }

    @Rollback(false)
    @Test
    public void testDynamicIterate() throws ParseException {
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
        List<JobHistVO> resultList = jobHistDAO.getSqlMapClientTemplate().queryForList("selectJobHistListUsingDynamicIterate", resultVO);

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
    public void testDynamicIterateSimple() {
        List<Object> iterateList = new ArrayList<>();
        iterateList.add("a");
        iterateList.add("b");
        iterateList.add("c");

        // select
        Map<String, Object> resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicIterateSimple", iterateList);

        // check
        assertNotNull(resultMap);
        assertEquals("a", resultMap.get("a"));
        assertEquals("b", resultMap.get("b"));
        assertEquals("c", resultMap.get("c"));
        assertFalse(resultMap.containsKey("d"));

        // map 안에 collection 이란 property 로 List 를 넣은 경우
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("collection", iterateList);

        // select
        resultMap = (Map<String, Object>) jobHistDAO.getSqlMapClientTemplate().queryForObject("selectDynamicIterateSimple", map);

        // check
        assertNotNull(resultMap);
        assertEquals("a", resultMap.get("a"));
        assertEquals("b", resultMap.get("b"));
        assertEquals("c", resultMap.get("c"));
        assertFalse(resultMap.containsKey("d"));
    }

    @Rollback(false)
    @Test
    public void testDynamicNestedIterate() throws ParseException {
        Map<String, Object> map = new HashMap<>();
        List<Object> condition = new ArrayList<>();
        Map<String, Object> columnMap1 = new HashMap<String, Object>();
        columnMap1.put("columnName", "DEPT_NO");
        columnMap1.put("columnOperation", "=");
        columnMap1.put("columnValue", new BigDecimal(30));
        condition.add(columnMap1);

        Map<String, Object> columnMap2 = new HashMap<String, Object>();
        columnMap2.put("columnName", "SAL");
        columnMap2.put("columnOperation", "<");
        columnMap2.put("columnValue", new BigDecimal(3000));
        condition.add(columnMap2);

        Map<String, Object> columnMap3 = new HashMap<String, Object>();
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
        List<JobHistVO> resultList = jobHistDAO.getSqlMapClientTemplate().queryForList("selectJobHistListUsingDynamicNestedIterate", map);

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
