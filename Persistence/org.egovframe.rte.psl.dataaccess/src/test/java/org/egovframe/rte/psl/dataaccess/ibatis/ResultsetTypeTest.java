package org.egovframe.rte.psl.dataaccess.ibatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpDAO;
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
import java.util.List;

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
public class ResultsetTypeTest {

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
    public void testResultSetType() {
        List<EmpVO> resultList = empDAO.selectEmpList("selectEmpUsingResultSetType", null);

        // check
        assertNotNull(resultList);
        assertEquals(14, resultList.size());
        assertEquals(new BigDecimal(7369), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7499), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7521), resultList.get(2).getEmpNo());
        assertEquals(new BigDecimal(7566), resultList.get(3).getEmpNo());
        assertEquals(new BigDecimal(7654), resultList.get(4).getEmpNo());
        assertEquals(new BigDecimal(7698), resultList.get(5).getEmpNo());
        assertEquals(new BigDecimal(7782), resultList.get(6).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(7).getEmpNo());
        assertEquals(new BigDecimal(7839), resultList.get(8).getEmpNo());
        assertEquals(new BigDecimal(7844), resultList.get(9).getEmpNo());
        assertEquals(new BigDecimal(7876), resultList.get(10).getEmpNo());
        assertEquals(new BigDecimal(7900), resultList.get(11).getEmpNo());
        assertEquals(new BigDecimal(7902), resultList.get(12).getEmpNo());
        assertEquals(new BigDecimal(7934), resultList.get(13).getEmpNo());

        resultList = empDAO.getSqlMapClientTemplate().queryForList("selectEmpUsingResultSetType", 5, 3);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals(new BigDecimal(7698), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7782), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(2).getEmpNo());
    }

    @Rollback(false)
    @Test
    public void testResultSetTypeScrollInsensitive() {
        List<EmpVO> resultList = empDAO.selectEmpList("selectEmpUsingResultSetTypeScrollInsensitive", null);

        // check
        assertNotNull(resultList);
        assertEquals(14, resultList.size());
        assertEquals(new BigDecimal(7369), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7499), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7521), resultList.get(2).getEmpNo());
        assertEquals(new BigDecimal(7566), resultList.get(3).getEmpNo());
        assertEquals(new BigDecimal(7654), resultList.get(4).getEmpNo());
        assertEquals(new BigDecimal(7698), resultList.get(5).getEmpNo());
        assertEquals(new BigDecimal(7782), resultList.get(6).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(7).getEmpNo());
        assertEquals(new BigDecimal(7839), resultList.get(8).getEmpNo());
        assertEquals(new BigDecimal(7844), resultList.get(9).getEmpNo());
        assertEquals(new BigDecimal(7876), resultList.get(10).getEmpNo());
        assertEquals(new BigDecimal(7900), resultList.get(11).getEmpNo());
        assertEquals(new BigDecimal(7902), resultList.get(12).getEmpNo());
        assertEquals(new BigDecimal(7934), resultList.get(13).getEmpNo());

        resultList = empDAO.getSqlMapClientTemplate().queryForList("selectEmpUsingResultSetTypeScrollInsensitive", 5, 3);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals(new BigDecimal(7698), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7782), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(2).getEmpNo());
    }

    @Rollback(false)
    @Test
    public void testResultSetTypeScrollSensitive() {
        List<EmpVO> resultList = empDAO.selectEmpList("selectEmpUsingResultSetTypeScrollSensitive", null);

        // check
        assertNotNull(resultList);
        assertEquals(14, resultList.size());
        assertEquals(new BigDecimal(7369), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7499), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7521), resultList.get(2).getEmpNo());
        assertEquals(new BigDecimal(7566), resultList.get(3).getEmpNo());
        assertEquals(new BigDecimal(7654), resultList.get(4).getEmpNo());
        assertEquals(new BigDecimal(7698), resultList.get(5).getEmpNo());
        assertEquals(new BigDecimal(7782), resultList.get(6).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(7).getEmpNo());
        assertEquals(new BigDecimal(7839), resultList.get(8).getEmpNo());
        assertEquals(new BigDecimal(7844), resultList.get(9).getEmpNo());
        assertEquals(new BigDecimal(7876), resultList.get(10).getEmpNo());
        assertEquals(new BigDecimal(7900), resultList.get(11).getEmpNo());
        assertEquals(new BigDecimal(7902), resultList.get(12).getEmpNo());
        assertEquals(new BigDecimal(7934), resultList.get(13).getEmpNo());

        resultList = empDAO.getSqlMapClientTemplate().queryForList("selectEmpUsingResultSetTypeScrollSensitive", 5, 3);

        assertNotNull(resultList);
        assertEquals(3, resultList.size());
        assertEquals(new BigDecimal(7698), resultList.get(0).getEmpNo());
        assertEquals(new BigDecimal(7782), resultList.get(1).getEmpNo());
        assertEquals(new BigDecimal(7788), resultList.get(2).getEmpNo());
    }

}
