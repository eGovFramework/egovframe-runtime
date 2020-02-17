package egovframework.rte.psl.dataaccess.mapper;

import java.math.BigDecimal;
import java.util.List;

import egovframework.rte.psl.dataaccess.vo.EmpVO;

import org.apache.ibatis.session.ResultHandler;

/**
 *  == 개정이력(Modification Information) ==
 *   
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  FileUsingResultHandler테스트를 위한 selectEmpListToOutFileUsingResultHandler() 메서드 추가
 *
 */
@Mapper("employerMapper")
public interface EmployerMapper {

	public List<EmpVO> selectEmployerList(EmpVO vo);

	public EmpVO selectEmployer(BigDecimal empNo);

	public void insertEmployer(EmpVO vo);

	public int updateEmployer(EmpVO vo);

	public int deleteEmployer(BigDecimal empNo);

	public void selectEmpListToOutFileUsingResultHandler(ResultHandler handler);

}
