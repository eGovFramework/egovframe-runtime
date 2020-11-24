package org.egovframe.rte.psl.dataaccess.dao;

import java.util.Iterator;
import java.util.List;

import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.egovframe.rte.psl.dataaccess.vo.DeptIncludesEmpListVO;
import org.egovframe.rte.psl.dataaccess.vo.DeptVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpDeptSimpleCompositeVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpExtendsDeptVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpIncludesDeptVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpIncludesEmpListVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpIncludesMgrVO;
import org.egovframe.rte.psl.dataaccess.vo.EmpVO;

import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

/**
 *  == 개정이력(Modification Information) ==
 *  
 *   수정일      수정자           수정내용
 *  -------    --------    ---------------------------
 *   2014.01.22 권윤정  FileUsingResultHandler테스트를 위한 selectEmpListToOutFileUsingResultHandler() 메서드 추가
 *
 */
@Repository("empMapper")
public class EmpMapper extends EgovAbstractMapper {

	public void insertEmp(String queryId, EmpVO vo) {
		insert(queryId, vo);
	}

	public int insertEmpUsingSelectKey(String queryId, EmpVO vo) {
		return insert(queryId, vo);
	}

	public Integer batchInsertEmp(String queryId, List<EmpVO> list) {
		Iterator<EmpVO> itr = list.iterator();

		int count = 0;

		while (itr.hasNext()) {
			count += insert(queryId, itr.next());
		}

		return count;
	}

	public EmpVO selectEmp(String queryId, EmpVO vo) {
		return (EmpVO) selectOne(queryId, vo);
	}

	public List<EmpVO> selectEmpList(String queryId, EmpVO vo) {
		return selectList(queryId, vo);
	}

	public List<?> selectList(String statementName, int skipResults, int maxResults) {
		RowBounds rowBounds = new RowBounds(skipResults, maxResults);
		return getSqlSession().selectList(statementName, "EmpVO", rowBounds);
	}

	public EmpExtendsDeptVO selectEmpExtendsDept(String queryId, EmpVO vo) {
		return (EmpExtendsDeptVO) selectOne(queryId, vo);
	}

	public EmpDeptSimpleCompositeVO selectEmpDeptSimpleComposite(String queryId, EmpVO vo) {
		return (EmpDeptSimpleCompositeVO) selectOne(queryId, vo);
	}

	public EmpIncludesDeptVO selectEmpDeptComplexProperties(String queryId, EmpVO vo) {
		return (EmpIncludesDeptVO) selectList(queryId, vo);
	}

	public DeptIncludesEmpListVO selectDeptEmpListComplexProperties(String queryId, DeptVO vo) {
		return (DeptIncludesEmpListVO) selectList(queryId, vo);
	}

	public List<DeptIncludesEmpListVO> selectDeptEmpListComplexPropertiesList(String queryId, DeptVO vo) {
		return selectList(queryId, vo);
	}

	public EmpIncludesMgrVO selectEmpMgrHierarchy(String queryId, EmpVO vo) {
		return (EmpIncludesMgrVO) selectList(queryId, vo);
	}

	public EmpIncludesEmpListVO selectEmpIncludesEmpList(String queryId, EmpVO vo) {
		return (EmpIncludesEmpListVO) selectList(queryId, vo);
	}

	public void selectEmpListToOutFileUsingResultHandler(String queryId, ResultHandler handler) {
		listToOutUsingResultHandler(queryId, handler);
	}
}
