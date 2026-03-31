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
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
public class SelectKeyTest {

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

    public EmpVO makeVO() throws ParseException {
        EmpVO vo = new EmpVO();
        vo.setEmpName("홍길동");
        vo.setJob("CLERK");
        vo.setMgr(new BigDecimal(7902));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        vo.setHireDate(sdf.parse("2009-02-18"));
        vo.setSal(new BigDecimal(800));
        vo.setDeptNo(new BigDecimal(20));
        return vo;
    }

    public void checkResult(EmpVO vo, EmpVO resultVO) throws ParseException {
        assertNotNull(resultVO);
        assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
        assertEquals("홍길동", resultVO.getEmpName());
        assertEquals("CLERK", resultVO.getJob());
        assertEquals(new BigDecimal(7902), resultVO.getMgr());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("2009-02-18"), resultVO.getHireDate());
        assertEquals(new BigDecimal(800), resultVO.getSal());
        assertEquals(new BigDecimal(0), resultVO.getComm());
        assertEquals(new BigDecimal(20), resultVO.getDeptNo());
    }

    @Rollback(false)
    @Test
    public void testInsertUsingSelectKey() throws ParseException {
        EmpVO vo = makeVO();

        // insert
        BigDecimal selectKey = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo);

        // key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
        vo.setEmpNo(selectKey);

        // select
        EmpVO resultVO = empDAO.selectEmp("selectEmpUsingResultMap", vo);

        // check
        checkResult(vo, resultVO);

        // delete all
        empDAO.getSqlMapClientTemplate().delete("deleteEmpAll");

        EmpVO vo2 = makeVO();

        // 처음 데이터 입력인 경우는 1000 으로 key 를 설정토록 작성하였음.
        BigDecimal selectKey2 = empDAO.insertEmpUsingSelectKey("insertEmpUsingSelectKey", vo2);

        assertEquals(new BigDecimal(1000), selectKey2);

        vo2.setEmpNo(selectKey2);

        // check
        checkResult(vo, resultVO);
    }

}
