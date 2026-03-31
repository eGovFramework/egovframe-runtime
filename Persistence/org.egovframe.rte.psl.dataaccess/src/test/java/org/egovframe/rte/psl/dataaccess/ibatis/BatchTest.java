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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
public class BatchTest {

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

    public EmpVO makeVO() {
        return makeVO(1000);
    }

    public EmpVO makeVO(int id) {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(id));
        vo.setEmpName("name" + id);
        vo.setJob("CLERK");
        return vo;
    }

    private List<EmpVO> makeVOList() {
        List<EmpVO> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(makeVO(1000 + i));
        }
        return list;
    }

    @Test
    public void testBatchInsert() {
        List<EmpVO> list = makeVOList();
        Integer row = empDAO.batchInsertEmp("insertEmpUsingBatch", list);
        assertNotNull(row);
    }

}
