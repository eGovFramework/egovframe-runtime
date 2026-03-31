package org.egovframe.rte.psl.dataaccess.mapper;

import org.apache.ibatis.session.ResultHandler;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * == 개정이력(Modification Information) ==
 * <p>
 * 수정일      수정자           수정내용
 * -------    --------    ---------------------------
 * 2014.01.22 권윤정  FileUsingResultHandler테스트를 위한 selectEmpListToOutFileUsingResultHandler() 메서드 추가
 */
@EgovMapper("employerMapper")
public interface EmployerMapper {

    List<EmpVO> selectEmployerList(EmpVO vo);

    EmpVO selectEmployer(BigDecimal empNo);

    void insertEmployer(EmpVO vo);

    int updateEmployer(EmpVO vo);

    int deleteEmployer(BigDecimal empNo);

    void selectEmpListToOutFileUsingResultHandler(ResultHandler handler);

}
