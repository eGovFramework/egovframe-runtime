package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpGeneralMapper;
import org.egovframe.rte.psl.dataaccess.vo.EmpIncludesEmpListVO;
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
public class CompositeKeyMapperTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "empGeneralMapper")
    private EmpGeneralMapper empGeneralMapper;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    @Rollback(false)
    @Test
    public void testCompositeKeySelect() throws Exception {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(7521));

        // select
        EmpIncludesEmpListVO resultVO = empGeneralMapper.selectEmpIncludesEmpList("selectEmpIncludesSameMgrMoreSalaryEmpList", vo);

        // check
        assertNotNull(resultVO);
        assertEquals(new BigDecimal(7521), resultVO.getEmpNo());
        assertEquals("WARD", resultVO.getEmpName());
        assertEquals("SALESMAN", resultVO.getJob());
        assertEquals(new BigDecimal(7698), resultVO.getMgr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("1981-02-22"), resultVO.getHireDate());
        assertEquals(new BigDecimal(1250), resultVO.getSal());
        assertEquals(new BigDecimal(500), resultVO.getComm());
        assertEquals(new BigDecimal(30), resultVO.getDeptNo());

        assertInstanceOf(List.class, resultVO.getEmpList());
        assertEquals(3, resultVO.getEmpList().size());
        assertEquals(new BigDecimal(7499), resultVO.getEmpList().get(0).getEmpNo());
        assertEquals(new BigDecimal(1600), resultVO.getEmpList().get(0).getSal());
        assertEquals(new BigDecimal(7844), resultVO.getEmpList().get(1).getEmpNo());
        assertEquals(new BigDecimal(1500), resultVO.getEmpList().get(1).getSal());
        assertEquals(new BigDecimal(7654), resultVO.getEmpList().get(2).getEmpNo());
        assertEquals(new BigDecimal(1250), resultVO.getEmpList().get(2).getSal());
    }

}
