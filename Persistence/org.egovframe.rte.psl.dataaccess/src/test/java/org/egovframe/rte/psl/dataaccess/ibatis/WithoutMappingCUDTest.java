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
public class WithoutMappingCUDTest {

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
        vo.setDeptName("test Dept");
        vo.setLoc("test Loc");
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
    public void testSimpleInsert() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptSimple", vo);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDeptSimpleUsingResultClass", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testSimpleUpdate() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptSimple", vo);

        // data change
        vo.setDeptName("upd Dept");
        vo.setLoc("upd loc");

        // update
        int effectedRows = deptDAO.updateDept("updateDeptSimple", vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDeptSimpleUsingResultClass", vo);

        // check
        checkResult(vo, resultVO);
    }

    @Rollback(false)
    @Test
    public void testSimpleDelete() {
        DeptVO vo = makeVO();

        // insert
        deptDAO.insertDept("insertDeptSimple", vo);

        // delete
        int effectedRows = deptDAO.deleteDept("deleteDeptSimple", vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = deptDAO.selectDept("selectDeptSimpleUsingResultClass", vo);

        // null 이어야 함
        assertNull(resultVO);
    }

}
