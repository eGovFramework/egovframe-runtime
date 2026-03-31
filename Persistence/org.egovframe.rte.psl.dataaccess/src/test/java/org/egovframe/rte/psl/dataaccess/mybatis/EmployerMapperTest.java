package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.mapper.EmployerMapper;
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
public class EmployerMapperTest {

    @Resource(name = "dataSource")
    private DataSource dataSource;

    @Resource(name = "employerMapper")
    private EmployerMapper employerMapper;

    @BeforeEach
    public void onSetUp() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(conn, new ClassPathResource("/META-INF/testdata/testdb.sql"));
        }
    }

    public EmpVO makeVO() {
        EmpVO vo = new EmpVO();
        vo.setEmpNo(new BigDecimal(100));
        vo.setEmpName("홍길동");
        vo.setJob("대리");
        return vo;
    }

    public void checkResult(EmpVO vo, EmpVO resultVO) {
        assertNotNull(resultVO);
        assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
        assertEquals(vo.getEmpName(), resultVO.getEmpName());
        assertEquals(vo.getJob(), resultVO.getJob());
    }

    @Test
    public void testInsert() {
        EmpVO vo = makeVO();

        // insert
        employerMapper.insertEmployer(vo);

        // select
        EmpVO resultVO = employerMapper.selectEmployer(vo.getEmpNo());

        // check
        checkResult(vo, resultVO);
    }

    @Test
    public void testUpdate() {
        EmpVO vo = makeVO();

        // insert
        employerMapper.insertEmployer(vo);

        // data change
        vo.setEmpName("홍길서");
        vo.setJob("과장");

        // update
        int effectedRows = employerMapper.updateEmployer(vo);
        assertEquals(1, effectedRows);

        // select
        EmpVO resultVO = employerMapper.selectEmployer(vo.getEmpNo());

        // check
        checkResult(vo, resultVO);
    }

    @Test
    public void testDelete() {
        EmpVO vo = makeVO();

        // insert
        employerMapper.insertEmployer(vo);

        // delete
        int effectedRows = employerMapper.deleteEmployer(vo.getEmpNo());
        assertEquals(1, effectedRows);

        // select
        EmpVO resultVO = employerMapper.selectEmployer(vo.getEmpNo());

        // null 이어야 함
        assertNull(resultVO);
    }

    @Test
    public void testSelectList() {
        EmpVO vo = makeVO();

        // insert
        employerMapper.insertEmployer(vo);

        // 검색조건으로 key 설정
        EmpVO searchVO = new EmpVO();
        searchVO.setEmpName("홍길");

        // selectList
        List<EmpVO> resultList = employerMapper.selectEmployerList(searchVO);

        // key 조건에 대한 결과는 한건일 것임
        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        checkResult(vo, resultList.get(0));
    }

}
