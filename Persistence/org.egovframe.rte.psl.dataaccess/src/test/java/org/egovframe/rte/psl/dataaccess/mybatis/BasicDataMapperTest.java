package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.DeptMapper;
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
public class BasicDataMapperTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "deptMapper")
    private DeptMapper deptMapper;

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
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testBasicUpdate() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

        // data change
        vo.setDeptName("upd Dept");
        vo.setLoc("upd loc");

        // update
        int effectedRows = deptMapper.updateDept("org.egovframe.rte.psl.dataaccess.DeptMapper.updateDept", vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testBasicDelete() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

        // delete
        int effectedRows = deptMapper.deleteDept("org.egovframe.rte.psl.dataaccess.DeptMapper.deleteDept", vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

        // null 이어야 함
        assertNull(resultVO);
    }

    @Rollback(false)
    @Test
    public void testBasicSelectList() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDept", vo);

        // 검색조건으로 key 설정
        DeptVO searchVO = new DeptVO();
        searchVO.setDeptNo(new BigDecimal(90));

        // selectList
        List<DeptVO> resultList = deptMapper.selectDeptList("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptList", searchVO);

        // key 조건에 대한 결과는 한건일 것임
        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        checkResult(vo, resultList.get(0));

        DeptVO searchVO2 = new DeptVO();
        searchVO2.setDeptName("");

        // selectList
        List<DeptVO> resultList2 = deptMapper.selectDeptList("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptList", searchVO2);

        // like 조건에 대한 결과는 한건 이상일 것임
        assertNotNull(resultList2);
        assertFalse(resultList2.isEmpty());
    }

    @Rollback(false)
    @Test
    public void testInsertUsingParameterMap() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingParameterMap", vo);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testInsertAndSelectUsingParameterClass() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingParameterClass", vo);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testInsertUsingInLineParamWithDBType() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingInLineParamWithDBType", vo);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDept", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testInsertAndSelectUsingResultClass() {
        DeptVO vo = makeVO();

        // insert
        deptMapper.insertDept("org.egovframe.rte.psl.dataaccess.DeptMapper.insertDeptUsingParameterClass", vo);

        // select
        DeptVO resultVO = deptMapper.selectDept("org.egovframe.rte.psl.dataaccess.DeptMapper.selectDeptUsingResultClass", vo);

        // check
        checkResult(vo, resultVO);
    }

}
