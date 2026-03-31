package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.mapper.DepartmentMapper;
import org.egovframe.rte.psl.dataaccess.vo.DeptVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
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
public class DepartmentMapperTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "departmentMapper")
    private DepartmentMapper departmentMapper;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public DeptVO makeVO() {
        DeptVO vo = new DeptVO();
        vo.setDeptNo(new BigDecimal(90));
        vo.setDeptName("총무부");
        vo.setLoc("본사");
        return vo;
    }

    public void checkResult(DeptVO vo, DeptVO resultVO) {
        assertNotNull(resultVO);
        assertEquals(vo.getDeptNo(), resultVO.getDeptNo());
        assertEquals(vo.getDeptName(), resultVO.getDeptName());
        assertEquals(vo.getLoc(), resultVO.getLoc());
    }

    @Test
    public void testInsert() {
        DeptVO vo = makeVO();

        // insert
        departmentMapper.insertDepartment(vo);

        // select
        DeptVO resultVO = departmentMapper.selectDepartment(vo.getDeptNo());

        // check
        checkResult(vo, resultVO);
    }

    @Test
    public void testUpdate() {
        DeptVO vo = makeVO();

        // insert
        departmentMapper.insertDepartment(vo);

        // data change
        vo.setDeptName("개발부");
        vo.setLoc("연구소");

        // update
        int effectedRows = departmentMapper.updateDepartment(vo);
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = departmentMapper.selectDepartment(vo.getDeptNo());

        // check
        checkResult(vo, resultVO);
    }

    @Test
    public void testDelete() {
        DeptVO vo = makeVO();

        // insert
        departmentMapper.insertDepartment(vo);

        // delete
        int effectedRows = departmentMapper.deleteDepartment(vo.getDeptNo());
        assertEquals(1, effectedRows);

        // select
        DeptVO resultVO = departmentMapper.selectDepartment(vo.getDeptNo());

        // check
        assertNull(resultVO);
    }

}
