package org.egovframe.rte.psl.dataaccess.mybatis;

import jakarta.annotation.Resource;
import org.egovframe.rte.psl.dataaccess.config.DataAccessTestConfig;
import org.egovframe.rte.psl.dataaccess.dao.EmpMapper;
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
public class CacheModelMapperTest {

    @Resource(name = "empMapper")
    EmpMapper empMapper;
    @Resource(name = "dataSource")
    private DataSource dataSource;

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
        checkResult(vo, resultVO, "홍길동");
    }

    public void checkResult(EmpVO vo, EmpVO resultVO, String empName) throws ParseException {
        assertNotNull(resultVO);
        assertEquals(vo.getEmpNo(), resultVO.getEmpNo());
        assertEquals(empName, resultVO.getEmpName());
        assertEquals("CLERK", resultVO.getJob());
        assertEquals(new BigDecimal(7902), resultVO.getMgr());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        assertEquals(sdf.parse("2009-02-18"), resultVO.getHireDate());
        assertEquals(new BigDecimal(800), resultVO.getSal());
        assertNull(resultVO.getComm());
        assertEquals(new BigDecimal(20), resultVO.getDeptNo());
    }

    @Rollback(false)
    @Test
    public void testSelectUsingCacheModelLRU() throws ParseException {
        EmpVO vo = makeVO();

        // insert
        empMapper.insertEmpUsingSelectKey("org.egovframe.rte.psl.dataaccess.EmpMapper.insertEmpUsingSelectKey", vo);
        BigDecimal selectKey = vo.getEmpNo();

        // key 를 딴 값을 받아와 비교를 위해 source vo 에 설정
        vo.setEmpNo(selectKey);

        // select - cache model use
        EmpVO resultVO = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingCacheModelLRU", vo);

        // check
        checkResult(vo, resultVO);

        // select - 같은 조건으로 동일한 쿼리 재실행
        EmpVO resultVO2 = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingCacheModelLRU", vo);

        // check
        checkResult(vo, resultVO2);
        // 결과 객체의 instance 가 같음!
        assertEquals(resultVO, resultVO2);
        assertSame(resultVO, resultVO2);

        // select - cache model 을 사용하지 않는 일반 select
        EmpVO resultVO3 = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingResultMap", vo);

        // check
        checkResult(vo, resultVO3);

        // 결과 객체의 instance 가 다름 (cache 를 사용하지 않으면 매번
        // 새로운 결과 객체가 만들어짐)
        assertNotSame(resultVO, resultVO3);

        // EMP 추가 insert -> <flushOnExecute
        // statement="insertEmpUsingSelectKey" /> 테스트
        EmpVO vo2 = makeVO();
        vo2.setEmpName("홍길순");
        // 기타 속성은 같다고 가정

        // insert - vo2
        empMapper.insertEmpUsingSelectKey("org.egovframe.rte.psl.dataaccess.EmpMapper.insertEmpUsingSelectKey", vo2);
        BigDecimal selectKey2 = vo2.getEmpNo();

        // select - '홍길동' 을 cache 사용하여 재조회
        EmpVO resultVO4 = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingCacheModelLRU", vo);

        // check
        checkResult(vo, resultVO4);

        // 이미 flush 되어 이전 조회한 cache 된 instance 와는 다름
        assertNotSame(resultVO, resultVO4);

        // '홍길순' 조회 및 cache 적용 확인
        vo2.setEmpNo(selectKey2);

        // select - '홍길순' 을 cache 사용하여 조회
        EmpVO resultVO5 = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingCacheModelLRU", vo2);

        // check
        checkResult(vo2, resultVO5, "홍길순");

        // select - '홍길순' 을 cache 사용하여 재조회
        EmpVO resultVO6 = empMapper.selectEmp("org.egovframe.rte.psl.dataaccess.EmpMapper.selectEmpUsingCacheModelLRU", vo2);

        // check
        checkResult(vo2, resultVO6, "홍길순");

        // 결과 객체의 instance 가 같음!
        assertEquals(resultVO5, resultVO6);
        assertSame(resultVO5, resultVO6);
    }

}
