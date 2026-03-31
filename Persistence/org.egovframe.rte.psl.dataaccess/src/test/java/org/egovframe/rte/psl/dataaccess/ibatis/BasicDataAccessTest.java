package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.DeptDAO;
import org.egovframe.rte.psl.dataaccess.vo.DeptVO;
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
public class BasicDataAccessTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "deptDAO")
    private DeptDAO deptDAO;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public DeptVO makeVO() {
        DeptVO vo = new DeptVO();
        vo.setDeptNo(new BigDecimal(90));
        vo.setDeptName("test 부서");
        vo.setLoc("test 위치");
        return vo;
    }

    public void checkResult(DeptVO vo, DeptVO resultVO) {
        assertNotNull(resultVO);
        assertEquals(vo.getDeptNo(), resultVO.getDeptNo());
        assertEquals(vo.getDeptName(), resultVO.getDeptName());
        assertEquals(vo.getLoc(), resultVO.getLoc());
    }

    @Rollback(false)
    @Test
    public void testBasicInsert() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDept", vo);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testBasicUpdate() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDept", vo);

        // data change
        vo.setDeptName("upd Dept");
        vo.setLoc("upd loc");

        // update
        int effectedRows = deptDAO.updateDept("updateDept", vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testBasicDelete() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDept", vo);

        // delete
        int effectedRows = deptDAO.deleteDept("deleteDept", vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

        // null 이어야 함
        assertNull(resultVO);
    }

    @Rollback(false)
    @Test
    public void testBasicSelectList() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDept", vo);

        // 검색조건으로 key 설정
        DeptVO searchVO = new DeptVO();
        searchVO.setDeptNo(new BigDecimal(90));

        // selectList
        List<DeptVO> resultList = deptDAO.selectDeptList("selectDeptList", searchVO);

        // key 조건에 대한 결과는 한건일 것임
        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        checkResult(vo, resultList.get(0));

        DeptVO searchVO2 = new DeptVO();
        searchVO2.setDeptName("");

        // selectList
        List<DeptVO> resultList2 = deptDAO.selectDeptList("selectDeptList", searchVO2);

        // like 조건에 대한 결과는 한건 이상일 것임
        assertNotNull(resultList2);
        assertFalse(resultList2.isEmpty());
    }

    @Rollback(false)
    @Test
    public void testInsertUsingParameterMap() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptUsingParameterMap", vo);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testInsertAndSelectUsingParameterClass() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptUsingParameterClass", vo);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testInsertUsingInLineParamWithDBType() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptUsingInLineParamWithDBType", vo);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testInsertAndSelectUsingResultClass() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptUsingParameterClass", vo);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDeptUsingResultClass", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testSelectListWithPaging() {
        DeptVO vo = makeVO();

        vo.setDeptNo(new BigDecimal(90));
        vo.setDeptName("부서 90");
        deptDAO.insertDept("insertDept", vo);

        vo.setDeptNo(new BigDecimal(91));
        vo.setDeptName("부서 91");
        deptDAO.insertDept("insertDept", vo);

        vo.setDeptNo(new BigDecimal(92));
        vo.setDeptName("부서 92");
        deptDAO.insertDept("insertDept", vo);

        vo.setDeptNo(new BigDecimal(93));
        vo.setDeptName("부서 93");
        deptDAO.insertDept("insertDept", vo);

        vo.setDeptNo(new BigDecimal(94));
        vo.setDeptName("부서 94");
        deptDAO.insertDept("insertDept", vo);

        DeptVO searchVO = new DeptVO();
        searchVO.setDeptName("부서");

        // selectList
        List<DeptVO> resultList = deptDAO.selectDeptListWithPaging("selectDeptList", searchVO, 0, 2);

        // like 조건에 대한 결과는 한건 이상일 것임
        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        assertEquals(new BigDecimal(90), resultList.get(0).getDeptNo());
        assertEquals(new BigDecimal(91), resultList.get(1).getDeptNo());

        // selectList
        List<DeptVO> resultList2 = deptDAO.selectDeptListWithPaging("selectDeptList", searchVO, 1, 2);

        // like 조건에 대한 결과는 한건 이상일 것임
        assertNotNull(resultList2);
        assertEquals(2, resultList2.size());
        assertEquals(new BigDecimal(92), resultList2.get(0).getDeptNo());

        // selectList
        List<DeptVO> resultList3 = deptDAO.selectDeptListWithPaging("selectDeptList", searchVO, 2, 2);

        // like 조건에 대한 결과는 한건 이상일 것임
        assertNotNull(resultList3);
        assertEquals(1, resultList3.size());
        assertEquals(new BigDecimal(94), resultList3.get(0).getDeptNo());
    }

}
